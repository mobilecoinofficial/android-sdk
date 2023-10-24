// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.log.LogAdapter;

import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Set;

/**
 * This class provides configuration for MobileCoinClient
 */
public final class ClientConfig {
    public Service report;
    public Service fogView;
    public Service fogLedger;
    public Service consensus;
    public StorageAdapter storageAdapter;
    public LogAdapter logAdapter;
    // default minimum fee cache TTL is 30 minutes
    public long minimumFeeCacheTTLms = 1800000L;

    /**
     * Service Configuration
     */
    public static final class Service {
        private TrustedIdentities trustedIdentities;
        private Set<X509Certificate> trustRoots;

        /***
         * Sets TrustedIdentities for this {@link Service}
         * @param trustedIdentities the TrustedIdentities to set
         * @return this {@link Service} with TrustedIdentities set
         */
        @NonNull
        public Service withTrustedIdentities(@NonNull TrustedIdentities trustedIdentities) {
            this.trustedIdentities = trustedIdentities;
            return this;
        }

        /***
         * Sets the TrustedIdentities from a deprecated {@link Verifier}
         * @param verifier
         * @return this Service with TrustedIdentities set
         * @deprecated Deprecated as of 6.0.0. Use {@link Service#withTrustedIdentities(TrustedIdentities)} instead
         */
        @Deprecated
        @NonNull
        public Service withVerifier(@NonNull Verifier verifier) {
            this.trustedIdentities = verifier.getTrustedIdentities();
            return this;
        }

        /**
         * Pin Trust Root certificates for the service network connections
         */
        @NonNull
        public Service withTrustRoots(@NonNull Set<X509Certificate> trustRoots) {
            this.trustRoots = trustRoots;
            return this;
        }

        /**
         * Get current attestation verifier
         */
        @NonNull
        public TrustedIdentities getTrustedIdentities() {
            return Objects.requireNonNull(trustedIdentities);
        }

        /**
         * Get pinned Trust Root certificates
         */
        @Nullable
        public Set<X509Certificate> getTrustRoots() {
            return trustRoots;
        }
    }

    @NonNull
    public static ClientConfig defaultConfig() {
        try {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.fogView = new Service()
                    .withTrustedIdentities((new TrustedIdentities()));
            clientConfig.fogLedger = new Service()
                    .withTrustedIdentities((new TrustedIdentities()));
            clientConfig.consensus = new Service()
                    .withTrustedIdentities((new TrustedIdentities()));
            clientConfig.report = new Service()
                    .withTrustedIdentities((new TrustedIdentities()));
            return clientConfig;
        } catch (AttestationException attestationException) {
            throw new IllegalStateException("BUG: Unreachable code");
        }
    }
}

