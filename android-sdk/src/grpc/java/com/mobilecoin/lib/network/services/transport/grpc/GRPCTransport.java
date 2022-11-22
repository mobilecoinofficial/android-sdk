package com.mobilecoin.lib.network.services.transport.grpc;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.ClientConfig;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.NetworkResult;
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

public class GRPCTransport extends Transport {
    private static final String TAG = GRPCTransport.class.getName();
    // How long to wait for the managed connection to gracefully shutdown in milliseconds
    private final static long MANAGED_CONNECTION_SHUTDOWN_TIME_LIMIT = 1000;

    private final ManagedChannel managedChannel;

    public GRPCTransport(@NonNull MobileCoinUri uri, @NonNull ClientConfig.Service serviceConfiguration) throws NetworkException {
        try {
            Logger.i(TAG, "Managed channel does not exist: creating one");
            OkHttpChannelBuilder managedChannelBuilder = OkHttpChannelBuilder
                    .forAddress(
                            uri.getUri().getHost(),
                            uri.getUri().getPort()
                    );
            Set<X509Certificate> trustRoots = serviceConfiguration.getTrustRoots();
            if (trustRoots != null && trustRoots.size() > 0) {
                KeyStore caKeyStore = getTrustRootsKeyStore(trustRoots);
                SSLSocketFactory sslSocketFactory = getTrustedSSLSocketFactory(caKeyStore);
                managedChannelBuilder.sslSocketFactory(sslSocketFactory);
            }
            if (uri.isTlsEnabled()) {
                managedChannelBuilder.useTransportSecurity();
            } else {
                managedChannelBuilder.usePlaintext();
            }
            this.managedChannel = managedChannelBuilder.build();
        } catch (Exception ex) {
            NetworkException exception = new NetworkException(NetworkResult.UNKNOWN
                    .withDescription("Unable to create managed channel")
                    .withCause(ex));
            String message = exception.getMessage();
            if (null == message) {
                message = "";
            }
            Logger.w(TAG, message, exception);
            throw(exception);
        }
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

    /**
     * Create a new SSLSocketFactory
     *
     * @param trustRootsKeyStore keystore containing the trust anchors
     */
    @NonNull
    private static SSLSocketFactory getTrustedSSLSocketFactory(@NonNull KeyStore trustRootsKeyStore)
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
    private static KeyStore getTrustRootsKeyStore(@NonNull Set<X509Certificate> trustRoots)
            throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        for (X509Certificate trustRoot : trustRoots) {
            ks.setCertificateEntry(trustRoot.toString(), trustRoot);
        }
        return ks;
    }

    @Override
    public void shutdown() {
        try {
            this.managedChannel.shutdown();
            Logger.i(TAG, "Shutting down the managed channel, awaiting for termination...");
            managedChannel.awaitTermination(
                    MANAGED_CONNECTION_SHUTDOWN_TIME_LIMIT,
                    TimeUnit.MILLISECONDS
            );
            Logger.i(TAG, "The managed channel has been shut down");
        } catch(InterruptedException ignored) {/*  */}
    }

}
