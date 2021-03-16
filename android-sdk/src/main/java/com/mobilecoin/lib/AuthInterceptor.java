// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.squareup.okhttp.Credentials;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * AuthInterceptor intercepts GRPC API calls to adds basic authorization header
 */
class AuthInterceptor implements ClientInterceptor {

    // metadata keys are case insensitive
    static final Metadata.Key<String> AUTHORIZATION_HEADER_KEY = Metadata.Key.of(
            "Authorization",
            Metadata.ASCII_STRING_MARSHALLER
    );
    private String authToken;

    /**
     * Authorize requests using the provided credentials.
     * <p>
     * Credentials are encoded and attached as an HTTP header field in the form of Authorization:
     * Basic <credentials>, where credentials is the Base64 encoding of ID and password joined by a
     * single colon :
     */
    void setAuthorization(
            @NonNull String username,
            @NonNull String password
    ) {
        authToken = Credentials.basic(
                username,
                password
        );
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next
    ) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(
                method,
                callOptions
        )) {

            @Override
            public void start(
                    Listener<RespT> responseListener,
                    Metadata headers
            ) {
                if (authToken != null) {
                    headers.put(
                            AUTHORIZATION_HEADER_KEY,
                            authToken
                    );
                }
                super.start(
                        new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                            @Override
                            public void onHeaders(Metadata headers) {
                                super.onHeaders(headers);
                            }
                        },
                        headers
                );
            }
        };
    }
}
