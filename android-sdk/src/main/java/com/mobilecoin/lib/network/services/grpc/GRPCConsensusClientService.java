package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.AuthInterceptor;
import com.mobilecoin.lib.network.CookieInterceptor;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.ConsensusClientService;

import java.util.concurrent.ExecutorService;

import attest.Attest;
import consensus_client.ConsensusClientAPIGrpc;
import consensus_common.ConsensusCommon;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

public class GRPCConsensusClientService
        extends GRPCService<ConsensusClientAPIGrpc.ConsensusClientAPIBlockingStub>
        implements ConsensusClientService {
    public GRPCConsensusClientService(@NonNull ManagedChannel managedChannel,
                               @NonNull CookieInterceptor cookieInterceptor,
                               @NonNull AuthInterceptor authInterceptor,
                               @NonNull ExecutorService executorService) {
        super(managedChannel, cookieInterceptor, authInterceptor, executorService);
    }

    @NonNull
    @Override
    ConsensusClientAPIGrpc.ConsensusClientAPIBlockingStub newBlockingStub(@NonNull ManagedChannel managedChannel) {
        return ConsensusClientAPIGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public ConsensusCommon.ProposeTxResponse clientTxPropose(Attest.Message message) throws NetworkException {
        try {
            return getApiBlockingStub().clientTxPropose(message);
        } catch(StatusRuntimeException e) {
            throw new NetworkException(new NetworkResult(new GRPCStatusResponse(e.getStatus())), e);
        }
    }
}
