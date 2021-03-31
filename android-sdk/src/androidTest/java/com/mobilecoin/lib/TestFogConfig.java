package com.mobilecoin.lib;

import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.util.Hex;

import java.security.cert.X509Certificate;
import java.util.Set;

public class TestFogConfig {
    public static final short SECURITY_VERSION = 1;
    public static final short CONSENSUS_PRODUCT_ID = 1;
    public static final short FOG_LEDGER_PRODUCT_ID = 2;
    public static final short FOG_VIEW_PRODUCT_ID = 3;
    public static final short FOG_REPORT_PRODUCT_ID = 4;

    private final Uri fogUri;
    private final Uri consensusUri;
    private final String username;
    private final String password;
    private final ClientConfig clientConfig;
    private final byte[] fogAuthoritySpki;
    private final String fogReportId;

    private static final String TEST_DEV_USERNAME = "REPLACE_TEST_DEV_USER_STRING";
    private static final String TEST_DEV_PASSWORD = "REPLACE_TEST_DEV_PASSWORD_STRING";

    private static final byte[] mobiledevFogAuthoritySpki = Base64.decode("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAxABZ75QZv9uH9/E823VTTmpWiOiehoqksZMqsDARqYdDexAQb1Y+qyT6Hlp5QMUHQlkomFKLnhe/0+wxZ1/uTqnhy2FRhrlclpOvczT10Smcx9RkKACpxCW095MWxeFwtMmLpqkXfl4KeMptxdHRASHuLlKL+FXwOqKw3J2nw5q2DpBsg1ONkdW4m55ZFdimX3M7T/Wur5WlB+ntBpKFU/5T+rdD3OUm/tExbYk7C58XmYW08TnFR9JOMekFZMmTfl5d1ee3koyzz225QfNEupUJDVMXcg4whp826arxQIXrM2DfgwZnxFqS617dNsOPNjIoAYSEFPczYTw9WHR7O3UISnYwYvCsXxGwLZLXFkgUBM5GKItvEHDbUh3C7ZjyM51A04EJg47G3nI1A6q9EVnmwGaZFxq8bJAzosn5zaSrbUA25hRff25C4BYNjydBI133PjSflLaGjnJYPruLO4XpzB3wszqKm3tiWN39sgC4sMWZfSlxlWox3SzY2XVl8Q9RqMO8LMUPNhwmTfpEXDW5+NqH+vMiH9UmnsiEwybFche4sE23NJTeO2Xytt55VfoD2Gidte/Sqt5AJUPu6nfK8QloOCZ1N99MrpWpcZPHittqaYHZ5lWXHKthp/im672hXPl8bNxMUoREqomZdD9mdj/P6w9zFeTkr7P9XQUCAwEAAQ==", Base64.DEFAULT);
    public static final byte[] alphaFogAuthoritySpki = Base64.decode("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAyFOockvCEc9TcO1NvsiUfFVzvtDsR64UIRRUl3tBM2Bh8KBA932/Up86RtgJVnbslxuUCrTJZCV4dgd5hAo/mzuJOy9lAGxUTpwWWG0zZJdpt8HJRVLX76CBpWrWEt7JMoEmduvsCR8q7WkSNgT0iIoSXgT/hfWnJ8KGZkN4WBzzTH7hPrAcxPrzMI7TwHqUFfmOX7/gc+bDV5ZyRORrpuu+OR2BVObkocgFJLGmcz7KRuN7/dYtdYFpiKearGvbYqBrEjeo/15chI0Bu/9oQkjPBtkvMBYjyJPrD7oPP67i0ZfqV6xCj4nWwAD3bVjVqsw9cCBHgaykW8ArFFa0VCMdLy7UymYU5SQsfXrw/mHpr27Pp2Z0/7wpuFgJHL+0ARU48OiUzkXSHX+sBLov9X6f9tsh4q/ZRorXhcJi7FnUoagBxewvlfwQfcnLX3hp1wqoRFC4w1DC+ki93vIHUqHkNnayRsf1n48fSu5DwaFfNvejap7HCDIOpCCJmRVR8mVuxi6jgjOUa4Vhb/GCzxfNIn5ZYym1RuoE0TsFO+TPMzjed3tQvG7KemGFz3pQIryb43SbG7Q+EOzIigxYDytzcxOO5Jx7r9i+amQEiIcjBICwyFoEUlVJTgSpqBZGNpznoQ4I2m+uJzM+wMFsinTZN3mp4FU5UHjQsHKG+ZMCAwEAAQ==", Base64.DEFAULT);

    private TestFogConfig(@NonNull Uri fogUri, @NonNull Uri consensusUri, @NonNull String username,
                          @NonNull String password, @NonNull ClientConfig clientConfig,
                          @NonNull byte[] fogAuthoritySpki, @NonNull String fogReportId) {
        this.fogUri = fogUri;
        this.consensusUri = consensusUri;
        this.username = username;
        this.password = password;
        this.clientConfig = clientConfig;
        this.fogAuthoritySpki = fogAuthoritySpki;
        this.fogReportId = fogReportId;
    }

    @NonNull
    public Uri getFogUri() {
        return fogUri;
    }

    @NonNull
    public Uri getConsensusUri() {
        return consensusUri;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public byte[] getFogAuthoritySpki() {
        return fogAuthoritySpki;
    }

    @NonNull
    public String getFogReportId() {
        return fogReportId;
    }

    @NonNull
    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    @NonNull
    public static TestFogConfig getFogConfig(Environment.TestEnvironment testEnvironment) {
        final Uri fogUri = Uri.parse(String.format(
                "fog://fog.%s.mobilecoin.com",
                testEnvironment.getName()
        ));
        final Uri consensusUri = Uri.parse(String.format(
                "mc://consensus.%s.mobilecoin.com",
                testEnvironment.getName()
        ));

        switch (testEnvironment) {
            case MOBILE_DEV:
                return new TestFogConfig(fogUri, consensusUri, TEST_DEV_USERNAME,
                        TEST_DEV_PASSWORD, getDevClientConfig(),
                        mobiledevFogAuthoritySpki, "");
            case ALPHA:
                return new TestFogConfig(fogUri, consensusUri, TEST_DEV_USERNAME,
                        TEST_DEV_PASSWORD, getDevClientConfig(),
                        alphaFogAuthoritySpki, "");

        }
        throw new UnsupportedOperationException("Requested config does not exist");
    }

    @NonNull
    private static ClientConfig getDevClientConfig() {
        try {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.fogView = new ClientConfig.Service()
                    .withVerifier((new Verifier())
                            .withMrSigner(Hex.toByteArray(
                                    "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411"),
                                    FOG_VIEW_PRODUCT_ID, SECURITY_VERSION,
                                    null,
                                    new String[]{"INTEL-SA-00334"}))
                    .withTrustRoots(getDevTrustRoots());
            clientConfig.fogLedger = new ClientConfig.Service()
                    .withVerifier((new Verifier())
                            .withMrSigner(Hex.toByteArray(
                                    "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411"),
                                    FOG_LEDGER_PRODUCT_ID, SECURITY_VERSION,
                                    null,
                                    new String[]{"INTEL-SA-00334"}))
                    .withTrustRoots(getDevTrustRoots());
            clientConfig.consensus = new ClientConfig.Service()
                    .withVerifier((new Verifier())
                            .withMrSigner(Hex.toByteArray(
                                    "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411"),
                                    CONSENSUS_PRODUCT_ID, SECURITY_VERSION,
                                    null,
                                    new String[]{"INTEL-SA-00334"}))
                    .withTrustRoots(getDevTrustRoots());
            clientConfig.report = new ClientConfig.Service()
                    .withVerifier((new Verifier())
                            .withMrSigner(Hex.toByteArray(
                                    "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411"),
                                    FOG_REPORT_PRODUCT_ID, SECURITY_VERSION,
                                    null,
                                    new String[]{"INTEL-SA-00334"})
                    );
            return clientConfig;
        } catch (AttestationException ex) {
            throw new IllegalStateException("BUG: Unreachable code");
        }
    }

    @NonNull
    private static Set<X509Certificate> getDevTrustRoots() {
        String trustRootBase64String = "MIIDSjCCAjKgAwIBAgIQRK+wgNajJ7qJMDmGLvhAazANBgkqhkiG9w0BAQUFADA/MSQwIgYDVQQKExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMTDkRTVCBSb290IENBIFgzMB4XDTAwMDkzMDIxMTIxOVoXDTIxMDkzMDE0MDExNVowPzEkMCIGA1UEChMbRGlnaXRhbCBTaWduYXR1cmUgVHJ1c3QgQ28uMRcwFQYDVQQDEw5EU1QgUm9vdCBDQSBYMzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAN+v6ZdQCINXtMxiZfaQguzH0yxrMMpb7NnDfcdAwRgUi+DoM3ZJKuM/IUmTrE4Orz5Iy2Xu/NMhD2XSKtkyj4zl93ewEnu1lcCJo6m67XMuegwGMoOifooUMM0RoOEqOLl5CjH9UL2AZd+3UWODyOKIYepLYYHsUmu5ouJLGiifSKOeDNoJjj4XLh7dIN9bxiqKqy69cK3FCxolkHRyxXtqqzTWMIn/5WgTe1QLyNau7Fqckh49ZLOMxt+/yUFw7BZy1SbsOFU5Q9D8/RhcQPGX69Wam40dutolucbY38EVAjqr2m7xPi71XAicPNaDaeQQmxkqtilX4+U9m5/wAl0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAQYwHQYDVR0OBBYEFMSnsaR7LHH62+FLkHX/xBVghYkQMA0GCSqGSIb3DQEBBQUAA4IBAQCjGiybFwBcqR7uKGY3Or+Dxz9LwwmglSBd49lZRNI+DT69ikugdB/OEIKcdBodfpga3csTS7MgROSR6cz8faXbauX+5v3gTt23ADq1cEmv8uXrAvHRAosZy5Q6XkjEGB5YGV8eAlrwDPGxrancWYaLbumR9YbK+rlmM6pZW87ipxZzR8srzJmwN0jP41ZL9c8PDHIyh8bwRLtTcm1D9SZImlJnt1ir/md2cXjbDaJWFBM5JDGFoqgCWjBH4d1QB7wCCZAA62RjYJsWvIjJEubSfZGL+T0yjWW06XyxV3bqxbYoOb8VZRzI9neWagqNdwvYkQsEjgfbKbYK7p2CNTUQ";
        byte[] trustRootBytes = Base64.decode(trustRootBase64String, Base64.DEFAULT);
        return Util.makeCertificatesFromData(trustRootBytes);
    }
}
