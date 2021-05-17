package com.mobilecoin.lib.network.services.transport;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.services.http.clients.RestClient;

import io.grpc.ManagedChannel;

public abstract class Transport {
    public static RestTransport fromRestClient(@NonNull RestClient restClient) {
        return new RestTransport(restClient);
    }

    public static GRPCTransport fromManagedChannel(@NonNull ManagedChannel managedChannel) {
        return new GRPCTransport(managedChannel);
    }

    @NonNull
    public abstract TransportType getTransportType();

    public enum TransportType {
        GRPC,
        HTTP
    }
}
