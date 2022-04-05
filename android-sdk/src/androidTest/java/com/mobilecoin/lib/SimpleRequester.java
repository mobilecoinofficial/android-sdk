package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.services.http.Requester.Requester;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple Requester is intended to be used for the integration tests
 */
public class SimpleRequester implements Requester {

    private final OkHttpClient httpClient;

    public SimpleRequester(String username, String password) {
        httpClient = new OkHttpClient();
        final String credential = Credentials.basic(username, password);
        httpClient.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                return response.request().newBuilder().header("Authorization", credential).build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return response.request().newBuilder().header("Proxy-Authorization", credential).build();
            }
        });
    }

    @NonNull
    @Override
    public HttpResponse httpRequest(@NonNull String httpMethod,
                                    @NonNull Uri uri,
                                    @NonNull Map<String, String> headers,
                                    @NonNull byte[] body,
                                    @NonNull String contentType
    ) throws IOException {

        RequestBody requestBody = RequestBody.create(
                MediaType.parse(contentType),
                body
        );

        Request request = new Request.Builder()
                .method(httpMethod, requestBody)
                .headers(Headers.of(headers))
                .url(new URL(uri.toString()))
                .build();

        Response response = httpClient.newCall(request).execute();
        byte[] responseBytes = response.body().bytes();
        return new HttpResponse() {
            @Override
            public int getResponseCode() {
                return response.code();
            }

            @Override
            public byte[] getResponseData() {
                return responseBytes;
            }

            @Override
            public Map<String, String> getResponseHeaders() {
                Headers headers = response.headers();
                Set<String> keys = headers.names();
                HashMap<String, String> headerMap = new HashMap<>();
                for (String key : keys) {
                    headerMap.put(key, headers.get(key));
                }
                return headerMap;
            }
        };
    }
}
