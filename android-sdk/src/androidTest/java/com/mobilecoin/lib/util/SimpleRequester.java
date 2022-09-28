package com.mobilecoin.lib.util;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.services.http.Requester.Requester;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Simple Requester is intended to be used for the integration tests
 */
public class SimpleRequester implements Requester {

    private final OkHttpClient httpClient;
    private final String basicAuthHeader;

    public SimpleRequester(String username, String password) {
        httpClient = new OkHttpClient();
        basicAuthHeader = Credentials.basic(username, password);
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

        headers.put("Authorization", basicAuthHeader);
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
