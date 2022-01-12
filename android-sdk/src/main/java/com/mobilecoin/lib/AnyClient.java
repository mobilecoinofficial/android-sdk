// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.ClientConfig.Service;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.grpc.GRPCServiceAPIManager;
import com.mobilecoin.lib.network.services.http.RestServiceAPIManager;
import com.mobilecoin.lib.network.services.ServiceAPIManager;
import com.mobilecoin.lib.network.services.transport.Transport;
import com.mobilecoin.lib.network.uri.MobileCoinUri;

class AnyClient extends Native {
    private final static String TAG = AttestedClient.class.getName();

    private final LoadBalancer loadBalancer;
    private final ClientConfig.Service serviceConfig;
    private ServiceAPIManager serviceAPIManager;
    private Transport networkTransport;
    private TransportProtocol transportProtocol;
    private MobileCoinUri currentServiceUri;

    /**
     * Creates and initializes an instance of {@link AttestedClient}
     *
     * @param loadBalancer a complete {@link Uri} of the service including port.
     */
    protected AnyClient(@NonNull LoadBalancer loadBalancer,
                        @NonNull Service serviceConfig,
                        @NonNull TransportProtocol transportProtocol) {
        this.loadBalancer = loadBalancer;
        this.serviceConfig = serviceConfig;
        this.setTransportProtocol(transportProtocol);
    }

    @NonNull
    final ServiceAPIManager getAPIManager() {
        return this.serviceAPIManager;
    }

    synchronized void setTransportProtocol(@NonNull TransportProtocol protocol) {
        this.transportProtocol = protocol;
        this.resetNetworkTransport();
        switch(protocol.getTransportType()) {
            case GRPC:
                this.serviceAPIManager = new GRPCServiceAPIManager();
                break;
            case HTTP:
                this.serviceAPIManager = new RestServiceAPIManager();
                break;
            default:
                throw new UnsupportedOperationException("Unsupported");
        }
    }

    @NonNull
    synchronized Transport getNetworkTransport() throws NetworkException, AttestationException {
        if(null == this.networkTransport) {
            this.currentServiceUri = getNextServiceUri();
            this.networkTransport = Transport.forConfig(this.transportProtocol, this.currentServiceUri, this.serviceConfig);
        }
        return networkTransport;
    }

    protected synchronized void resetNetworkTransport() {
       networkTransport = null;
    }

    @NonNull
    private MobileCoinUri getNextServiceUri() {
        return loadBalancer.getNextServiceUri();
    }

    @NonNull
    final ClientConfig.Service getServiceConfig() {
        return serviceConfig;
    }

    protected MobileCoinUri getCurrentServiceUri() {
        return currentServiceUri;
    }

    /**
     * Authorize requests using the provided credentials.
     * <p>
     * Credentials are encoded and attached as an HTTP header field in the form of
     * Authorization:
     * Basic <credentials>, where credentials is the Base64 encoding of ID and password joined by a
     * single colon :
     */
    public void setAuthorization(
            @NonNull String username,
            @NonNull String password
    ) {
        Logger.i(TAG, "Set API authorization");
        getAPIManager().setAuthorization(
                username,
                password
        );
    }

    /**
     * Try to gracefully shut down the managed connection All existing requests will be completed
     * but no new requests accepted
     */
    void shutdown() {
        Logger.i(TAG, "Client shutdown");
        if(null != this.networkTransport) {
            this.networkTransport.shutdown();
            this.networkTransport = null;
        }
    }

}
