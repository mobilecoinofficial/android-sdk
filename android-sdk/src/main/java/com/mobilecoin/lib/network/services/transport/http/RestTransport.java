package com.mobilecoin.lib.network.services.transport.http;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.http.clients.RestClient;
import com.mobilecoin.lib.network.services.transport.Transport;
import com.mobilecoin.lib.network.uri.MobileCoinUri;

public class RestTransport extends Transport {
    private final RestClient restClient;

    public RestTransport(@NonNull TransportProtocol transportProtocol,
                         @NonNull MobileCoinUri currentUri) {
        this.restClient = new RestClient(currentUri.getUri(), transportProtocol.getHttpRequester());
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

    @Override
    public void shutdown() {/*  */}

}
