package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.services.ApiService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

abstract class RestService implements ApiService {
    private final RestClient restClient;

    protected RestService(@NonNull RestClient restClient) {
        this.restClient = restClient;
    }

    @NonNull
    protected RestClient getRestClient() {
        return restClient;
    }
}
