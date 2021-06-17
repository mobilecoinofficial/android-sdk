package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.AuthInterceptor;
import com.mobilecoin.lib.network.CookieInterceptor;
import com.mobilecoin.lib.network.services.FogMerkleProofService;

import java.util.concurrent.ExecutorService;

import attest.Attest;
import fog_ledger.FogMerkleProofAPIGrpc;
import io.grpc.ManagedChannel;

public class GRPCFogMerkleProofService
        extends GRPCService<FogMerkleProofAPIGrpc.FogMerkleProofAPIBlockingStub>
        implements FogMerkleProofService {
    public GRPCFogMerkleProofService(@NonNull ManagedChannel managedChannel,
                                     @NonNull CookieInterceptor cookieInterceptor,
                                     @NonNull AuthInterceptor authInterceptor,
                                     @NonNull ExecutorService executorService) {
        super(managedChannel, cookieInterceptor, authInterceptor, executorService);
    }

    @NonNull
    @Override
    FogMerkleProofAPIGrpc.FogMerkleProofAPIBlockingStub newBlockingStub(
            @NonNull ManagedChannel managedChannel
    ) {
        return FogMerkleProofAPIGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public Attest.Message getOutputs(Attest.Message request) {
        return getApiBlockingStub().getOutputs(request);
    }
}
