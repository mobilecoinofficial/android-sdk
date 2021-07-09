// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;


import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.services.FogUntrustedService;
import com.mobilecoin.lib.network.services.transport.Transport;
import com.mobilecoin.lib.network.uri.FogUri;
import com.mobilecoin.lib.util.NetworkingCall;

import java.util.Set;

import fog_ledger.Ledger;
import io.grpc.StatusRuntimeException;

/**
 * Attested client for a ledger service Attestation is done automatically by the parent class {@link
 * AttestedClient}
 */
final class FogUntrustedClient extends AnyClient<FogUri> {
    private static final String TAG = AttestedLedgerClient.class.getName();

    /**
     * Creates and initializes an instance of {@link FogUntrustedClient}
     *
     * @param uri           an address of the service. Example:
     *                      fog://fog.test.mobilecoin.com
     * @param serviceConfig service configuration passed to MobileCoinClient
     */
    FogUntrustedClient(@NonNull FogUri uri, @NonNull ClientConfig.Service serviceConfig) {
        super(uri, serviceConfig);
        Logger.i(TAG, "Created new FogUntrustedClient", null,
                "uri:", uri,
                "verifier:", serviceConfig);
    }

    /**
     * Fetch TxOuts by their public keys
     */
    @NonNull
    Ledger.TxOutResponse fetchTxOuts(@NonNull Set<RistrettoPublic> publicKeys) throws NetworkException {
        Logger.i(TAG, "Fetching TxOuts via untrusted fog API", null,
                "public keys number:", publicKeys.size());
        Transport transport;
        try {
            transport = getNetworkTransport();
        } catch (AttestationException exception) {
            throw new IllegalStateException("BUG: Untrusted service cannot throw attestation " +
                    "exception");
        }
        FogUntrustedService fogService =
                getAPIManager().getFogUntrustedService(transport);
        Ledger.TxOutRequest.Builder requestBuilder = Ledger.TxOutRequest.newBuilder();
        for (RistrettoPublic publicKey : publicKeys) {
            requestBuilder.addTxOutPubkeys(publicKey.toProtoBufObject());
        }
        NetworkingCall<Ledger.TxOutResponse> networkingCall =
                new NetworkingCall<>(() -> {
                    try {
                        return fogService.getTxOuts(requestBuilder.build());
                    } catch (StatusRuntimeException exception) {
                        Logger.w(TAG, "Unable to fetch TxOuts from the untrusted service",
                                exception);
                        throw new NetworkException(exception);
                    }
                });
        try {
            return networkingCall.run();
        } catch (NetworkException | RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("BUG: unreachable code");
        }
    }
}
