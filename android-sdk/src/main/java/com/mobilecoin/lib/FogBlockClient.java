// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;


import androidx.annotation.NonNull;

import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.services.FogBlockService;
import com.mobilecoin.lib.network.uri.FogUri;
import com.mobilecoin.lib.util.NetworkingCall;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fog_ledger.Ledger;
import fog_view.View;
import io.grpc.StatusRuntimeException;

/**
 * Attested client for a ledger service Attestation is done automatically by the parent class {@link
 * AttestedClient}
 */
final class FogBlockClient extends AnyClient {
    private static final String TAG = FogBlockClient.class.getName();

    /**
     * Creates and initializes an instance of {@link FogBlockClient}
     *
     * @param uri           an address of the service. Example:
     *                      fog://fog.test.mobilecoin.com
     * @param serviceConfig service configuration passed to MobileCoinClient
     */
    FogBlockClient(@NonNull FogUri uri, @NonNull ClientConfig.Service serviceConfig) {
        super(uri.getUri(), serviceConfig);
        Logger.i(TAG, "Created new FogBlockClient", null,
                "uri:", uri,
                "verifier:", serviceConfig);
    }

    /**
     * Scan for OwnedTxOuts
     * @param range block range to scan
     * @param accountKey for TxOuts decoding
     */
    @NonNull
    public List<OwnedTxOut> scanForTxOutsInBlockRange(
            @NonNull BlockRange range, @NonNull AccountKey accountKey
    ) throws NetworkException {
        Logger.i(TAG, "Scanning the ledger for TxOuts");
        ArrayList<OwnedTxOut> txos = new ArrayList<>();
        List<View.TxOutRecord> records = fetchTxRecordsInBlockRange(range);
        Logger.d(TAG,
                "Received TxRecords response", null,
                "count:", records.size(),
                "range:", range);
        for (View.TxOutRecord record : records) {
            try {
                OwnedTxOut txo = new OwnedTxOut(record, accountKey);
                txos.add(txo);
                Logger.d(TAG, "Found TxOut", null,
                        "block:", record.getBlockIndex());
            } catch (Exception ignored) { /* */ }
        }
        Logger.d(TAG, String.format(Locale.US,
                "Found total %d TxOuts",
                txos.size())
        );
        return txos;
    }

    /**
     * Fetch TxOutRecords from the block range
     */
    @NonNull
    public List<View.TxOutRecord> fetchTxRecordsInBlockRange(@NonNull BlockRange range)
            throws NetworkException {
        Logger.i(TAG, "Fetching TxOuts via Block API", null,
                "range:", range);
        NetworkingCall<Ledger.BlockResponse> networkingCall;
        try {
            FogBlockService fogBlockService =
                    getAPIManager().getFogBlockService(getNetworkTransport());
            Ledger.BlockRequest request = Ledger.BlockRequest.newBuilder()
                    .addRanges(range.toProtoBuf())
                    .build();
            networkingCall =
                    new NetworkingCall<>(() -> {
                        try {
                            return fogBlockService.getBlocks(request);
                        } catch (StatusRuntimeException exception) {
                            Logger.w(TAG, "Unable to post transaction with consensus", exception);
                            throw new NetworkException(exception);
                        }
                    });
        } catch (AttestationException exception) {
            throw new IllegalStateException("BUG", exception);
        }
        Ledger.BlockResponse response;
        try {
            response = networkingCall.run();
        } catch (NetworkException | RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("BUG: unreachable code");
        }
        List<Ledger.BlockData> blocks = response.getBlocksList();
        ArrayList<View.TxOutRecord> records = new ArrayList<>();
        for (Ledger.BlockData block : blocks) {
            long globalIndexStart = block.getGlobalTxoCount() - block.getOutputsCount();
            List<MobileCoinAPI.TxOut> outputs = block.getOutputsList();
            for (MobileCoinAPI.TxOut txOut : outputs) {
                View.TxOutRecord record = View.TxOutRecord.newBuilder()
                        .setBlockIndex(block.getIndex())
                        .setTimestamp(block.getTimestamp())
                        .setTxOutGlobalIndex(globalIndexStart + block.getOutputsList().indexOf(txOut))
                        .setTxOutAmountCommitmentData(txOut.getAmount().getCommitment().getData())
                        .setTxOutAmountMaskedValue(txOut.getAmount().getMaskedValue())
                        .setTxOutPublicKeyData(txOut.getPublicKey().getData())
                        .setTxOutTargetKeyData(txOut.getTargetKey().getData())
                        .build();
                records.add(record);
                Logger.d(TAG, "Found TxOut", null,
                        "block index:", record.getBlockIndex());
            }
        }
        return records;
    }
}
