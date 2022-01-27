// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.network.grpc;

import com.mobilecoin.lib.log.Logger;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * CookieInterceptor intercepts incoming calls to preserve service cookies and attach them to
 * outgoing requests. Cookie persistence is required for session stickiness on the load balanced
 * services.
 */
public final class CookieInterceptor implements ClientInterceptor {
    private final static String TAG = CookieInterceptor.class.getName();

    // metadata keys are case insensitive
    static final Metadata.Key<String> SET_COOKIE_HEADER_KEY = Metadata.Key.of(
            "set-cookie",
            Metadata.ASCII_STRING_MARSHALLER
    );
    static final Metadata.Key<String> COOKIE_HEADER_KEY = Metadata.Key.of(
            "cookie",
            Metadata.ASCII_STRING_MARSHALLER
    );
    private String serviceCookie;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next
    ) {
        Logger.i(TAG, "Intercepting client call");
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(
                method,
                callOptions
        )) {

            @Override
            public void start(
                    Listener<RespT> responseListener,
                    Metadata headers
            ) {
                String serviceCookie = getServiceCookie();
                if (serviceCookie != null) {
                    headers.put(
                            COOKIE_HEADER_KEY,
                            serviceCookie
                    );
                }
                super.start(
                        new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                            @Override
                            public void onHeaders(Metadata headers) {
                                if (headers.containsKey(SET_COOKIE_HEADER_KEY)) {
                                    String serviceCookie = headers.get(SET_COOKIE_HEADER_KEY);
                                    setServiceCookie(serviceCookie);
                                }
                                super.onHeaders(headers);
                            }
                        },
                        headers
                );
            }
        };
    }

    synchronized public String getServiceCookie() {
        return serviceCookie;
    }

    synchronized void setServiceCookie(String serviceCookie) {
        this.serviceCookie = serviceCookie;
    }
}
