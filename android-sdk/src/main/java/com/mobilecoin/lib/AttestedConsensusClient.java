// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.uri.ConsensusUri;
import com.mobilecoin.lib.util.NetworkingCall;

import attest.Attest;
import attest.AttestedApiGrpc;
import consensus_client.ConsensusClientAPIGrpc;
import consensus_common.ConsensusCommon;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * Attested client for a consensus service
 */
final class AttestedConsensusClient extends AttestedClient {
    private static final String TAG = AttestedConsensusClient.class.getName();

    /**
     * Creates and initializes an instance of {@link AttestedViewClient}
     *
     * @param uri           an address of the service. Example:
     *                      mc://consensus.test.mobilecoin.com
     * @param serviceConfig service configuration passed to MobileCoinClient
     */
    AttestedConsensusClient(@NonNull ConsensusUri uri,
                            @NonNull ClientConfig.Service serviceConfig) {
        super(uri.getUri(), serviceConfig);
        Logger.i(TAG, "Created new AttestedConsensusClient", null,
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
     * 3) {@code attestFinish} uses service response to
     * derive the key for encrypted communication in the subsequent messages
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
            Logger.i(TAG, "Attest consensus connection");
            byte[] requestBytes = attestStart(getServiceUri());
            AttestedApiGrpc.AttestedApiBlockingStub blockingRequest =
                    getAPIManager().getAttestedAPIStub(managedChannel);
            ByteString bytes = ByteString.copyFrom(requestBytes);
            Attest.AuthMessage authMessage = Attest.AuthMessage.newBuilder().setData(bytes).build();
            Attest.AuthMessage response = blockingRequest.auth(authMessage);
            attestFinish(response.getData().toByteArray(), getServiceConfig().getVerifier());
        } catch (StatusRuntimeException exception) {
            attestReset();
            if (exception.getStatus().getCode() == Status.Code.INTERNAL) {
                AttestationException attestationException =
                        new AttestationException(exception.getStatus().getDescription(), exception);
                Util.logException(TAG, attestationException);
                throw attestationException;
            }
            NetworkException networkException = new NetworkException(exception);
            Util.logException(TAG, networkException);
            throw networkException;
        } catch (Exception exception) {
            AttestationException attestationException = new AttestationException("Failed to" +
                    " attest the consensus connection", exception);
            Util.logException(TAG, attestationException);
            throw attestationException;
        }
    }

    /**
     * Propose a new transaction to a consensus network
     */
    synchronized ConsensusCommon.ProposeTxResponse proposeTx(@NonNull MobileCoinAPI.Tx tx)
            throws AttestationException, NetworkException {
        NetworkingCall<ConsensusCommon.ProposeTxResponse> networkingCall =
                new NetworkingCall<>(
                        () -> {
                            Logger.i(TAG, "Propose transaction to consensus");
                            ConsensusClientAPIGrpc.ConsensusClientAPIBlockingStub blockingRequest =
                                    getAPIManager().getConsensusAPIStub(getManagedChannel());
                            Attest.Message encryptedRequest = encryptMessage(tx);
                            try {
                                return blockingRequest.clientTxPropose(encryptedRequest);
                            } catch (StatusRuntimeException exception) {
                                attestReset();
                                throw new NetworkException(exception);
                            }
                        }
                );
        try {
            return networkingCall.run();
        } catch (AttestationException | NetworkException | RuntimeException exception) {
            Util.logException(TAG, exception);
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("BUG: unreachable code");
        }
    }
}
