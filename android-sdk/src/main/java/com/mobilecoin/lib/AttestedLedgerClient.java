// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;


import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.ClientConfig.Service;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.services.FogKeyImageService;
import com.mobilecoin.lib.network.services.FogMerkleProofService;
import com.mobilecoin.lib.network.services.transport.Transport;
import com.mobilecoin.lib.util.NetworkingCall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import attest.Attest;
import fog_ledger.Ledger;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * Attested client for a ledger service Attestation is done automatically by the parent class {@link
 * AttestedClient}
 */
class AttestedLedgerClient extends AttestedClient {
    private static final String TAG = AttestedLedgerClient.class.getName();

    /**
     * Creates and initializes an instance of {@link AttestedLedgerClient}
     *  @param loadBalancer           an address of the service. Example:
     *                      fog://fog.test.mobilecoin.com
     * @param serviceConfig service configuration passed to MobileCoinClient
     */
    AttestedLedgerClient(@NonNull LoadBalancer loadBalancer, @NonNull Service serviceConfig) {
        super(loadBalancer, serviceConfig);
        Logger.i(TAG, "Created new AttestedLedgerClient", null,
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
    protected synchronized void attest(@NonNull Transport transport)
            throws AttestationException, NetworkException {
        try {
            Logger.i(TAG, "Attest ledger connection");
            byte[] requestBytes = attestStart(getCurrentServiceUri());
            FogKeyImageService fogKeyImageService =
                    getAPIManager().getFogKeyImageService(transport);
            ByteString bytes = ByteString.copyFrom(requestBytes);
            Attest.AuthMessage authMessage = Attest.AuthMessage.newBuilder().setData(bytes).build();
            Attest.AuthMessage response = fogKeyImageService.auth(authMessage);
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
            AttestationException attestationException = new AttestationException("Failed to " +
                    "attest the ledger connection", exception);
            Util.logException(TAG, attestationException);
            throw attestationException;
        }
    }

    /**
     * Retrieves outputs If the key is not registered with the service empty message is returned
     *
     * @param indexes is a collection of indexes
     * @return initialized or empty {@link Ledger.GetOutputsResponse} instance
     */
    @NonNull
    public synchronized Ledger.GetOutputsResponse getOutputs(
            @NonNull Collection<UnsignedLong> indexes,
            long merkleRootBlock
    ) throws InvalidFogResponse, AttestationException, NetworkException {
        Logger.i(TAG, "Retrieving outputs");
        Ledger.GetOutputsRequest request =
                Ledger.GetOutputsRequest.newBuilder().addAllIndices(
                        indexes.stream().map(UnsignedLong::longValue).collect(Collectors.toList()))
                        .setMerkleRootBlock(merkleRootBlock).build();
        NetworkingCall<Ledger.GetOutputsResponse> networkingCall =
                new NetworkingCall<>(() -> {
                    try {
                        FogMerkleProofService fogMerkleProofService =
                                getAPIManager().getFogMerkleProofService(getNetworkTransport());
                        Attest.Message message = encryptMessage(request);
                        Attest.Message responseMessage = fogMerkleProofService.getOutputs(message);
                        Attest.Message response = decryptMessage(responseMessage);
                        return Ledger.GetOutputsResponse.parseFrom(response.getData());
                    } catch (StatusRuntimeException exception) {
                        attestReset();
                        throw new NetworkException(exception);
                    } catch (InvalidProtocolBufferException exception) {
                        attestReset();
                        throw new InvalidFogResponse("GetOutputsResponse contains invalid data",
                                exception);
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

    /**
     * Query key images status
     *
     * @param keyImages a list of key images to check
     */
    @NonNull
    public synchronized Ledger.CheckKeyImagesResponse checkKeyImages(
            @NonNull Set<KeyImage> keyImages
    ) throws InvalidFogResponse, AttestationException, NetworkException {
        Logger.i(TAG, "Checking key images", null,
                "size:", keyImages.size());
        ArrayList<Ledger.KeyImageQuery> keyImageQueries = new ArrayList<>();
        for (KeyImage keyImage : keyImages) {
            Ledger.KeyImageQuery query = Ledger.KeyImageQuery.newBuilder()
                    .setKeyImage(MobileCoinAPI.KeyImage.newBuilder()
                            .setData(ByteString.copyFrom(keyImage.getData())).build()).build();
            keyImageQueries.add(query);
        }
        Ledger.CheckKeyImagesRequest imagesRequest =
                Ledger.CheckKeyImagesRequest.newBuilder().addAllQueries(keyImageQueries)
                        .build();
        NetworkingCall<Ledger.CheckKeyImagesResponse> networkingCall =
                new NetworkingCall<>(() -> {
                    try {
                        FogKeyImageService fogKeyImageService =
                                getAPIManager().getFogKeyImageService(getNetworkTransport());
                        Attest.Message encryptedRequest = encryptMessage(imagesRequest);
                        Attest.Message encryptedResponse = fogKeyImageService.checkKeyImages(encryptedRequest);
                        Attest.Message response = decryptMessage(encryptedResponse);
                        return Ledger.CheckKeyImagesResponse.parseFrom(response.getData().toByteArray());
                    } catch (InvalidProtocolBufferException exception) {
                        attestReset();
                        throw new InvalidFogResponse(
                                "CheckKeyImagesResponse contains invalid data", exception);
                    } catch (StatusRuntimeException exception) {
                        attestReset();
                        throw new NetworkException(exception);
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

    /**
     * Query key images status
     *
     * @param txos a list of OwnedTxOuts whose key images to check
     */
    @NonNull
    public synchronized Ledger.CheckKeyImagesResponse checkUtxoKeyImages(@NonNull Set<OwnedTxOut> txos)
            throws InvalidFogResponse, AttestationException, NetworkException {
        Logger.i(TAG, "Checking unspent OwnedTxOut key images");
        HashSet<KeyImage> keyImages = new HashSet<>();
        for (OwnedTxOut txo : txos) {
            KeyImage keyImage = txo.getKeyImage();
            keyImages.add(keyImage);
        }
        return checkKeyImages(keyImages);
    }
}
