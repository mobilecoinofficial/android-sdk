// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.log.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import attest.AttestedApiGrpc;
import consensus_client.ConsensusClientAPIGrpc;
import fog_ledger.FogBlockAPIGrpc;
import fog_ledger.FogKeyImageAPIGrpc;
import fog_ledger.FogMerkleProofAPIGrpc;
import fog_ledger.FogUntrustedTxOutApiGrpc;
import fog_view.FogViewAPIGrpc;
import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractStub;
import report.ReportAPIGrpc;

class ServiceAPIManager {
    private static final String TAG = AttestedClient.class.toString();
    private static final int SHUTDOWN_TIMEOUT = 5000;
    private static final int MEGABYTE = 1024 * 1024;
    private static final int MAX_INBOUND_MESSAGE_SIZE = 50 * MEGABYTE;
    private final ExecutorService executorService;
    private final CookieInterceptor cookieInterceptor;
    private final AuthInterceptor authInterceptor;
    private FogViewAPIGrpc.FogViewAPIBlockingStub fogViewAPIBlockingStub;
    private ReportAPIGrpc.ReportAPIBlockingStub reportAPIBlockingStub;
    private FogKeyImageAPIGrpc.FogKeyImageAPIBlockingStub fogKeyImageAPIBlockingStub;
    private FogMerkleProofAPIGrpc.FogMerkleProofAPIBlockingStub fogMerkleProofAPIBlockingStub;
    private FogBlockAPIGrpc.FogBlockAPIBlockingStub fogBlockAPIBlockingStub;
    private ConsensusClientAPIGrpc.ConsensusClientAPIBlockingStub consensusClientAPIBlockingStub;
    private AttestedApiGrpc.AttestedApiBlockingStub attestedApiBlockingStub;
    private FogUntrustedTxOutApiGrpc.FogUntrustedTxOutApiBlockingStub fogUntrustedTxOutApiBlockingStub;

    ServiceAPIManager() {
        executorService = Executors.newSingleThreadExecutor();
        cookieInterceptor = new CookieInterceptor();
        authInterceptor = new AuthInterceptor();
    }

    @NonNull
    <T extends AbstractStub<T>> T configureStub(@NonNull T stub) {
        return stub
                .withInterceptors(
                        cookieInterceptor,
                        authInterceptor)
                .withMaxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                .withExecutor(executorService);
    }

    @NonNull
    synchronized FogViewAPIGrpc.FogViewAPIBlockingStub getFogViewAPIStub(
            @NonNull ManagedChannel managedChannel
    ) {
        if (fogViewAPIBlockingStub == null) {
            fogViewAPIBlockingStub = configureStub(FogViewAPIGrpc.newBlockingStub(managedChannel));
        }
        return fogViewAPIBlockingStub;
    }

    @NonNull
    synchronized FogUntrustedTxOutApiGrpc.FogUntrustedTxOutApiBlockingStub getFogUntrustedTxOutApiBlockingStub(
            @NonNull ManagedChannel managedChannel
    ) {
        if (fogUntrustedTxOutApiBlockingStub == null) {
            fogUntrustedTxOutApiBlockingStub =
                    configureStub(FogUntrustedTxOutApiGrpc.newBlockingStub(managedChannel));
        }
        return fogUntrustedTxOutApiBlockingStub;
    }

    @NonNull
    synchronized ReportAPIGrpc.ReportAPIBlockingStub getReportAPIStub(
            @NonNull ManagedChannel managedChannel
    ) {
        if (reportAPIBlockingStub == null) {
            reportAPIBlockingStub = configureStub(ReportAPIGrpc.newBlockingStub(managedChannel));
        }
        return reportAPIBlockingStub;
    }

    @NonNull
    synchronized FogKeyImageAPIGrpc.FogKeyImageAPIBlockingStub getFogKeyImageAPIStub(
            @NonNull ManagedChannel managedChannel
    ) {
        if (fogKeyImageAPIBlockingStub == null) {
            fogKeyImageAPIBlockingStub =
                    configureStub(FogKeyImageAPIGrpc.newBlockingStub(managedChannel));
        }
        return fogKeyImageAPIBlockingStub;
    }

    @NonNull
    synchronized FogMerkleProofAPIGrpc.FogMerkleProofAPIBlockingStub getMerkleProofAPIStub(
            @NonNull ManagedChannel managedChannel
    ) {
        if (fogMerkleProofAPIBlockingStub == null) {
            fogMerkleProofAPIBlockingStub =
                    configureStub(FogMerkleProofAPIGrpc.newBlockingStub(managedChannel));
        }
        return fogMerkleProofAPIBlockingStub;
    }

    @NonNull
    synchronized FogBlockAPIGrpc.FogBlockAPIBlockingStub getBlockAPIStub(
            @NonNull ManagedChannel managedChannel
    ) {
        if (fogBlockAPIBlockingStub == null) {
            fogBlockAPIBlockingStub =
                    configureStub(FogBlockAPIGrpc.newBlockingStub(managedChannel));
        }
        return fogBlockAPIBlockingStub;
    }

    @NonNull
    synchronized ConsensusClientAPIGrpc.ConsensusClientAPIBlockingStub getConsensusAPIStub(
            @NonNull ManagedChannel managedChannel
    ) {
        if (consensusClientAPIBlockingStub == null) {
            consensusClientAPIBlockingStub =
                    configureStub(ConsensusClientAPIGrpc.newBlockingStub(managedChannel));
        }
        return consensusClientAPIBlockingStub;
    }

    @NonNull
    synchronized AttestedApiGrpc.AttestedApiBlockingStub getAttestedAPIStub(
            @NonNull ManagedChannel managedChannel
    ) {
        if (attestedApiBlockingStub == null) {
            attestedApiBlockingStub =
                    configureStub(AttestedApiGrpc.newBlockingStub(managedChannel));
        }
        return attestedApiBlockingStub;
    }

    void retireExecutorService(@Nullable ExecutorService executorService) {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(
                        SHUTDOWN_TIMEOUT,
                        TimeUnit.MILLISECONDS
                )) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(
                            SHUTDOWN_TIMEOUT,
                            TimeUnit.MILLISECONDS
                    )) Logger.e(TAG, "Unable to terminate ExecutorService");
                }
            } catch (InterruptedException e) {
                String message = e.getLocalizedMessage();
                Logger.e(TAG, message != null
                                ? message
                                : e.toString()
                );
                executorService.shutdownNow();
            }
        }
    }

    void setAuthorization(
            @NonNull String username,
            @NonNull String password
    ) {
        authInterceptor.setAuthorization(
                username,
                password
        );
    }

    synchronized void reset() {
        fogViewAPIBlockingStub = null;
        reportAPIBlockingStub = null;
        fogKeyImageAPIBlockingStub = null;
        fogMerkleProofAPIBlockingStub = null;
        fogBlockAPIBlockingStub = null;
        consensusClientAPIBlockingStub = null;
        attestedApiBlockingStub = null;
    }

    @Override
    protected void finalize() throws Throwable {
        retireExecutorService(executorService);
        super.finalize();
    }
}
