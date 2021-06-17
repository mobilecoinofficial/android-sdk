// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.network.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.AuthInterceptor;
import com.mobilecoin.lib.network.CookieInterceptor;
import com.mobilecoin.lib.network.services.grpc.GRPCAttestedService;
import com.mobilecoin.lib.network.services.grpc.GRPCBlockchainService;
import com.mobilecoin.lib.network.services.grpc.GRPCConsensusClientService;
import com.mobilecoin.lib.network.services.grpc.GRPCFogBlockService;
import com.mobilecoin.lib.network.services.grpc.GRPCFogKeyImageService;
import com.mobilecoin.lib.network.services.grpc.GRPCFogMerkleProofService;
import com.mobilecoin.lib.network.services.grpc.GRPCFogReportService;
import com.mobilecoin.lib.network.services.grpc.GRPCFogUntrustedService;
import com.mobilecoin.lib.network.services.grpc.GRPCFogViewService;
import com.mobilecoin.lib.network.services.transport.GRPCTransport;
import com.mobilecoin.lib.network.services.transport.Transport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;

public final class GRPCServiceAPIManager implements ServiceAPIManager {
    private static final String TAG = GRPCServiceAPIManager.class.toString();
    private static final int SHUTDOWN_TIMEOUT = 5000;
    private final ExecutorService executorService;
    private final CookieInterceptor cookieInterceptor;
    private final AuthInterceptor authInterceptor;

    public GRPCServiceAPIManager() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.cookieInterceptor = new CookieInterceptor();
        this.authInterceptor = new AuthInterceptor();
    }

    @NonNull
    ManagedChannel managedChannelFromTransport(@NonNull Transport transport) {
        if (transport.getTransportType() == Transport.TransportType.GRPC) {
            GRPCTransport grpcTransport = (GRPCTransport) transport;
            return grpcTransport.getManagedChannel();
        }
        throw new IllegalArgumentException("BUG: should not be reachable");
    }

    @NonNull
    @Override
    public FogViewService getFogViewService(@NonNull Transport transport) {
        return new GRPCFogViewService(
                managedChannelFromTransport(transport),
                cookieInterceptor,
                authInterceptor,
                executorService);
    }

    @NonNull
    @Override
    public FogUntrustedService getFogUntrustedService(@NonNull Transport transport) {
        return new GRPCFogUntrustedService(
                managedChannelFromTransport(transport),
                cookieInterceptor,
                authInterceptor,
                executorService);
    }

    @NonNull
    @Override
    public FogReportService getFogReportService(@NonNull Transport transport) {
        return new GRPCFogReportService(
                managedChannelFromTransport(transport),
                cookieInterceptor,
                authInterceptor,
                executorService);
    }

    @NonNull
    @Override
    public FogKeyImageService getFogKeyImageService(@NonNull Transport transport) {
        return new GRPCFogKeyImageService(
                managedChannelFromTransport(transport),
                cookieInterceptor,
                authInterceptor,
                executorService);
    }

    @NonNull
    @Override
    public FogMerkleProofService getFogMerkleProofService(@NonNull Transport transport) {
        return new GRPCFogMerkleProofService(
                managedChannelFromTransport(transport),
                cookieInterceptor,
                authInterceptor,
                executorService);
    }

    @NonNull
    @Override
    public FogBlockService getFogBlockService(@NonNull Transport transport) {
        return new GRPCFogBlockService(
                managedChannelFromTransport(transport),
                cookieInterceptor,
                authInterceptor,
                executorService);
    }

    @NonNull
    @Override
    public ConsensusClientService getConsensusClientService(@NonNull Transport transport) {
        return new GRPCConsensusClientService(
                managedChannelFromTransport(transport),
                cookieInterceptor,
                authInterceptor,
                executorService);
    }

    @NonNull
    @Override
    public BlockchainService getBlockchainService(@NonNull Transport transport) {
        return new GRPCBlockchainService(
                managedChannelFromTransport(transport),
                cookieInterceptor,
                authInterceptor,
                executorService);
    }

    @NonNull
    @Override
    public AttestedService getAttestedService (@NonNull Transport transport) {
        return new GRPCAttestedService(
                managedChannelFromTransport(transport),
                cookieInterceptor,
                authInterceptor,
                executorService);
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

    @Override
    public void setAuthorization(
            @NonNull String username,
            @NonNull String password
    ) {
        authInterceptor.setAuthorization(
                username,
                password
        );
    }

    @Override
    protected void finalize() throws Throwable {
        retireExecutorService(executorService);
        super.finalize();
    }
}
