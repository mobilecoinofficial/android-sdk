package com.mobilecoin.lib.network.services.transport;

import androidx.annotation.NonNull;

import io.grpc.ManagedChannel;

public class GRPCTransport extends Transport {
    private final ManagedChannel managedChannel;

    GRPCTransport(@NonNull ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    @NonNull
    public ManagedChannel getManagedChannel() {
        return managedChannel;
    }

    @NonNull
    @Override
    public TransportType getTransportType() {
        return TransportType.GRPC;
    }
}
