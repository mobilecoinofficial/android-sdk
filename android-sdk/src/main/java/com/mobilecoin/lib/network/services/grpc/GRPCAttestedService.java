package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.AuthInterceptor;
import com.mobilecoin.lib.network.CookieInterceptor;
import com.mobilecoin.lib.network.services.AttestedService;

import java.util.concurrent.ExecutorService;

import attest.Attest;
import attest.AttestedApiGrpc;
import io.grpc.ManagedChannel;

public class GRPCAttestedService
        extends GRPCService<AttestedApiGrpc.AttestedApiBlockingStub>
        implements AttestedService {
    public GRPCAttestedService(@NonNull ManagedChannel managedChannel,
                               @NonNull CookieInterceptor cookieInterceptor,
                               @NonNull AuthInterceptor authInterceptor,
                               @NonNull ExecutorService executorService) {
        super(managedChannel, cookieInterceptor, authInterceptor, executorService);
    }

    @NonNull
    @Override
    AttestedApiGrpc.AttestedApiBlockingStub newBlockingStub(@NonNull ManagedChannel managedChannel) {
        return AttestedApiGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public Attest.AuthMessage auth(Attest.AuthMessage authMessage) {
        return getApiBlockingStub().auth(authMessage);
    }
}
