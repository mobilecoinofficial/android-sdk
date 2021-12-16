package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.AuthInterceptor;
import com.mobilecoin.lib.network.CookieInterceptor;
import com.mobilecoin.lib.network.services.FogBlockService;

import java.util.concurrent.ExecutorService;

import fog_ledger.FogBlockAPIGrpc;
import fog_ledger.Ledger;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

public class GRPCFogBlockService
        extends GRPCService<FogBlockAPIGrpc.FogBlockAPIBlockingStub>
        implements FogBlockService {
    public GRPCFogBlockService(@NonNull ManagedChannel managedChannel,
                        @NonNull CookieInterceptor cookieInterceptor,
                        @NonNull AuthInterceptor authInterceptor,
                        @NonNull ExecutorService executorService) {
        super(managedChannel, cookieInterceptor, authInterceptor, executorService);
    }

    @NonNull
    @Override
    FogBlockAPIGrpc.FogBlockAPIBlockingStub newBlockingStub(@NonNull ManagedChannel managedChannel) {
        return FogBlockAPIGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public Ledger.BlockResponse getBlocks(Ledger.BlockRequest request) throws NetworkException {
        try {
            return getApiBlockingStub().getBlocks(request);
        } catch (StatusRuntimeException e) {
            throw new NetworkException(e.getStatus(), e);
        }
    }
}
