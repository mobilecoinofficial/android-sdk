package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;

import com.google.protobuf.Empty;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.AuthInterceptor;
import com.mobilecoin.lib.network.CookieInterceptor;
import com.mobilecoin.lib.network.services.BlockchainService;

import java.util.concurrent.ExecutorService;

import consensus_common.BlockchainAPIGrpc;
import consensus_common.ConsensusCommon;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

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
    public ConsensusCommon.LastBlockInfoResponse getLastBlockInfo(Empty request) throws NetworkException {
        try {
            return getApiBlockingStub().getLastBlockInfo(request);
        } catch (StatusRuntimeException e) {
            throw new NetworkException(e.getStatus(), e);
        }
    }
}
