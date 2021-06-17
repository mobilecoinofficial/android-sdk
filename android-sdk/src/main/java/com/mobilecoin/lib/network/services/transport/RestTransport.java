package com.mobilecoin.lib.network.services.transport;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.services.http.clients.RestClient;

public class RestTransport extends Transport {
    private final RestClient restClient;

    public RestTransport(@NonNull RestClient restClient) {
        this.restClient = restClient;
    }

    @NonNull
    public RestClient getRestClient() {
        return restClient;
    }

    @NonNull
    @Override
    public TransportType getTransportType() {
        return TransportType.HTTP;
    }
}
