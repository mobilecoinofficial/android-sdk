package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.AuthInterceptor;
import com.mobilecoin.lib.network.CookieInterceptor;
import com.mobilecoin.lib.network.services.FogViewService;

import java.util.concurrent.ExecutorService;

import attest.Attest;
import fog_view.FogViewAPIGrpc;
import io.grpc.ManagedChannel;

public class GRPCFogViewService extends GRPCService<FogViewAPIGrpc.FogViewAPIBlockingStub> implements FogViewService {

    public GRPCFogViewService(@NonNull ManagedChannel managedChannel,
                              @NonNull CookieInterceptor cookieInterceptor,
                              @NonNull AuthInterceptor authInterceptor,
                              @NonNull ExecutorService executorService) {
        super(managedChannel, cookieInterceptor, authInterceptor, executorService);
    }

    @NonNull
    @Override
    FogViewAPIGrpc.FogViewAPIBlockingStub newBlockingStub(@NonNull ManagedChannel managedChannel) {
        return FogViewAPIGrpc.newBlockingStub(getManagedChannel());
    }

    @Override
    public Attest.AuthMessage auth(Attest.AuthMessage authMessage) {
        return getApiBlockingStub().auth(authMessage);
    }

    @Override
    public Attest.Message query(Attest.Message message) {
        return getApiBlockingStub().query(message);
    }
}
