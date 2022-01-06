package com.mobilecoin.lib.network.services.transport;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.ClientConfig;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.http.clients.RestClient;
import com.mobilecoin.lib.network.uri.MobileCoinUri;

public abstract class Transport {

    public synchronized static Transport forConfig(@NonNull TransportProtocol transportProtocol,
                                                   @NonNull MobileCoinUri currentUri,
                                                   @NonNull ClientConfig.Service serviceConfig)
                                                throws NetworkException {
        switch(transportProtocol.getTransportType()) {
            case GRPC:
                return new GRPCTransport(currentUri, serviceConfig);
            case HTTP:
                return new RestTransport(new RestClient(currentUri.getUri(), transportProtocol.getHttpRequester()));
            default:
                throw new UnsupportedOperationException("Unsupported protocol");
        }
    }

    @NonNull
    public abstract TransportType getTransportType();

    public abstract void shutdown();

    public enum TransportType {
        GRPC,
        HTTP
    }
}
