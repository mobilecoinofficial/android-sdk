// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;


import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.uri.FogUri;
import com.mobilecoin.lib.util.NetworkingCall;

import java.util.Set;

import fog_ledger.FogUntrustedTxOutApiGrpc;
import fog_ledger.Ledger;
import io.grpc.StatusRuntimeException;

/**
 * Attested client for a ledger service Attestation is done automatically by the parent class {@link
 * AttestedClient}
 */
class FogUntrustedClient extends AnyClient {
    private static final String TAG = AttestedLedgerClient.class.getName();

    /**
     * Creates and initializes an instance of {@link FogUntrustedClient}
     *
     * @param uri           an address of the service. Example:
     *                      fog://fog.test.mobilecoin.com
     * @param serviceConfig service configuration passed to MobileCoinClient
     */
    FogUntrustedClient(@NonNull FogUri uri, @NonNull ClientConfig.Service serviceConfig) {
        super(uri.getUri(), serviceConfig);
        Logger.i(TAG, "Created new FogUntrustedClient", null,
                "uri:", uri,
                "verifier:", serviceConfig);
    }

    /**
     * Fetch TxOuts by their public keys
     */
    @NonNull
    Ledger.TxOutResponse fetchTxOuts(@NonNull Set<RistrettoPublic> publicKeys) throws NetworkException,
            AttestationException {
        Logger.i(TAG, "Fetching TxOuts via untrusted fog API", null,
                "public keys number:", publicKeys.size());
        FogUntrustedTxOutApiGrpc.FogUntrustedTxOutApiBlockingStub fogClient =
                getAPIManager().getFogUntrustedTxOutApiBlockingStub(getManagedChannel());
        Ledger.TxOutRequest.Builder requestBuilder = Ledger.TxOutRequest.newBuilder();
        for (RistrettoPublic publicKey : publicKeys) {
            requestBuilder.addTxOutPubkeys(publicKey.toProtoBufObject());
        }
        NetworkingCall<Ledger.TxOutResponse> networkingCall =
                new NetworkingCall<>(() -> {
                    try {
                        return fogClient.getTxOuts(requestBuilder.build());
                    } catch (StatusRuntimeException exception) {
                        Logger.w(TAG, "Unable to fetch TxOuts from the untrusted service",
                                exception);
                        throw new NetworkException(exception);
                    }
                });
        try {
            return networkingCall.run();
        } catch (AttestationException | NetworkException | RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("BUG: unreachable code");
        }
    }
}
