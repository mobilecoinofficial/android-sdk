package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.AuthInterceptor;
import com.mobilecoin.lib.network.CookieInterceptor;
import com.mobilecoin.lib.network.services.FogUntrustedService;

import java.util.concurrent.ExecutorService;

import fog_ledger.FogUntrustedTxOutApiGrpc;
import fog_ledger.Ledger;
import io.grpc.ManagedChannel;

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
    public Ledger.TxOutResponse getTxOuts(Ledger.TxOutRequest request) {
        return getApiBlockingStub().getTxOuts(request);
    }
}
