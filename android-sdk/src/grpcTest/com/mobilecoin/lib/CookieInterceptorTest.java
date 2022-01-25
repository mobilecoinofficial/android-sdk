// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.Nullable;

import com.mobilecoin.lib.network.grpc.CookieInterceptor;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

public class CookieInterceptorTest {
    static final String COOKIE_NAME = "FOO";
    static final String COOKIE_VALUE = "BAR";
    static final Metadata.Key<String> SET_COOKIE_HEADER_KEY = Metadata.Key.of(
            "set-cookie",
            Metadata.ASCII_STRING_MARSHALLER
    );
    static final Metadata.Key<String> COOKIE_HEADER_KEY = Metadata.Key.of(
            "cookie",
            Metadata.ASCII_STRING_MARSHALLER
    );

    @Test
    public void test_cookie_received() {
        final URI path = URI.create("https://www.test.com/grpc.Service/GetCookies");
        final Metadata requestHeaders = new Metadata();
        final Metadata responseHeaders = new Metadata();
        final HttpCookie srcCookie = new HttpCookie(
                COOKIE_NAME,
                COOKIE_VALUE
        );
        responseHeaders.put(
                SET_COOKIE_HEADER_KEY,
                srcCookie.toString()
        );

        CookieInterceptor interceptor = new CookieInterceptor();

        runInterceptCall(
                interceptor,
                path,
                requestHeaders,
                responseHeaders
        );
        Assert.assertTrue(
                "service cookie must be correctly stored in the interceptor",
                isServiceCookieValid(interceptor.getServiceCookie())
        );

        Metadata secondRequestHeaders = runInterceptCall(interceptor,
                                                         path,
                                                         requestHeaders,
                                                         responseHeaders
        );
        String cookies = secondRequestHeaders.get(COOKIE_HEADER_KEY);
        Assert.assertTrue(
                "every subsequent request must include a service cookie",
                isServiceCookieValid(cookies)
        );
    }

    private boolean isServiceCookieValid(String cookieString) {
        List<HttpCookie> cookiePairs = HttpCookie.parse(cookieString);
        for (HttpCookie cookie : cookiePairs) {
            if (cookie.getValue().equals(COOKIE_VALUE) && cookie.getName().equals(COOKIE_NAME)) {
                return true;
            }
        }
        return false;
    }

    private Metadata runInterceptCall(
            final CookieInterceptor interceptor,
            final URI uri,
            final Metadata requestHeaders,
            final Metadata responseHeaders
    ) {

        final AtomicReference<Metadata> actualRequestHeadersStore = new AtomicReference<>(null);

        final StringMarshaller marshaller = new StringMarshaller();
        final MethodDescriptor<String, String> methodDescriptor = MethodDescriptor.
                newBuilder(
                        marshaller,
                        marshaller
                ).
                setFullMethodName(uri.getPath().substring(1)).
                setType(MethodDescriptor.MethodType.UNARY).
                build();
        final CallOptions callOptions = CallOptions.DEFAULT.withAuthority(uri.getAuthority());
        final Channel channel = new Channel() {
            @Override
            public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(
                    final MethodDescriptor<RequestT, ResponseT> methodDescriptor,
                    final CallOptions callOptions
            ) {
                return new ClientCall<RequestT, ResponseT>() {
                    @Override
                    public void start(
                            final Listener<ResponseT> responseListener,
                            final Metadata requestHeaders
                    ) {
                        actualRequestHeadersStore.set(requestHeaders);
                        responseListener.onHeaders(responseHeaders);
                    }

                    @Override
                    public void request(final int numMessages) {
                        // do nothing
                    }

                    @Override
                    public void cancel(
                            @Nullable final String message,
                            @Nullable final Throwable cause
                    ) {
                        // do nothing
                    }

                    @Override
                    public void halfClose() {
                        // do nothing
                    }

                    @Override
                    public void sendMessage(final RequestT message) {
                        // do nothing
                    }
                };
            }

            @Override
            public String authority() {
                return null;
            }
        };

        final ClientCall<String, String> clientCall = interceptor.interceptCall(methodDescriptor,
                                                                                callOptions,
                                                                                channel
        );
        final ClientCall.Listener<String> responseListener = new ClientCall.Listener<String>() {
            @Override
            public void onHeaders(Metadata headers) {
                super.onHeaders(headers);
            }

            @Override
            public void onMessage(String message) {
                super.onMessage(message);
            }

            @Override
            public void onClose(
                    Status status,
                    Metadata trailers
            ) {
                super.onClose(
                        status,
                        trailers
                );
            }

            @Override
            public void onReady() {
                super.onReady();
            }
        };
        clientCall.start(
                responseListener,
                requestHeaders
        );

        return actualRequestHeadersStore.get();
    }
    private static class StringMarshaller implements MethodDescriptor.Marshaller<String> {
        @Override
        public InputStream stream(final String value) {
            return new ByteArrayInputStream(value.getBytes());
        }

        @Override
        public String parse(final InputStream stream) {
            try {
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                final byte[] data = new byte[1024];

                int numRead;
                while ((numRead = stream.read(data,
                                              0,
                                              data.length
                )) != -1) {
                    buffer.write(
                            data,
                            0,
                            numRead
                    );
                }

                buffer.flush();
                byte[] byteArray = buffer.toByteArray();

                return new String(
                        byteArray,
                        StandardCharsets.UTF_8
                );
            } catch (final Throwable throwable) {
                return null;
            }
        }
    }
}
