// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.ClientConfig.Service;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.FogViewService;
import com.mobilecoin.lib.network.services.transport.Transport;
import com.mobilecoin.lib.util.NetworkingCall;

import java.util.List;

import attest.Attest;
import fog_view.View;

/**
 * Attested client for a Fog View service Attestation is done automatically by the parent class
 * {@link AttestedClient}
 */
class AttestedViewClient extends AttestedClient {
    private static final String TAG = AttestedViewClient.class.getName();

    /**
     * Creates and initializes an instance of {@link AttestedViewClient}
     *  @param loadBalancer           an address of the service
     * @param serviceConfig service configuration passed to MobileCoinClient
     */
    AttestedViewClient(@NonNull LoadBalancer loadBalancer,
                       @NonNull Service serviceConfig,
                       @NonNull TransportProtocol transportProtocol) {
        super(loadBalancer, serviceConfig, transportProtocol);
        Logger.i(TAG, "Created new AttestedViewClient", null,
                "loadBalancer:", loadBalancer,
                "verifier:", serviceConfig);
    }

    /**
     * Attest a managed connection
     * <p>
     * <pre>
     * The process consists from 3 parts:
     * 1) {@code attestStart} generates the authorization request
     * 2) send authorization request to the service
     * 3) {@code attestFinish} uses service response to derive the key for encrypted communication
     * in the subsequent messages
     * </pre>
     * <p>
     * If the attestation failed, the invalid intermediate state can be reset with {@link
     * AttestedClient#attestReset}
     *
     * @param transport a channel that requires attestation
     */
    @Override
    public synchronized void attest(@NonNull Transport transport)
            throws AttestationException, NetworkException {
        try {
            Logger.i(TAG, "Attest view connection");
            byte[] requestBytes = attestStart(getCurrentServiceUri());
            FogViewService fogViewService = getAPIManager().getFogViewService(getNetworkTransport());
            ByteString bytes = ByteString.copyFrom(requestBytes);
            Attest.AuthMessage authMessage = Attest.AuthMessage.newBuilder().setData(bytes).build();
            Attest.AuthMessage response = fogViewService.auth(authMessage);
            attestFinish(response.getData().toByteArray(), getServiceConfig().getVerifier());
        } catch (NetworkException exception) {
            attestReset();
            if (exception.getResult().getResultCode() == NetworkResult.ResultCode.INTERNAL) {
                AttestationException attestationException =
                        new AttestationException(exception.getResult().getDescription(), exception);
                Util.logException(TAG, attestationException);
                throw attestationException;
            }
            Logger.w(TAG, "Failed to attest the fog view connection", exception);
            throw exception;
        } catch (Exception exception) {
            attestReset();
            AttestationException attestationException =
                    new AttestationException("Failed to attest the fog view connection", exception);
            Util.logException(TAG, attestationException);
            throw attestationException;
        }
    }

    /**
     * Fog enclave request to query RNG seeds and TxOuts
     *
     * @param getTxosKexRngOutputs (optional) list of search keys to query
     */
    @NonNull
    synchronized View.QueryResponse request(
            @Nullable List<byte[]> getTxosKexRngOutputs, long lastKnownEventId, long lastKnownBlockIndex
    ) throws InvalidFogResponse, AttestationException, NetworkException {
        View.QueryRequest.Builder requestBuilder = View.QueryRequest.newBuilder();
        View.QueryRequestAAD.Builder aadRequestBuilder = View.QueryRequestAAD.newBuilder();
        if (getTxosKexRngOutputs != null) {
            Logger.i(TAG, "Requesting outputs from fog view", null,
                    "search keys count:", getTxosKexRngOutputs.size());
            for (byte[] kexRngOutput : getTxosKexRngOutputs) {
                requestBuilder.addGetTxos(ByteString.copyFrom(kexRngOutput));
            }
        }
        aadRequestBuilder.setStartFromUserEventId(lastKnownEventId);
        aadRequestBuilder.setStartFromBlockIndex(lastKnownBlockIndex);

        NetworkingCall<View.QueryResponse> networkingCall = new NetworkingCall<>(() -> {
            try {
                FogViewService fogViewService = getAPIManager().getFogViewService(getNetworkTransport());
                Attest.Message message = encryptMessage(requestBuilder.build(), aadRequestBuilder.build());
                Attest.Message encryptedResponse = fogViewService.query(message);
                Attest.Message response = decryptMessage(encryptedResponse);
                View.QueryResponse queryResponse = View.QueryResponse.parseFrom(response.getData());
                return queryResponse;
            } catch (InvalidProtocolBufferException exception) {
                InvalidFogResponse invalidFogResponse = new InvalidFogResponse("View response " +
                        "contains invalid data", exception);
                Util.logException(TAG, invalidFogResponse);
                throw invalidFogResponse;
            }
        });
        try {
            return networkingCall.run();
        } catch (InvalidFogResponse | AttestationException | NetworkException | RuntimeException exception) {
            attestReset();
            Util.logException(TAG, exception);
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("BUG: unreachable code");
        }
    }
}
