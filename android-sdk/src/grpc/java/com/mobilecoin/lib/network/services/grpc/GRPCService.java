package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.grpc.AuthInterceptor;
import com.mobilecoin.lib.network.grpc.CookieInterceptor;
import com.mobilecoin.lib.network.services.ApiService;

import java.util.concurrent.ExecutorService;

import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractStub;

public abstract class GRPCService <T extends AbstractStub<T>> implements ApiService {
    private static final int MEGABYTE = 1024 * 1024;
    private static final int MAX_INBOUND_MESSAGE_SIZE = 50 * MEGABYTE;
    private final T apiBlockingStub;
    private final ManagedChannel managedChannel;

    protected GRPCService(@NonNull ManagedChannel managedChannel,
                @NonNull CookieInterceptor cookieInterceptor,
                @NonNull AuthInterceptor authInterceptor,
                @NonNull ExecutorService executorService) {
        this.managedChannel = managedChannel;
        apiBlockingStub = configureStub(
                newBlockingStub(getManagedChannel()),
                cookieInterceptor,
                authInterceptor,
                executorService
        );
    }

    @NonNull
    protected abstract T newBlockingStub(@NonNull ManagedChannel managedChannel);

    @NonNull
    protected T getApiBlockingStub() {
        return apiBlockingStub;
    }

    @NonNull
    protected ManagedChannel getManagedChannel() {
        return managedChannel;
    }

    @NonNull
    protected T configureStub(@NonNull T stub,
                              @NonNull CookieInterceptor cookieInterceptor,
                              @NonNull AuthInterceptor authInterceptor,
                              @NonNull ExecutorService executorService) {
        return stub
                .withInterceptors(
                        cookieInterceptor,
                        authInterceptor)
                .withMaxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                .withExecutor(executorService);
    }
}
