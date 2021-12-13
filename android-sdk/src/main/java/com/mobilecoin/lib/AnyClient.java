// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.ClientConfig.Service;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.GRPCServiceAPIManager;
import com.mobilecoin.lib.network.services.RestServiceAPIManager;
import com.mobilecoin.lib.network.services.ServiceAPIManager;
import com.mobilecoin.lib.network.services.http.Requester;
import com.mobilecoin.lib.network.services.http.clients.RestClient;
import com.mobilecoin.lib.network.services.transport.Transport;
import com.mobilecoin.lib.network.uri.MobileCoinUri;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;

class AnyClient extends Native {
    private final static String TAG = AttestedClient.class.getName();
    // How long to wait for the managed connection to gracefully shutdown in milliseconds
    private final static long MANAGED_CONNECTION_SHUTDOWN_TIME_LIMIT = 1000;
    private final LoadBalancer loadBalancer;
    private final ClientConfig.Service serviceConfig;
    private ServiceAPIManager grpcApiManager;
    private ServiceAPIManager restApiManager;
    private ManagedChannel managedChannel;
    private RestClient restClient;
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
        switch (transportProtocol.getTransportType()) {
            case HTTP:
                return restApiManager;
            case GRPC:
                return grpcApiManager;
            default:
                throw new UnsupportedOperationException("Unimplemented");
        }
    }

    synchronized void setTransportProtocol(@NonNull TransportProtocol protocol) {
        this.transportProtocol = protocol;
        this.networkTransport = null;
        switch(protocol.getTransportType()) {
            case GRPC:
                if(this.grpcApiManager == null) {
                    this.grpcApiManager = new GRPCServiceAPIManager();
                }
                break;
            case HTTP:
                if(this.restApiManager == null) {
                    this.restApiManager = new RestServiceAPIManager();
                }
                break;
        }
    }

    @NonNull
    synchronized Transport getNetworkTransport() throws NetworkException, AttestationException {
        if (null == networkTransport) {
            switch (transportProtocol.getTransportType()) {
                case GRPC: {
                    networkTransport = Transport.fromManagedChannel(getManagedChannel());
                }
                break;
                case HTTP: {
                    networkTransport = Transport.fromRestClient(getRestClient());
                }
                break;
                default:
                    throw new UnsupportedOperationException("Unimplemented");
            }
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

    @NonNull
    protected synchronized RestClient getRestClient() throws NetworkException,
            AttestationException {
        if (null == restClient) {
            Requester httpRequester = transportProtocol.getHttpRequester();
            if (null == httpRequester) {
                throw new IllegalArgumentException("HttpRequester was not properly set");
            }
            currentServiceUri = getNextServiceUri();
            restClient = new RestClient(currentServiceUri.getUri(), httpRequester);
        }
        return restClient;
    }

    @NonNull
    protected synchronized ManagedChannel getManagedChannel()
            throws AttestationException, NetworkException {
        try {
            if (null == managedChannel) {
                Logger.i(TAG, "Managed channel does not exist: creating one");
                currentServiceUri = getNextServiceUri();
                OkHttpChannelBuilder managedChannelBuilder = OkHttpChannelBuilder
                        .forAddress(
                                currentServiceUri.getUri().getHost(),
                                currentServiceUri.getUri().getPort()
                        );
                if (currentServiceUri.isTlsEnabled()) {
                    managedChannelBuilder.useTransportSecurity();
                } else {
                    managedChannelBuilder.usePlaintext();
                }
                Set<X509Certificate> trustRoots = getServiceConfig().getTrustRoots();
                if (trustRoots != null && trustRoots.size() > 0) {
                    KeyStore caKeyStore = getTrustRootsKeyStore(trustRoots);
                    SSLSocketFactory sslSocketFactory = getTrustedSSLSocketFactory(caKeyStore);
                    managedChannelBuilder.sslSocketFactory(sslSocketFactory);
                }
                managedChannel = managedChannelBuilder.build();
            } else {
                Logger.i(TAG, "Managed channel exists: using existing");
            }
        } catch (Exception ex) {
            NetworkException exception = new NetworkException(NetworkResult.UNKNOWN
                    .withDescription("Unable to create managed channel")
                    .withCause(ex));
            String message = exception.getMessage();
            if (null == message) {
                message = "";
            }
            Logger.w(TAG, message, exception);
        }
        return managedChannel;
    }

    /**
     * Create a new SSLSocketFactory
     *
     * @param trustRootsKeyStore keystore containing the trust anchors
     */
    @NonNull
    static SSLSocketFactory getTrustedSSLSocketFactory(@NonNull KeyStore trustRootsKeyStore)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        // initialize trust manager from certs keystore
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustRootsKeyStore);

        // initialize SSL context from trust manager factory
        SSLContext context = SSLContext.getInstance("TLS");
        if (context == null) {
            throw new NoSuchAlgorithmException("TLS is not supported");
        }
        context.init(null, tmf.getTrustManagers(), new SecureRandom());

        // return socket factory from the SSL context
        return context.getSocketFactory();
    }

    /**
     * Load CA anchors into a KeyStore
     */
    @NonNull
    static KeyStore getTrustRootsKeyStore(@NonNull Set<X509Certificate> trustRoots)
            throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        for (X509Certificate trustRoot : trustRoots) {
            ks.setCertificateEntry(trustRoot.toString(), trustRoot);
        }
        return ks;
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
        if (null != managedChannel) {
            try {
                managedChannel.shutdown();
                Logger.i(TAG, "Shutting down the managed channel, awaiting for termination...");
                managedChannel.awaitTermination(
                        MANAGED_CONNECTION_SHUTDOWN_TIME_LIMIT,
                        TimeUnit.MILLISECONDS
                );
                Logger.i(TAG, "The managed channel has been shut down");
            } catch (InterruptedException ignored) { /* */ }
            managedChannel = null;
        }

        restClient = null;
    }

}
