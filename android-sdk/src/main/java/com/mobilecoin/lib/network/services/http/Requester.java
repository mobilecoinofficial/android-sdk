package com.mobilecoin.lib.network.services.http;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Map;

public interface Requester {
    @NonNull
    HttpResponse httpRequest(
            @NonNull String httpMethod,
            @NonNull Uri uri,
            @NonNull Map<String, String> headers,
            @NonNull byte[] body,
            @NonNull String contentType
    ) throws IOException;

    interface HttpResponse {
        int getResponseCode();

        byte[] getResponseData();

        Map<String, String> getResponseHeaders();
    }
}
