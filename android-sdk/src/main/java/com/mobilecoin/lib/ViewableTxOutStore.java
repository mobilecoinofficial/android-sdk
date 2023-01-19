package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FogSyncException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.KexRngException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.util.Hex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import fog_common.FogCommon;
import fog_view.View;

class ViewableTxOutStore {
    private static final String TAG = ViewableTxOutStore.class.getName();

    protected static final UnsignedLong FOG_SYNC_THRESHOLD = UnsignedLong.TEN;

    // A map of nonce -> Seed.
    private HashMap<Integer, ViewFogSeed> seeds;

    private Set<Long> decommissionedIngestInvocationIds;
    // AccountKey.
    private ViewAccountKey viewAccountKey;
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
    private ConcurrentLinkedQueue<ViewableTxOut> recoveredTxOuts;

    ViewableTxOutStore(@NonNull final ViewAccountKey accountKey) {
        this.seeds = new HashMap<>();
        this.decommissionedIngestInvocationIds = new HashSet<>();
        this.viewAccountKey = accountKey;
        this.ledgerBlockIndex = UnsignedLong.ZERO;
        this.viewBlockIndex = UnsignedLong.ZERO;
        this.consensusBlockIndex = UnsignedLong.ZERO;
        this.recoveredTxOuts = new ConcurrentLinkedQueue<>();
    }

    static String createStorageKey(AccountKey accountKey) {
        return accountKey.hashCode() + "-" + TAG;
    }

    /**
     * Gets the list of synchronized TXOs we are aware of. A "synchronized TXO" is a TXO that we
     * were able to check key images for.
     */
    @NonNull
    synchronized Set<ViewableTxOut> getSyncedTxOuts() {
        // it's possible to have overlapping TxOuts in different Seeds
        // HashSet will leave the set unchanged if the element exists
        HashSet<ViewableTxOut> syncedTxOuts = new HashSet<>();
        for (ViewFogSeed seed : seeds.values()) {
            for (ViewableTxOut txOut : seed.getTxOuts()) {
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
    Set<ViewableTxOut> getUnspentTxOuts() {
        return getSyncedTxOuts().stream().filter(p -> !p.isSpent(getCurrentBlockIndex()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    void setViewAccountKey(@NonNull final ViewAccountKey viewAccountKey) {
        this.viewAccountKey = viewAccountKey;
    }

    @NonNull
    synchronized UnsignedLong getCurrentBlockIndex() {
        /*return ledgerBlockIndex.compareTo(viewBlockIndex) < 0
                ? ledgerBlockIndex
                : viewBlockIndex;*/
        return viewBlockIndex;
    }

    @NonNull
    synchronized UnsignedLong getLedgerTotalTxCount() {
        return ledgerTotalTxCount;
    }

    void refresh(
            @NonNull AttestedViewClient viewClient,
            @NonNull FogBlockClient blockClient
    ) throws InvalidFogResponse, NetworkException, AttestationException, FogSyncException {
        // update RNGs, TxOuts, and fog misses
        Set<BlockRange> fogMisses;
        try {
            fogMisses = updateRNGsAndTxOuts(viewClient, new DefaultFogQueryScalingStrategy(),
                    new DefaultFogSeedProvider(), new DefaultVersionedCryptoBox());
            // Find the first RNG
            Optional<ViewFogSeed> firstRngSeed = seeds.values().stream()
                    .min((o1, o2) -> o1.getStartBlock().compareTo(o2.getStartBlock()));

            // Skip all the blocks that come before the first RNG
            if (firstRngSeed.isPresent()) {
                final UnsignedLong finalMinBlockIndex = firstRngSeed.get().getStartBlock();
                Set<BlockRange> filteredFogMisses =
                        fogMisses.stream()
                                .filter(blockRange -> (blockRange.getEnd().compareTo(finalMinBlockIndex) < 0))
                                .collect(Collectors.toSet());
                // fetch any missed TxOuts
                Set<ViewableTxOut> missedTxOuts = fetchFogMisses(filteredFogMisses, blockClient);
                recoveredTxOuts.addAll(missedTxOuts);
            }
        } catch (KexRngException exception) {
            throw new InvalidFogResponse("Invalid KexRng", exception);
        }

        /*if(Math.abs(ledgerBlockIndex.longValue() - viewBlockIndex.longValue()) >= FOG_SYNC_THRESHOLD.longValue()) {
            throw new FogSyncException(
                    String.format("Fog view and ledger block indices are out of sync. " +
                                    "Try again later. View index: %s, Ledger index: %s",
                            viewBlockIndex, ledgerBlockIndex));
        }*/

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
        ViewFogSearchKeyProvider searchKeyProvider = new ViewFogSearchKeyProvider(this.seeds.values());
        long blockCount = 0L;
        do {
            Map<ByteString, ViewFogSeed> searchKeys = searchKeyProvider.getNSearchKeys(scalingStrategy.nextQuerySize());
            View.QueryResponse result = viewClient
                    .request(
                            searchKeys.keySet().stream().map(ByteString::toByteArray).collect(Collectors.toList()),
                            lastKnownFogViewEventId, viewBlockIndex.longValue()
                    );
            blockCount = result.getHighestProcessedBlockCount();
            lastKnownFogViewEventId = result.getNextStartFromUserEventId();
            for (View.DecommissionedIngestInvocation decommissionedIngestInvocation : result
                    .getDecommissionedIngestInvocationsList()) {
                decommissionedIngestInvocationIds.add(decommissionedIngestInvocation.getIngestInvocationId());
            }
            for (FogCommon.BlockRange fogRange : result.getMissedBlockRangesList()) {
                BlockRange range = new BlockRange(fogRange);
                missedRanges.add(range);
            }
            Logger.d(TAG, String.format(Locale.US, "Received %d missed block ranges",
                    result.getMissedBlockRangesCount()));
            Logger.d(TAG, String.format(Locale.US, "Received %d RNGs", result.getRngsCount()));
            for (View.RngRecord rngRecord : result.getRngsList()) {
                ViewFogSeed existingSeed =
                        seeds.get(Arrays.hashCode(rngRecord.getPubkey().getPubkey().toByteArray()));
                if (existingSeed == null) {
                    Logger.d(TAG, String.format(TAG, "Adding the RNG seed %s",
                            Hex.toString(rngRecord.getPubkey().getPubkey().toByteArray()))
                    );
                    ViewFogSeed newSeed = fogSeedProvider.viewFogSeedFor(
                            viewAccountKey.getDefaultSubaddressViewKey(),
                            rngRecord
                    );
                    seeds.put(
                            Arrays.hashCode(rngRecord.getPubkey().getPubkey().toByteArray()),
                            newSeed
                    );
                    // received a new seed
                    searchKeyProvider.addFogSeed(newSeed);
                } else {
                    Logger.d(TAG, String.format(TAG,
                            "The RNG seed %s is found in cache, updating the record",
                            Hex.toString(rngRecord.getPubkey().getPubkey().toByteArray()))
                    );
                    existingSeed.update(rngRecord);
                }
            }
            for (View.TxOutSearchResult txResult : result.getTxOutSearchResultsList()) {
                ViewFogSeed seed = searchKeys.get(txResult.getSearchKey());
                switch (txResult.getResultCode()) {
                    case View.TxOutSearchResultCode.Found_VALUE: {
                        // Decrypt the TxOut
                        try {
                            byte[] plainText = cryptoBox.versionedCryptoBoxDecrypt(
                                    viewAccountKey.getDefaultSubaddressViewKey(),
                                    txResult.getCiphertext().toByteArray()
                            );
                            View.TxOutRecord record = View.TxOutRecord.parseFrom(plainText);
                            seed.addTXO(cryptoBox.viewableTxOutFor(
                                    record,
                                    viewAccountKey
                            ));
                            searchKeyProvider.resetSeed(seed);
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
                        if (isSeedDecommissioned(seed)) {
                            seed.markObsolete();
                        }
                        searchKeyProvider.markSeedComplete(seed);
                        break;
                    }
                }
            }
        } while (searchKeyProvider.hasKeys());
        viewBlockIndex = (blockCount != 0)
                ? UnsignedLong.fromLongBits(blockCount).sub(UnsignedLong.ONE)
                : UnsignedLong.ZERO;
        Logger.i(TAG, "View Request completed blockIndex = " + viewBlockIndex);
        return missedRanges;
    }

    private boolean isSeedDecommissioned(ViewFogSeed seed) {
        return decommissionedIngestInvocationIds.contains(seed.getIngestInvocationId());
    }

    /**
     * See if there are any blocks not covered by our list of seeds. These are blocks we'll have to
     * get manually and do view-key scanning against.
     */
    @NonNull
    synchronized Set<ViewableTxOut> fetchFogMisses(@NonNull Set<BlockRange> missedRanges,
                                                @NonNull FogBlockClient blockClient)
            throws NetworkException {
        HashSet<ViewableTxOut> recovered = new HashSet<>();
        for (BlockRange missedRange : missedRanges) {
            List<ViewableTxOut> txos = blockClient.viewScanForTxOutsInBlockRange(missedRange, viewAccountKey);
            recovered.addAll(txos);
        }
        return recovered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ViewableTxOutStore that = (ViewableTxOutStore) o;

        return lastKnownFogViewEventId == that.lastKnownFogViewEventId &&
                Objects.equals(ledgerBlockIndex, that.ledgerBlockIndex) &&
                Objects.equals(viewBlockIndex, that.viewBlockIndex) &&
                Objects.equals(consensusBlockIndex, that.consensusBlockIndex) &&
                Objects.equals(ledgerTotalTxCount, that.ledgerTotalTxCount) &&
                Objects.equals(seeds, that.seeds) &&
                Objects.equals(decommissionedIngestInvocationIds, that.decommissionedIngestInvocationIds) &&
                Arrays.equals(recoveredTxOuts.toArray(), that.recoveredTxOuts.toArray()) &&
                Objects.equals(viewAccountKey, that.viewAccountKey);
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
