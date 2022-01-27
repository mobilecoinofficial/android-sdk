package com.mobilecoin.lib.network.services;

import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.grpc.GRPCServiceAPIManager;
import com.mobilecoin.lib.network.services.http.RestServiceAPIManager;

public final class APIManagerFactory {

    public static ServiceAPIManager forProtocol(TransportProtocol protocol) {
        switch (protocol.getTransportType()) {
            case GRPC:
                return new GRPCServiceAPIManager();
            case HTTP:
                return new RestServiceAPIManager();
            default:
                throw new UnsupportedOperationException("Unsupported");
        }
    }

}
