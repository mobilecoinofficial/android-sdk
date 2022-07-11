package com.mobilecoin.lib.network.services.http.Requester;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.mobilecoin.lib.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HttpRequester class is used to make a HTTP request and returns the response.
 */

public class HttpRequester implements Requester {
    private static final String HEADER_CONTENT_TYPE_KEY = "Content-Type";
    private final String credentials;

    public HttpRequester(String username, String password) {
        byte credentialBytes[] = (username + ":" + password).getBytes(StandardCharsets.ISO_8859_1);
        this.credentials = "Basic " + android.util.Base64.encodeToString(credentialBytes, 0);
        password = null;
    }

    /**
     * Builds a request using the arguments, parses the response to
     * return response code, headers and data.
     *
     * @param httpMethod  Method name for the request
     * @param uri         URI target for the request
     * @param headers     Headers to be added to the request
     * @param body        Request body
     * @param contentType Header to be added to the request
     * @return HttpResponse which contains response code, data and headers
     * @throws IOException
     */

    @NonNull
    @Override
    public HttpResponse httpRequest(@NonNull String httpMethod,
                                    @NonNull Uri uri,
                                    @NonNull Map<String, String> headers,
                                    @NonNull byte[] body,
                                    @NonNull String contentType) throws IOException {
        headers.put("Authorization", credentials);
        HttpURLConnection connection = createConnection(httpMethod, uri, headers, body, contentType);
        ByteArrayOutputStream byteArrayOutputStream = parseResponse(connection);
        int responseCode = connection.getResponseCode();
        return new HttpResponse() {
            @Override
            public int getResponseCode() {
                return responseCode;
            }

            @Override
            public byte[] getResponseData() {
                return byteArrayOutputStream.toByteArray();
            }

            @Override
            public Map<String, String> getResponseHeaders() {
                return parseHeaderFields(connection);
            }
        };
    }

    @NonNull
    @VisibleForTesting
    HttpURLConnection createConnection(@NonNull String httpMethod,
                                              @NonNull Uri uri,
                                              @NonNull Map<String, String> headers,
                                              @NonNull byte[] body,
                                              @NonNull String contentType) throws IOException {
        URL url = new URL(uri.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod(httpMethod);
        addRequestHeaders(connection, headers, contentType);
        addRequestBody(connection, body);
        connection.connect();
        return connection;
    }

    private void addRequestHeaders(@NonNull HttpURLConnection connection,
                                   @NonNull Map<String, String> headers,
                                   @NonNull String contentType) {
        connection.setRequestProperty(HEADER_CONTENT_TYPE_KEY, contentType);
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey().trim(), header.getValue().trim());
        }
    }

    private void addRequestBody(@NonNull HttpURLConnection connection,
                                @NonNull byte[] body) throws IOException {
        OutputStream os = connection.getOutputStream();
        os.write(body);
        os.close();
    }

    @NonNull
    private ByteArrayOutputStream parseResponse(@NonNull HttpURLConnection connection) throws IOException {
        InputStream responseStream = null;
        ByteArrayOutputStream byteArrayStream;
        try {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                responseStream = connection.getInputStream();
            } else {
                responseStream = connection.getErrorStream();
            }
            byteArrayStream = convertToByteArrayStream(responseStream);
        } finally {
            if (responseStream != null) {
                responseStream.close();
            }
            connection.disconnect();
        }
        return byteArrayStream;
    }

    @NonNull
    private ByteArrayOutputStream convertToByteArrayStream(InputStream responseStream) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while ((length = responseStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream;
    }

    @NonNull
    private HashMap<String, String> parseHeaderFields(@NonNull HttpURLConnection connection) {
        Map<String, List<String>> headers = connection.getHeaderFields();
        HashMap<String, String> headerMap = new HashMap<>();
        if (headers != null) {
            Set<String> keys = headers.keySet();
            for (String key : keys) {
                if (key != null) {
                    headerMap.put(key, Util.listToString(headers.get(key)));
                }
            }
        }
        return headerMap;
    }
}
