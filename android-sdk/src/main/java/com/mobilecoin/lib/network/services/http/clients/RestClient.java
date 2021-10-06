package com.mobilecoin.lib.network.services.http.clients;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.network.services.http.Requester;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public final class RestClient {
    public final static String METHOD = "POST";
    public final static String SCHEME = "https";
    public final static String SET_COOKIE_KEY = "Set-Cookie";
    public final static String COOKIE_KEY = "Cookie";
    public final static String CONTENT_TYPE = "application/x-protobuf";
    private final Requester requester;
    private final Uri serviceUri;
    private final HashMap<String, String> headers;

    public RestClient(@NonNull Uri serviceUri, @NonNull Requester requester) {
        this.serviceUri = serviceUri;
        this.requester = requester;
        this.headers = new HashMap<>();
    }

    @NonNull
    public byte[] makeRequest(@NonNull String apiPath,
                              @NonNull byte[] requestBytes) {
        try {
            Uri uri = new Uri.Builder()
                    .scheme(SCHEME)
                    .encodedAuthority(getServiceUri().getEncodedAuthority())
                    .path(apiPath)
                    .build();
            Requester.HttpResponse response = getRequester()
                    .httpRequest(
                            METHOD,
                            uri,
                            getHeaders(),
                            requestBytes,
                            CONTENT_TYPE
                    );
            Status status;
            switch (response.getResponseCode()) {
                case 200:
                    status = Status.OK;
                    break;
                case 401:
                    status = Status.UNAUTHENTICATED.withDescription(
                            new String(response.getResponseData())
                    );
                    break;
                case 403:
                    status = Status.PERMISSION_DENIED.withDescription(
                            new String(response.getResponseData())
                    );
                    break;
                case 404:
                    status = Status.NOT_FOUND.withDescription(
                            new String(response.getResponseData())
                    );
                    break;
                case 500:
                    status = Status.INTERNAL.withDescription(
                            new String(response.getResponseData())
                    );
                    break;
                case 501:
                    status = Status.UNIMPLEMENTED.withDescription(
                            new String(response.getResponseData())
                    );
                    break;
                case 504:
                    status = Status.DEADLINE_EXCEEDED.withDescription(
                            new String(response.getResponseData())
                    );
                    break;
                default:
                    status = Status.UNAVAILABLE.withDescription(
                            new String(response.getResponseData())
                    );
            }
            if (status != Status.OK) {
                throw new StatusRuntimeException(status);
            }
            Map<String, String> headers = response.getResponseHeaders();
            String cookie = headers.get(SET_COOKIE_KEY);
            if (null == cookie) {
                cookie = headers.get(SET_COOKIE_KEY.toLowerCase());
            }
            if (null != cookie) {
                setHeader(COOKIE_KEY, cookie);
            }
            return response.getResponseData();
        } catch (IOException exception) {
            throw new StatusRuntimeException(Status.UNAVAILABLE.withCause(exception));
        }
    }

    synchronized void setHeader(@NonNull String key, @Nullable String value) {
        headers.put(key, value);
    }

    @NonNull
    synchronized Map<String, String> getHeaders() {
        return headers;
    }

    @NonNull
    final Requester getRequester() {
        return requester;
    }

    @NonNull
    public final Uri getServiceUri() {
        return serviceUri;
    }
}
