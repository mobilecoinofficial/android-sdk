// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.log.LogAdapter;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.util.Hex;

import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Set;

/**
 * This class provides configuration for MobileCoinClient
 */
public final class ClientConfig {
    private final static String TAG = ClientConfig.class.getName();
    public static final short SECURITY_VERSION = 1;
    public static final short FOG_CONSENSUS_PRODUCT_ID = 1;
    public static final short FOG_LEDGER_PRODUCT_ID = 2;
    public static final short FOG_VIEW_PRODUCT_ID = 3;
    public static final short FOG_REPORT_PRODUCT_ID = 4;

    public Service report;
    public Service fogView;
    public Service fogLedger;
    public Service consensus;
    public StorageAdapter storageAdapter;
    public LogAdapter logAdapter;

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
        Logger.i(TAG, "Getting default config");
        // TODO: switch to prod before release
        return devConfig();
    }

    @NonNull
    static ClientConfig devConfig() throws IllegalStateException {
        try {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.fogView = new Service()
                    .withVerifier((new Verifier())
                            .withMrEnclave(Hex.toByteArray(
                                    "67c027eb783c737530350adae3b9c1468c7f25e81702b9b00e7c1bac11f019a8"),
                                    null,
                                    new String[]{"INTEL-SA-00334"})
                            .withMrSigner(Hex.toByteArray(
                                    "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411"),
                                    FOG_VIEW_PRODUCT_ID, SECURITY_VERSION,
                                    null,
                                    new String[]{"INTEL-SA-00334"}))
                    .withTrustRoots(getDevTrustRoots());
            clientConfig.fogLedger = new Service()
                    .withVerifier((new Verifier())
                            .withMrEnclave(Hex.toByteArray(
                                    "8c893c46afd534985f483647892d0879a3dc86f7209c13fb789d9de3998e1868"),
                                    null,
                                    new String[]{"INTEL-SA-00334"})
                            .withMrSigner(Hex.toByteArray(
                                    "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411"),
                                    FOG_LEDGER_PRODUCT_ID, SECURITY_VERSION,
                                    null,
                                    new String[]{"INTEL-SA-00334"}))
                    .withTrustRoots(getDevTrustRoots());
            clientConfig.consensus = new Service()
                    .withVerifier((new Verifier())
                            .withMrEnclave(Hex.toByteArray(
                                    "61a662c8fea7b4d2bd30cc450c64eff1c42ef1a15324c438cfa5b87819e2c5b7"),
                                    null,
                                    new String[]{"INTEL-SA-00334"})
                            .withMrSigner(Hex.toByteArray(
                                    "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411"),
                                    FOG_CONSENSUS_PRODUCT_ID, SECURITY_VERSION,
                                    null,
                                    new String[]{"INTEL-SA-00334"}))
                    .withTrustRoots(getDevTrustRoots());
            clientConfig.report = new Service()
                    .withVerifier((new Verifier())
                            .withMrSigner(Hex.toByteArray(
                                    "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411"),
                                    FOG_REPORT_PRODUCT_ID, SECURITY_VERSION,
                                    null,
                                    new String[]{"INTEL-SA-00334"})
                    );
            clientConfig.storageAdapter = new DefaultStorageAdapter();
            return clientConfig;
        } catch (AttestationException ex) {
            throw new IllegalStateException("BUG: Unreachable code");
        }
    }

    @NonNull
    static Set<X509Certificate> getDevTrustRoots() {
        String trustRootBase64String =
                "MIIDSjCCAjKgAwIBAgIQRK+wgNajJ7qJMDmGLvhAazANBgkqhkiG9w0BAQUFADA/MSQwIgYDVQQKExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMTDkRTVCBSb290IENBIFgzMB4XDTAwMDkzMDIxMTIxOVoXDTIxMDkzMDE0MDExNVowPzEkMCIGA1UEChMbRGlnaXRhbCBTaWduYXR1cmUgVHJ1c3QgQ28uMRcwFQYDVQQDEw5EU1QgUm9vdCBDQSBYMzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAN+v6ZdQCINXtMxiZfaQguzH0yxrMMpb7NnDfcdAwRgUi+DoM3ZJKuM/IUmTrE4Orz5Iy2Xu/NMhD2XSKtkyj4zl93ewEnu1lcCJo6m67XMuegwGMoOifooUMM0RoOEqOLl5CjH9UL2AZd+3UWODyOKIYepLYYHsUmu5ouJLGiifSKOeDNoJjj4XLh7dIN9bxiqKqy69cK3FCxolkHRyxXtqqzTWMIn/5WgTe1QLyNau7Fqckh49ZLOMxt+/yUFw7BZy1SbsOFU5Q9D8/RhcQPGX69Wam40dutolucbY38EVAjqr2m7xPi71XAicPNaDaeQQmxkqtilX4+U9m5/wAl0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAQYwHQYDVR0OBBYEFMSnsaR7LHH62+FLkHX/xBVghYkQMA0GCSqGSIb3DQEBBQUAA4IBAQCjGiybFwBcqR7uKGY3Or+Dxz9LwwmglSBd49lZRNI+DT69ikugdB/OEIKcdBodfpga3csTS7MgROSR6cz8faXbauX+5v3gTt23ADq1cEmv8uXrAvHRAosZy5Q6XkjEGB5YGV8eAlrwDPGxrancWYaLbumR9YbK+rlmM6pZW87ipxZzR8srzJmwN0jP41ZL9c8PDHIyh8bwRLtTcm1D9SZImlJnt1ir/md2cXjbDaJWFBM5JDGFoqgCWjBH4d1QB7wCCZAA62RjYJsWvIjJEubSfZGL+T0yjWW06XyxV3bqxbYoOb8VZRzI9neWagqNdwvYkQsEjgfbKbYK7p2CNTUQ";
        byte[] trustRootBytes = Base64.decode(trustRootBase64String, Base64.DEFAULT);
        return Util.makeCertificatesFromData(trustRootBytes);
    }
}

