package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;

import com.google.protobuf.Empty;
import com.mobilecoin.lib.network.AuthInterceptor;
import com.mobilecoin.lib.network.CookieInterceptor;
import com.mobilecoin.lib.network.services.BlockchainService;

import java.util.concurrent.ExecutorService;

import consensus_common.BlockchainAPIGrpc;
import consensus_common.ConsensusCommon;
import io.grpc.ManagedChannel;

public class GRPCBlockchainService
        extends GRPCService<BlockchainAPIGrpc.BlockchainAPIBlockingStub>
        implements BlockchainService {
    public GRPCBlockchainService(@NonNull ManagedChannel managedChannel,
                                 @NonNull CookieInterceptor cookieInterceptor,
                                 @NonNull AuthInterceptor authInterceptor,
                                 @NonNull ExecutorService executorService) {
        super(managedChannel, cookieInterceptor, authInterceptor, executorService);
    }

    @NonNull
    @Override
    BlockchainAPIGrpc.BlockchainAPIBlockingStub newBlockingStub(@NonNull ManagedChannel managedChannel) {
        return BlockchainAPIGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public ConsensusCommon.LastBlockInfoResponse getLastBlockInfo(Empty request) {
        return getApiBlockingStub().getLastBlockInfo(request);
    }
}
