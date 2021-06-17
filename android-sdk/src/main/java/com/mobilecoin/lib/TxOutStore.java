// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.KexRngException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.util.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fog_common.FogCommon;
import fog_ledger.Ledger;
import fog_view.View;

final class TxOutStore implements Serializable {
    private static final String TAG = TxOutStore.class.getName();

    // Bump serial version and read/write code if fields change
    private static final long serialVersionUID = 2L;

    // A map of nonce -> Seed.
    private HashMap<Integer, FogSeed> seeds;
    // AccountKey.
    private AccountKey accountKey;
    // Block index reported from ledger server.
    private UnsignedLong ledgerBlockIndex;
    // Block index reported from view server.
    private UnsignedLong viewBlockIndex;
    private UnsignedLong ledgerTotalTxCount;

    // TxOuts recovered from missed blocks
    private ConcurrentLinkedQueue<OwnedTxOut> recoveredTxOuts;

    TxOutStore(@NonNull AccountKey accountKey) {
        this.seeds = new HashMap<>();
        this.accountKey = accountKey;
        this.ledgerBlockIndex = UnsignedLong.ZERO;
        this.viewBlockIndex = UnsignedLong.ZERO;
        this.recoveredTxOuts = new ConcurrentLinkedQueue<>();
    }

    @NonNull
    static TxOutStore fromBytes(
            @NonNull byte[] serialized,
            @NonNull AccountKey accountKey
    ) throws SerializationException {
        Logger.i(TAG, "Deserializing the txo store from bytes");
        try (ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
             ObjectInputStream is = new ObjectInputStream(bis)) {
            TxOutStore store = (TxOutStore) is.readObject();
            store.setAccountKey(accountKey);
            return store;
        } catch (IOException | ClassNotFoundException exception) {
            Logger.w(TAG, "Unable to deserialize the txo store", exception);
            throw new SerializationException();
        }
    }

    @NonNull
    byte[] toByteArray() throws SerializationException {
        Logger.i(TAG, "Serializing txo store");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(this);
            return bos.toByteArray();
        } catch (IOException exception) {
            Logger.w(TAG, "Unable to serialize the txo store", exception);
            throw new SerializationException();
        }
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
    ) throws InvalidFogResponse, NetworkException, AttestationException {
        // update RNGs, TxOuts, and fog misses
        Set<BlockRange> fogMisses;
        try {
            fogMisses = updateRNGsAndTxOuts(viewClient, new DefaultFogQueryScalingStrategy());
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
    }

    /**
     * Update RNGs and TxOuts
     *
     * @return list of the skipped block ranges to scan manually
     */
    @NonNull
    synchronized Set<BlockRange> updateRNGsAndTxOuts(
            @NonNull AttestedViewClient viewClient,
            @NonNull FogQueryScalingStrategy scalingStrategy)
            throws InvalidFogResponse, NetworkException, AttestationException, KexRngException {
        Logger.i(TAG, "Updating owned TxOuts");

        Stack<FogSeed> pendingSeeds = new Stack<>();
        HashSet<BlockRange> missedRanges = new HashSet<>();
        pendingSeeds.addAll(seeds.values());
        do {
            FogSeed seed = null;
            if (pendingSeeds.size() > 0) {
                seed = pendingSeeds.pop();
            }

            boolean allTXOsRetrieved = false;
            do {
                List<byte[]> searchKeys = null;
                if (seed != null) {
                    searchKeys = Arrays.asList(seed.getNextN(scalingStrategy.nextQuerySize()));
                } else {
                    allTXOsRetrieved = true;
                }
                View.QueryResponse result = viewClient.request(searchKeys);
                for (FogCommon.BlockRange fogRange : result.getMissedBlockRangesList()) {
                    BlockRange range = new BlockRange(fogRange);
                    missedRanges.add(range);
                }
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
                        FogSeed newSeed = new FogSeed(
                                accountKey.getSubAddressViewKey(),
                                rngRecord
                        );
                        seeds.put(
                                Arrays.hashCode(rngRecord.getPubkey().getPubkey().toByteArray()),
                                newSeed
                        );
                        // received a new seed
                        pendingSeeds.add(newSeed);
                    } else {
                        Logger.d(TAG, String.format(TAG,
                                "The RNG seed %s is found in cache, updating the record",
                                Hex.toString(rngRecord.getPubkey().getPubkey().toByteArray()))
                        );
                        existingSeed.update(rngRecord);
                    }
                }
                for (View.TxOutSearchResult txResult : result.getTxOutSearchResultsList()) {
                    // Sanity check - fog should be returning results in the order we expect.
                    if (null == seed || !Arrays.equals(
                            seed.getOutput(),
                            txResult.getSearchKey().toByteArray()
                    )) {
                        throw new InvalidFogResponse("Received invalid reply from fog view - " +
                                "search key order mismatch");
                    }
                    switch (txResult.getResultCode()) {
                        case View.TxOutSearchResultCode.Found_VALUE: {
                            // Decrypt the TxOut
                            try {
                                byte[] plainText = Util.versionedCryptoBoxDecrypt(
                                        accountKey.getSubAddressViewKey(),
                                        txResult.getCiphertext().toByteArray()
                                );
                                View.TxOutRecord record = View.TxOutRecord.parseFrom(plainText);
                                // Advance RNG.
                                seed.addTXO(new OwnedTxOut(
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
                                    "Received invalid reply from fog view - " + "bad search key");
                        }
                        case View.TxOutSearchResultCode.InternalError_VALUE: {
                            throw new InvalidFogResponse(
                                    "Received invalid reply from fog view - " + "Internal Error");
                        }
                        case View.TxOutSearchResultCode.NotFound_VALUE: {
                            allTXOsRetrieved = true;
                            long blockCount = result.getHighestProcessedBlockCount();
                            viewBlockIndex = (blockCount != 0)
                                    ? UnsignedLong.fromLongBits(blockCount).sub(UnsignedLong.ONE)
                                    : UnsignedLong.ZERO;
                            Logger.i(TAG, "View Request completed blockIndex = " + viewBlockIndex);
                            break;
                        }
                    }
                    if (allTXOsRetrieved) break;
                }
            } while (!allTXOsRetrieved);
        } while (pendingSeeds.size() > 0);
        return missedRanges;
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

    private synchronized void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(ledgerBlockIndex);
        out.writeObject(viewBlockIndex);
        out.writeObject(ledgerTotalTxCount);
        out.writeObject(seeds);
        out.writeObject(recoveredTxOuts);
    }

    @SuppressWarnings("unchecked")
    private synchronized void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        ledgerBlockIndex = (UnsignedLong) in.readObject();
        viewBlockIndex = (UnsignedLong) in.readObject();
        ledgerTotalTxCount = (UnsignedLong) in.readObject();
        seeds = (HashMap<Integer, FogSeed>) in.readObject();
        recoveredTxOuts = new ConcurrentLinkedQueue<>();
    }
}
