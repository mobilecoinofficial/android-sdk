// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.uri.FogUri;
import com.mobilecoin.lib.util.NetworkingCall;

import java.util.List;

import attest.Attest;
import fog_view.FogViewAPIGrpc;
import fog_view.View;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * Attested client for a Fog View service Attestation is done automatically by the parent class
 * {@link AttestedClient}
 */
class AttestedViewClient extends AttestedClient {
    private static final String TAG = AttestedViewClient.class.getName();

    // The last event id serves as a cursor
    private long lastKnownEventId;

    // The last block index of the already downloaded TXOs
    private long lastKnownBlockIndex;

    /**
     * Creates and initializes an instance of {@link AttestedViewClient}
     *
     * @param uri      an address of the service
     * @param serviceConfig service configuration passed to MobileCoinClient
     */
    AttestedViewClient(@NonNull FogUri uri, @NonNull ClientConfig.Service serviceConfig) {
        super(uri.getUri(), serviceConfig);
        Logger.i(TAG, "Created new AttestedViewClient", null,
                "uri:", uri,
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
     * @param managedChannel a channel that requires attestation
     */
    @Override
    protected synchronized void attest(@NonNull ManagedChannel managedChannel)
            throws AttestationException, NetworkException {
        try {
            Logger.i(TAG, "Attest view connection");
            byte[] requestBytes = attestStart(getServiceUri());
            FogViewAPIGrpc.FogViewAPIBlockingStub blockingRequest =
                    getAPIManager().getFogViewAPIStub(managedChannel);
            ByteString bytes = ByteString.copyFrom(requestBytes);
            Attest.AuthMessage authMessage = Attest.AuthMessage.newBuilder().setData(bytes).build();
            Attest.AuthMessage response = blockingRequest.auth(authMessage);
            attestFinish(response.getData().toByteArray(), getServiceConfig().getVerifier());
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode() == Status.Code.INTERNAL) {
                attestReset();
                throw new AttestationException(exception.getStatus().getDescription(), exception);
            }
            Logger.w(TAG, "Failed to attest the fog view connection", exception);
            throw new NetworkException(exception);
        } catch (Exception exception) {
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
            @Nullable List<byte[]> getTxosKexRngOutputs
    ) throws InvalidFogResponse, AttestationException, NetworkException {
        FogViewAPIGrpc.FogViewAPIBlockingStub view =
                getAPIManager().getFogViewAPIStub(getManagedChannel());
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

        Attest.Message message = encryptMessage(requestBuilder.build(), aadRequestBuilder.build());
        NetworkingCall<View.QueryResponse> networkingCall = new NetworkingCall<>(() -> {
            try {
                Attest.Message encryptedResponse = view.query(message);
                Attest.Message response = decryptMessage(encryptedResponse);
                View.QueryResponse queryResponse = View.QueryResponse.parseFrom(response.getData());
                lastKnownBlockIndex = queryResponse.getHighestProcessedBlockCount();
                lastKnownEventId = queryResponse.getNextStartFromUserEventId();
                return queryResponse;
            } catch (InvalidProtocolBufferException exception) {
                InvalidFogResponse invalidFogResponse = new InvalidFogResponse("View response " +
                        "contains invalid data", exception);
                Util.logException(TAG, invalidFogResponse);
                throw invalidFogResponse;
            } catch (StatusRuntimeException exception) {
                if (exception.getStatus().getCode() == Status.Code.INTERNAL) {
                    AttestationException attestationException =
                            new AttestationException(exception.getStatus().getDescription(),
                                    exception);
                    Util.logException(TAG, attestationException);
                    attestReset();
                    throw attestationException;
                }
                Logger.w(TAG, "Fog request() query has failed", exception);
                throw new NetworkException(exception);
            }
        });
        try {
            return networkingCall.run();
        } catch (InvalidFogResponse | AttestationException | NetworkException | RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("BUG: unreachable code");
        }
    }
}
