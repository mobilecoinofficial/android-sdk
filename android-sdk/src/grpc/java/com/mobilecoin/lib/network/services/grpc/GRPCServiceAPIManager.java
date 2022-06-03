// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.grpc.AuthInterceptor;
import com.mobilecoin.lib.network.grpc.CookieInterceptor;
import com.mobilecoin.lib.network.services.AttestedService;
import com.mobilecoin.lib.network.services.BlockchainService;
import com.mobilecoin.lib.network.services.ConsensusClientService;
import com.mobilecoin.lib.network.services.FogBlockService;
import com.mobilecoin.lib.network.services.FogKeyImageService;
import com.mobilecoin.lib.network.services.FogMerkleProofService;
import com.mobilecoin.lib.network.services.FogReportService;
import com.mobilecoin.lib.network.services.FogUntrustedService;
import com.mobilecoin.lib.network.services.FogViewService;
import com.mobilecoin.lib.network.services.ServiceAPIManager;
import com.mobilecoin.lib.network.services.transport.Transport;
import com.mobilecoin.lib.network.services.transport.grpc.GRPCTransport;

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
