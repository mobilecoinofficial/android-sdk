package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.grpc.AuthInterceptor;
import com.mobilecoin.lib.network.grpc.CookieInterceptor;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.grpc.GRPCStatusResponse;
import com.mobilecoin.lib.network.services.FogUntrustedService;

import java.util.concurrent.ExecutorService;

import fog_ledger.FogUntrustedTxOutApiGrpc;
import fog_ledger.Ledger;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

public class GRPCFogUntrustedService
        extends GRPCService<FogUntrustedTxOutApiGrpc.FogUntrustedTxOutApiBlockingStub>
        implements FogUntrustedService {

    public GRPCFogUntrustedService(@NonNull ManagedChannel managedChannel,
                                   @NonNull CookieInterceptor cookieInterceptor,
                                   @NonNull AuthInterceptor authInterceptor,
                                   @NonNull ExecutorService executorService) {
        super(managedChannel, cookieInterceptor, authInterceptor, executorService);
    }

    @NonNull
    @Override
    FogUntrustedTxOutApiGrpc.FogUntrustedTxOutApiBlockingStub newBlockingStub(
            @NonNull ManagedChannel managedChannel
    ) {
        return FogUntrustedTxOutApiGrpc.newBlockingStub(getManagedChannel());
    }

    @Override
    public Ledger.TxOutResponse getTxOuts(Ledger.TxOutRequest request) throws NetworkException {
        try {
            return getApiBlockingStub().getTxOuts(request);
        } catch (StatusRuntimeException e) {
            throw new NetworkException(new NetworkResult(new GRPCStatusResponse(e.getStatus())), e);
        }
    }
}
