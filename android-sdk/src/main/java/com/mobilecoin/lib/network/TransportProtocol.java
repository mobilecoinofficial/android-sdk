package com.mobilecoin.lib.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.network.services.http.Requester.Requester;
import com.mobilecoin.lib.network.services.transport.Transport;

/**
 * <pre>
 * TransportProtocol class is responsible for specifying which communication protocol to use
 * between a client & server. GRPC and HTTP(s) protocols are currently supported.
 * </pre>
 */
public class TransportProtocol {
    private final Transport.TransportType transportType;
    private final Requester httpRequester;

    private TransportProtocol(@NonNull Transport.TransportType transportType,
                              @Nullable Requester requester) {
        this.transportType = transportType;
        this.httpRequester = requester;
    }

    /**
     * Create new protocol for GRPC transport
     */
    @NonNull
    public static TransportProtocol forGRPC() {
        return new TransportProtocol(Transport.TransportType.GRPC, null);
    }

    /**
     * Create new protocol for HTTP transport
     */
    @NonNull
    public static TransportProtocol forHTTP(@NonNull Requester httpRequester) {
        return new TransportProtocol(Transport.TransportType.HTTP, httpRequester);
    }

    @Nullable
    public Requester getHttpRequester() {
        return httpRequester;
    }

    @NonNull
    public Transport.TransportType getTransportType() {
        return transportType;
    }
}
