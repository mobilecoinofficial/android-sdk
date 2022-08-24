// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FogSyncException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.KexRngException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.util.Hex;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fog_common.FogCommon;
import fog_ledger.Ledger;
import fog_view.View;
import fog_view.View.DecommissionedIngestInvocation;

class TxOutStore implements Parcelable {
    private static final String TAG = TxOutStore.class.getName();

    // Bump serial version and read/write code if fields change
    private static final long serialVersionUID = 2L;

    protected static final UnsignedLong FOG_SYNC_THRESHOLD = UnsignedLong.TEN;

    // A map of nonce -> Seed.
    private HashMap<Integer, FogSeed> seeds;

    private Set<Long> decommissionedIngestInvocationIds;
    // AccountKey.
    private AccountKey accountKey;
    // Block index reported from ledger server.
    private UnsignedLong ledgerBlockIndex;
    // Block index reported from view server.
    private UnsignedLong viewBlockIndex;
    // Last known consensus block index.
    private UnsignedLong consensusBlockIndex;
    // The last event id serves as a cursor for Fog View events.
    private long lastKnownFogViewEventId;
    private UnsignedLong ledgerTotalTxCount;

    // TxOuts recovered from missed blocks
    private ConcurrentLinkedQueue<OwnedTxOut> recoveredTxOuts;

    TxOutStore(@NonNull AccountKey accountKey) {
        this.seeds = new HashMap<>();
        this.decommissionedIngestInvocationIds = new HashSet<>();
        this.accountKey = accountKey;
        this.ledgerBlockIndex = UnsignedLong.ZERO;
        this.viewBlockIndex = UnsignedLong.ZERO;
        this.consensusBlockIndex = UnsignedLong.ZERO;
        this.recoveredTxOuts = new ConcurrentLinkedQueue<>();
    }

    static String createStorageKey(AccountKey accountKey) {
        return accountKey.hashCode() + "-" + TAG;
    }

    @NonNull
    static TxOutStore fromBytes(@NonNull byte[] serialized) throws SerializationException {
        Logger.i(TAG, "Deserializing the txo store from bytes");
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(serialized, 0, serialized.length);
        parcel.setDataPosition(0);
        TxOutStore deserialized = CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return deserialized;
    }

    @NonNull
    byte[] toByteArray() throws SerializationException {
        Logger.i(TAG, "Serializing txo store");
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        byte serialized[] = parcel.marshall();
        parcel.recycle();
        return serialized;
    }

    /**
     * Gets the list of synchronized TXOs we are aware of. A "synchronized TXO" is a TXO that we
     * were able to check key images for.
     */
    @NonNull
    synchronized Set<OwnedTxOut> getSyncedTxOuts() {
        // it's possible to have overlapping TxOuts in different Seeds
        // HashSet will leave the set unchanged if the element exists
        HashSet<OwnedTxOut> syncedTxOuts = new HashSet<>();
        for (FogSeed seed : seeds.values()) {
            for (OwnedTxOut txOut : seed.getTxOuts()) {
                if ((getCurrentBlockIndex().equals(UnsignedLong.ZERO)
                        || (txOut.getReceivedBlockIndex().compareTo(getCurrentBlockIndex()) <= 0))) {
                    syncedTxOuts.add(txOut);
                }
            }
        }
        syncedTxOuts.addAll(recoveredTxOuts.stream().filter(txOut ->
                getCurrentBlockIndex().equals(UnsignedLong.ZERO)
                        || txOut.getReceivedBlockIndex().compareTo(getCurrentBlockIndex()) <= 0)
                .collect(Collectors.toList()));
        return syncedTxOuts;
    }

    @NonNull
    Set<OwnedTxOut> getUnspentTxOuts() {
        return getSyncedTxOuts().stream().filter(p -> !p.isSpent(getCurrentBlockIndex()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    void setAccountKey(@NonNull AccountKey accountKey) {
        this.accountKey = accountKey;
    }

    @NonNull
    synchronized UnsignedLong getCurrentBlockIndex() {
        return ledgerBlockIndex.compareTo(viewBlockIndex) < 0
                ? ledgerBlockIndex
                : viewBlockIndex;
    }

    @NonNull
    synchronized UnsignedLong getLedgerTotalTxCount() {
        return ledgerTotalTxCount;
    }

    void refresh(
            @NonNull AttestedViewClient viewClient,
            @NonNull AttestedLedgerClient ledgerClient,
            @NonNull FogBlockClient blockClient
    ) throws InvalidFogResponse, NetworkException, AttestationException, FogSyncException {
        // update RNGs, TxOuts, and fog misses
        Set<BlockRange> fogMisses;
        try {
            fogMisses = updateRNGsAndTxOuts(viewClient, new DefaultFogQueryScalingStrategy(),
                    new DefaultFogSeedProvider(), new DefaultVersionedCryptoBox());
            // Find the first RNG
            Optional<FogSeed> firstRngSeed = seeds.values().stream()
                    .min((o1, o2) -> o1.getStartBlock().compareTo(o2.getStartBlock()));

            // Skip all the blocks that come before the first RNG
            if (firstRngSeed.isPresent()) {
                final UnsignedLong finalMinBlockIndex = firstRngSeed.get().getStartBlock();
                Set<BlockRange> filteredFogMisses =
                        fogMisses.stream()
                                .filter(blockRange -> (blockRange.getEnd().compareTo(finalMinBlockIndex) < 0))
                                .collect(Collectors.toSet());
                // fetch any missed TxOuts
                Set<OwnedTxOut> missedTxOuts = fetchFogMisses(filteredFogMisses, blockClient);
                recoveredTxOuts.addAll(missedTxOuts);
            }
        } catch (KexRngException exception) {
            throw new InvalidFogResponse("Invalid KexRng", exception);
        }
        // update the spent status of the TxOuts
        updateKeyImages(ledgerClient);

        if(Math.abs(ledgerBlockIndex.longValue() - viewBlockIndex.longValue()) >= FOG_SYNC_THRESHOLD.longValue()) {
            throw new FogSyncException(
                    String.format("Fog view and ledger block indices are out of sync. " +
                            "Try again later. View index: %s, Ledger index: %s",
                            viewBlockIndex, ledgerBlockIndex));
        }

        UnsignedLong currentBlockIndex = getCurrentBlockIndex();
        if(consensusBlockIndex.compareTo(currentBlockIndex) > 0) {
            if(consensusBlockIndex.sub(currentBlockIndex).compareTo(FOG_SYNC_THRESHOLD) >= 0) {
                throw new FogSyncException(
                        String.format("Fog has not finished syncing with Consensus. " +
                                        "Try again later (Block index %s / %s).",
                        currentBlockIndex, consensusBlockIndex));
            }
        }
    }

    /**
     * Update RNGs and TxOuts
     *
     * @return list of the skipped block ranges to scan manually
     */
    @NonNull
    synchronized Set<BlockRange> updateRNGsAndTxOuts(
            @NonNull AttestedViewClient viewClient,
            @NonNull FogQueryScalingStrategy scalingStrategy,
            @NonNull FogSeedProvider fogSeedProvider,
            @NonNull VersionedCryptoBox cryptoBox)
            throws InvalidFogResponse, NetworkException, AttestationException, KexRngException {
        Logger.i(TAG, "Updating owned TxOuts");

        HashSet<BlockRange> missedRanges = new HashSet<>();
        FogSearchKeyProvider searchKeyProvider = new FogSearchKeyProvider(this.seeds.values());
        long blockCount = 0L;
        int requests = 0;//TODO: this
        do {

            boolean allTXOsRetrieved;
            do {
                Map<ByteString, FogSeed> searchKeys = searchKeyProvider.getNSearchKeys(scalingStrategy.nextQuerySize());
                allTXOsRetrieved = searchKeys.size() <= 0;
                View.QueryResponse result = viewClient
                    .request(
                            searchKeys.keySet().stream().map(ByteString::toByteArray).collect(Collectors.toList()),
                            lastKnownFogViewEventId, viewBlockIndex.longValue()
                    );
                requests++;//TODO: this
                blockCount = result.getHighestProcessedBlockCount();
                lastKnownFogViewEventId = result.getNextStartFromUserEventId();
                for (DecommissionedIngestInvocation decommissionedIngestInvocation : result
                    .getDecommissionedIngestInvocationsList()) {
                  decommissionedIngestInvocationIds.add(decommissionedIngestInvocation.getIngestInvocationId());
                }
                for (FogCommon.BlockRange fogRange : result.getMissedBlockRangesList()) {
                    BlockRange range = new BlockRange(fogRange);
                    missedRanges.add(range);
                }
                int x = result.getRngsCount();// TODO: this
                Logger.d(TAG, String.format(Locale.US, "Received %d missed block ranges",
                        result.getMissedBlockRangesCount()));
                Logger.d(TAG, String.format(Locale.US, "Received %d RNGs", result.getRngsCount()));
                for (View.RngRecord rngRecord : result.getRngsList()) {
                    FogSeed existingSeed =
                            seeds.get(Arrays.hashCode(rngRecord.getPubkey().getPubkey().toByteArray()));
                    if (existingSeed == null) {
                        Logger.d(TAG, String.format(TAG, "Adding the RNG seed %s",
                                Hex.toString(rngRecord.getPubkey().getPubkey().toByteArray()))
                        );
                        FogSeed newSeed = fogSeedProvider.fogSeedFor(
                                accountKey.getDefaultSubAddressViewKey(),
                                rngRecord
                        );
                        seeds.put(
                                Arrays.hashCode(rngRecord.getPubkey().getPubkey().toByteArray()),
                                newSeed
                        );
                        // received a new seed
                        searchKeyProvider.addFogSeed(newSeed);
                        allTXOsRetrieved = false;// Set this to false because we need to check new seeds next iteration
                    } else {
                        Logger.d(TAG, String.format(TAG,
                                "The RNG seed %s is found in cache, updating the record",
                                Hex.toString(rngRecord.getPubkey().getPubkey().toByteArray()))
                        );
                        existingSeed.update(rngRecord);
                    }
                }
                for (View.TxOutSearchResult txResult : result.getTxOutSearchResultsList()) {
                    FogSeed seed = searchKeys.get(txResult.getSearchKey());
                    if(!searchKeyProvider.hasSeed(seed)) continue;
                    // Sanity check - Fog should be returning results from the expected search keys
                    /*if(null == seed || !Arrays.equals(//TODO: HERE! don't need this anymore
                            seed.getOutput(),
                            txResult.getSearchKey().toByteArray()
                    )){
                        throw new InvalidFogResponse("Received invalid reply from fog view - " +
                                "search key mismatch");
                    }*/
                    switch (txResult.getResultCode()) {
                        case View.TxOutSearchResultCode.Found_VALUE: {
                            // Decrypt the TxOut
                            try {
                                byte[] plainText = cryptoBox.versionedCryptoBoxDecrypt(
                                        accountKey.getDefaultSubAddressViewKey(),
                                        txResult.getCiphertext().toByteArray()
                                );
                                View.TxOutRecord record = View.TxOutRecord.parseFrom(plainText);
                                seed.addTXO(cryptoBox.ownedTxOutFor(
                                        record,
                                        accountKey
                                ));
                                Logger.d(TAG, "Found TxOut in block with index " +
                                        record.getBlockIndex()
                                );
                            } catch (InvalidProtocolBufferException exception) {
                                Logger.w(TAG, "Unable to process TxOutRecord", exception);
                                throw new InvalidFogResponse("Unable to process TxOutRecord");
                            }
                        }
                        break;
                        case View.TxOutSearchResultCode.BadSearchKey_VALUE: {
                            throw new InvalidFogResponse(
                                    "Received invalid reply from fog view - bad search key");
                        }
                        case View.TxOutSearchResultCode.InternalError_VALUE: {
                            throw new InvalidFogResponse(
                                    "Received invalid reply from fog view - Internal Error");
                        }
                        case View.TxOutSearchResultCode.NotFound_VALUE: {
                            //allTXOsRetrieved = true;//TODO: HERE!
                            if (isSeedDecommissioned(seed)) {
                                seed.markObsolete();
                            }
                            searchKeyProvider.removeSeed(seed);
                            break;
                        }
                    }
                    //if (allTXOsRetrieved) break;//TODO: break once all seeds are complete//TODO: HERE!
                }
            } while (!allTXOsRetrieved);//TODO: break logic
            viewBlockIndex = (blockCount != 0)
                    ? UnsignedLong.fromLongBits(blockCount).sub(UnsignedLong.ONE)
                    : UnsignedLong.ZERO;
            Logger.i(TAG, "View Request completed blockIndex = " + viewBlockIndex);
        } while (searchKeyProvider.hasKeys());
        Logger.e("TAG", "HERE! " + requests);
        return missedRanges;
    }

    private boolean isSeedDecommissioned(FogSeed seed) {
      return decommissionedIngestInvocationIds.contains(seed.getIngestInvocationId());
    }

    void updateTxOutsSpentState(Ledger.CheckKeyImagesResponse keyImagesResponse) throws InvalidFogResponse {
        for (Ledger.KeyImageResult result : keyImagesResponse.getResultsList()) {
            if (result.getKeyImageResultCode() == Ledger.KeyImageResultCode.NotSpent_VALUE) {
                continue;
            }

            OwnedTxOut utxo = getUtxoByKeyImage(result.getKeyImage().getData().toByteArray());
            if (utxo == null) {
                throw new InvalidFogResponse("checkKeyImages returned invalid key image result");
            }
            Date spentBlockTimestamp = null;
            long longTimestampSeconds = result.getTimestamp();
            UnsignedLong timestampSeconds = UnsignedLong.fromLongBits(longTimestampSeconds);
            // when the timestamp is missing U64::MAX is returned
            if (!timestampSeconds.equals(UnsignedLong.MAX_VALUE)) {
                long longTimestampMillis = TimeUnit.SECONDS.toMillis(timestampSeconds.longValue());
                spentBlockTimestamp = new Date(longTimestampMillis);
            }

            utxo.setSpent(
                    UnsignedLong.fromLongBits(result.getSpentAt()),
                    spentBlockTimestamp
            );
            Logger.d(TAG, String.format(Locale.US,
                    "TxOut has been marked spent in block %s",
                    Objects.requireNonNull(utxo.getSpentBlockIndex()).toString())
            );
        }
        synchronized (this) {
            ledgerTotalTxCount = UnsignedLong.fromLongBits(keyImagesResponse.getGlobalTxoCount());
            ledgerBlockIndex = UnsignedLong.fromLongBits(keyImagesResponse.getNumBlocks())
                    .sub(UnsignedLong.ONE);
        }
    }

    void updateKeyImages(@NonNull AttestedLedgerClient ledgerClient)
            throws InvalidFogResponse, NetworkException, AttestationException {
        Logger.i(TAG, "Checking unspent TXOs key images");
        Set<OwnedTxOut> txOuts = getUnspentTxOuts();
        Ledger.CheckKeyImagesResponse response = ledgerClient.checkUtxoKeyImages(txOuts);
        updateTxOutsSpentState(response);
    }

    /**
     * See if there are any blocks not covered by our list of seeds. These are blocks we'll have to
     * get manually and do view-key scanning against.
     */
    @NonNull
    synchronized Set<OwnedTxOut> fetchFogMisses(@NonNull Set<BlockRange> missedRanges,
                                                @NonNull FogBlockClient blockClient)
            throws NetworkException {
        HashSet<OwnedTxOut> recovered = new HashSet<>();
        for (BlockRange missedRange : missedRanges) {
            List<OwnedTxOut> txos = blockClient.scanForTxOutsInBlockRange(missedRange,
                    accountKey);
            recovered.addAll(txos);
        }
        return recovered;
    }

    @Nullable
    OwnedTxOut getUtxoByKeyImage(@NonNull byte[] keyImage) {
        int keyImageHashCode = Arrays.hashCode(keyImage);
        Set<OwnedTxOut> syncedTXOs = getSyncedTxOuts();
        for (OwnedTxOut utxo : syncedTXOs) {
            if (utxo.getKeyImageHashCode() == keyImageHashCode) {
                return utxo;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TxOutStore that = (TxOutStore) o;

        return lastKnownFogViewEventId == that.lastKnownFogViewEventId &&
            Objects.equals(ledgerBlockIndex, that.ledgerBlockIndex) &&
            Objects.equals(viewBlockIndex, that.viewBlockIndex) &&
            Objects.equals(consensusBlockIndex, that.consensusBlockIndex) &&
            Objects.equals(ledgerTotalTxCount, that.ledgerTotalTxCount) &&
            Objects.equals(seeds, that.seeds) &&
            Objects
                .equals(decommissionedIngestInvocationIds, that.decommissionedIngestInvocationIds) &&
            Arrays.equals(recoveredTxOuts.toArray(), that.recoveredTxOuts.toArray()) &&
            Objects.equals(accountKey, that.accountKey);
    }

    /**
     * @return The flags needed to write and read this object to or from a parcel
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes this object to the provided parcel
     * @param parcel The parcel to write the object to
     * @param flags The flags describing the contents of this object
     */
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(seeds.size());
        for(Map.Entry<Integer, FogSeed> entry : this.seeds.entrySet()) {
            parcel.writeInt(entry.getKey());
            parcel.writeParcelable(entry.getValue(), flags);
        }
        parcel.writeInt(decommissionedIngestInvocationIds.size());
        for(Long id : decommissionedIngestInvocationIds) {
            parcel.writeLong(id);
        }
        parcel.writeParcelable(ledgerBlockIndex, flags);
        parcel.writeParcelable(viewBlockIndex, flags);
        parcel.writeParcelable(consensusBlockIndex, flags);
        parcel.writeLong(lastKnownFogViewEventId);
        parcel.writeParcelable(ledgerTotalTxCount, flags);
        parcel.writeInt(recoveredTxOuts.size());
        for(OwnedTxOut otxo : recoveredTxOuts) {
            parcel.writeParcelable(otxo, flags);
        }
    }

    public static final Creator<TxOutStore> CREATOR = new Creator<TxOutStore>() {
        /**
         * Create TxOutStore from the provided Parcel
         * @param parcel The parcel containing a TxOutStore
         * @return The TxOutStore contained in the provided Parcel
         */
        @Override
        public TxOutStore createFromParcel(Parcel parcel) {
            return new TxOutStore(parcel);
        }

        /**
         * Used by Creator to deserialize an array of TxOutStores
         */
        @Override
        public TxOutStore[] newArray(int length) {
            return new TxOutStore[length];
        }
    };

    /**
     * Creates a TxOutStore from the provided parcel
     * @param parcel The parcel that contains a TxOutStore
     */
    private TxOutStore(Parcel parcel) {
        seeds = new HashMap<Integer, FogSeed>();
        int seedSize = parcel.readInt();
        for(int i = 0; i < seedSize; i++) {
            Integer key = parcel.readInt();
            FogSeed value = parcel.readParcelable(FogSeed.class.getClassLoader());
            seeds.put(key, value);
        }
        int decommIdSize = parcel.readInt();
        decommissionedIngestInvocationIds = new HashSet<Long>();
        for(int i = 0; i < decommIdSize; i++) {
            decommissionedIngestInvocationIds.add(parcel.readLong());
        }
        ledgerBlockIndex = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        viewBlockIndex = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        consensusBlockIndex = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        lastKnownFogViewEventId = parcel.readLong();
        ledgerTotalTxCount = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        int otxoSize = parcel.readInt();
        recoveredTxOuts = new ConcurrentLinkedQueue<OwnedTxOut>();
        for(int i = 0; i < otxoSize; i++) {
            recoveredTxOuts.add(parcel.readParcelable(OwnedTxOut.class.getClassLoader()));
        }
    }

    public UnsignedLong getViewBlockIndex() {
        return this.viewBlockIndex;
    }

    public UnsignedLong getLedgerBlockIndex() {
        return this.ledgerBlockIndex;
    }

    public UnsignedLong getConsensusBlockIndex() {
        return this.consensusBlockIndex;
    }

    void setViewBlockIndex(UnsignedLong viewBlockIndex) {
        this.viewBlockIndex = viewBlockIndex;
    }

    void setLedgerBlockIndex(UnsignedLong ledgerBlockIndex) {
        this.ledgerBlockIndex = ledgerBlockIndex;
    }

    void setConsensusBlockIndex(UnsignedLong consensusBlockIndex) {
        this.consensusBlockIndex = consensusBlockIndex;
    }

}
