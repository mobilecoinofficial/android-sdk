// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.log.LogAdapter;

import java.security.cert.X509Certificate;
import java.time.Duration;
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
    public Duration minimumFeeCacheTTL = Duration.ofMinutes(30);

    /**
     * Service Configuration
     */
    public static final class Service {
        private Verifier verifier;
        private Set<X509Certificate> trustRoots;

        /**
         * Set attestation Verifier
         */
        @NonNull
        public Service withVerifier(@NonNull Verifier verifier) {
            this.verifier = verifier;
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
        public Verifier getVerifier() {
            return Objects.requireNonNull(verifier);
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
                    .withVerifier((new Verifier()));
            clientConfig.fogLedger = new Service()
                    .withVerifier((new Verifier()));
            clientConfig.consensus = new Service()
                    .withVerifier((new Verifier()));
            clientConfig.report = new Service()
                    .withVerifier((new Verifier()));
            return clientConfig;
        } catch (AttestationException attestationException) {
            throw new IllegalStateException("BUG: Unreachable code");
        }
    }
}

