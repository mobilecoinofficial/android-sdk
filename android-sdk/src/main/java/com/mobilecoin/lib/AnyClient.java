// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;

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
    private final Uri serviceUri;
    private final ServiceAPIManager apiManager;
    private final ClientConfig.Service serviceConfig;
    private ManagedChannel managedChannel;

    /**
     * Creates and initializes an instance of {@link com.mobilecoin.lib.AttestedClient}
     *
     * @param uri a complete {@link Uri} of the service including port.
     */
    protected AnyClient(@NonNull Uri uri, @NonNull ClientConfig.Service serviceConfig) {
        this.serviceUri = uri;
        this.serviceConfig = serviceConfig;
        this.apiManager = new ServiceAPIManager();
    }

    @NonNull
    final ServiceAPIManager getAPIManager() {
        return apiManager;
    }

    @NonNull
    final Uri getServiceUri() {
        return serviceUri;
    }

    @NonNull
    final ClientConfig.Service getServiceConfig() {
        return serviceConfig;
    }

    /**
     * Subclasses must use this method to get access to a managed channel. The connection
     * will be
     * automatically attested during this call
     *
     * @return {@link ManagedChannel}
     */
    @NonNull
    protected synchronized ManagedChannel getManagedChannel()
            throws AttestationException, NetworkException {
        try {
            if (null == managedChannel) {
                Logger.i(TAG, "Managed channel does not exist: creating one");
                OkHttpChannelBuilder managedChannelBuilder = OkHttpChannelBuilder
                        .forAddress(
                                serviceUri.getHost(),
                                serviceUri.getPort()
                        )
                        .useTransportSecurity();
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
            NetworkException exception = new NetworkException(500, "Unable to create managed channel", ex);
            Util.logException(TAG, exception);
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
        apiManager.reset();
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
    }
}
