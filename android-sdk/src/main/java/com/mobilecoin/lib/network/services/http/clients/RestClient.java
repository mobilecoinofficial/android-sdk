package com.mobilecoin.lib.network.services.http.clients;

import static com.mobilecoin.lib.network.NetworkResult.DEADLINE_EXCEEDED;
import static com.mobilecoin.lib.network.NetworkResult.INTERNAL;
import static com.mobilecoin.lib.network.NetworkResult.NOT_FOUND;
import static com.mobilecoin.lib.network.NetworkResult.OK;
import static com.mobilecoin.lib.network.NetworkResult.PERMISSION_DENIED;
import static com.mobilecoin.lib.network.NetworkResult.UNAUTHENTICATED;
import static com.mobilecoin.lib.network.NetworkResult.UNAVAILABLE;
import static com.mobilecoin.lib.network.NetworkResult.UNIMPLEMENTED;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.http.Requester;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
                              @NonNull byte[] requestBytes) throws NetworkException {
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
            NetworkResult status;
            switch (response.getResponseCode()) {
                case 200:
                    status = OK;
                    break;
                case 401:
                    status = UNAUTHENTICATED.withDescription(
                            new String(response.getResponseData())
                    );
                    break;
                case 403:
                    status = PERMISSION_DENIED.withDescription(
                            new String(response.getResponseData())
                    );
                    break;
                case 404:
                    status = NOT_FOUND.withDescription(
                            new String(response.getResponseData())
                    );
                    break;
                case 500:
                    status = INTERNAL.withDescription(
                            new String(response.getResponseData())
                    );
                    break;
                case 501:
                    status = UNIMPLEMENTED.withDescription(
                            new String(response.getResponseData())
                    );
                    break;
                case 504:
                    status = DEADLINE_EXCEEDED.withDescription(
                            new String(response.getResponseData())
                    );
                    break;
                default:
                    status = UNAVAILABLE.withDescription(
                            new String(response.getResponseData())
                    );
            }
            if (status.getResultCode() != OK.getResultCode()) {
                throw new NetworkException(status);
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
            throw new NetworkException(NetworkResult.UNAVAILABLE, exception);
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
