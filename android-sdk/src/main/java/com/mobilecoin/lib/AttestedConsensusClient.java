// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.ClientConfig.Service;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.AttestedService;
import com.mobilecoin.lib.network.services.ConsensusClientService;
import com.mobilecoin.lib.network.services.transport.Transport;
import com.mobilecoin.lib.util.NetworkingCall;

import attest.Attest;
import consensus_common.ConsensusCommon;

/**
 * Attested client for a consensus service
 */
class AttestedConsensusClient extends AttestedClient {
    private static final String TAG = AttestedConsensusClient.class.getName();

    /**
     * Creates and initializes an instance of {@link AttestedViewClient}
     *  @param loadBalancer           an address of the service. Example:
     *                      mc://consensus.test.mobilecoin.com
     * @param serviceConfig service configuration passed to MobileCoinClient
     */
    AttestedConsensusClient(@NonNull LoadBalancer loadBalancer,
                            @NonNull Service serviceConfig,
                            @NonNull TransportProtocol transportProtocol) {
        super(loadBalancer, serviceConfig, transportProtocol);
        Logger.i(TAG, "Created new AttestedConsensusClient", null,
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
     * 3) {@code attestFinish} uses service response to
     * derive the key for encrypted communication in the subsequent messages
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
            Logger.i(TAG, "Attest consensus connection");
            byte[] requestBytes = attestStart(getCurrentServiceUri());
            AttestedService attestedService = getAPIManager().getAttestedService(transport);
            ByteString bytes = ByteString.copyFrom(requestBytes);
            Attest.AuthMessage authMessage = Attest.AuthMessage.newBuilder().setData(bytes).build();
            Attest.AuthMessage response = attestedService.auth(authMessage);
            attestFinish(response.getData().toByteArray(), getServiceConfig().getTrustedIdentities());
        } catch (NetworkException exception) {
            attestReset();
            if (exception.getResult().getResultCode() == NetworkResult.ResultCode.INTERNAL) {
                AttestationException attestationException =
                        new AttestationException(exception.getResult().getDescription(), exception);
                Util.logException(TAG, attestationException);
                throw attestationException;
            }
            Util.logException(TAG, exception);
            throw exception;
        } catch (Exception exception) {
            attestReset();
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
                            ConsensusClientService consensusClientService =
                                    getAPIManager().getConsensusClientService(getNetworkTransport());
                            Attest.Message encryptedRequest = encryptMessage(tx);
                            return consensusClientService.clientTxPropose(encryptedRequest);
                        }
                );
        try {
            return networkingCall.run();
        } catch (AttestationException | NetworkException | RuntimeException exception) {
            attestReset();
            Util.logException(TAG, exception);
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("BUG: unreachable code");
        }
    }
}
