package com.mobilecoin.lib;

import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.util.Hex;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TestFogConfig {
    public static final short SECURITY_VERSION = 1;
    public static final short CONSENSUS_PRODUCT_ID = 1;
    public static final short FOG_LEDGER_PRODUCT_ID = 2;
    public static final short FOG_VIEW_PRODUCT_ID = 3;
    public static final short FOG_REPORT_PRODUCT_ID = 4;

    private final Uri fogUri;
    private final List<Uri> consensusUris;
    private final String username;
    private final String password;
    private final ClientConfig clientConfig;
    private final byte[] fogAuthoritySpki;
    private final String fogReportId;
    private final TransportProtocol transportProtocol;

    private static final String TEST_USERNAME = "REPLACE_TEST_DEV_USER_STRING";
    private static final String TEST_PASSWORD = "REPLACE_TEST_DEV_PASSWORD_STRING";

    private static final byte[] mobiledevFogAuthoritySpki = Base64.decode("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAxABZ75QZv9uH9/E823VTTmpWiOiehoqksZMqsDARqYdDexAQb1Y+qyT6Hlp5QMUHQlkomFKLnhe/0+wxZ1/uTqnhy2FRhrlclpOvczT10Smcx9RkKACpxCW095MWxeFwtMmLpqkXfl4KeMptxdHRASHuLlKL+FXwOqKw3J2nw5q2DpBsg1ONkdW4m55ZFdimX3M7T/Wur5WlB+ntBpKFU/5T+rdD3OUm/tExbYk7C58XmYW08TnFR9JOMekFZMmTfl5d1ee3koyzz225QfNEupUJDVMXcg4whp826arxQIXrM2DfgwZnxFqS617dNsOPNjIoAYSEFPczYTw9WHR7O3UISnYwYvCsXxGwLZLXFkgUBM5GKItvEHDbUh3C7ZjyM51A04EJg47G3nI1A6q9EVnmwGaZFxq8bJAzosn5zaSrbUA25hRff25C4BYNjydBI133PjSflLaGjnJYPruLO4XpzB3wszqKm3tiWN39sgC4sMWZfSlxlWox3SzY2XVl8Q9RqMO8LMUPNhwmTfpEXDW5+NqH+vMiH9UmnsiEwybFche4sE23NJTeO2Xytt55VfoD2Gidte/Sqt5AJUPu6nfK8QloOCZ1N99MrpWpcZPHittqaYHZ5lWXHKthp/im672hXPl8bNxMUoREqomZdD9mdj/P6w9zFeTkr7P9XQUCAwEAAQ==", Base64.DEFAULT);
    private static final byte[] alphaFogAuthoritySpki = Hex.toByteArray("30820222300d06092a864886f70d01010105000382020f003082020a0282020100c853a8724bc211cf5370ed4dbec8947c5573bed0ec47ae14211454977b41336061f0a040f77dbf529f3a46d8095676ec971b940ab4c9642578760779840a3f9b3b893b2f65006c544e9c16586d33649769b7c1c94552d7efa081a56ad612dec932812676ebec091f2aed69123604f4888a125e04ff85f5a727c286664378581cf34c7ee13eb01cc4faf3308ed3c07a9415f98e5fbfe073e6c357967244e46ba6ebbe391d8154e6e4a1c80524b1a6733eca46e37bfdd62d75816988a79aac6bdb62a06b1237a8ff5e5c848d01bbff684248cf06d92f301623c893eb0fba0f3faee2d197ea57ac428f89d6c000f76d58d5aacc3d70204781aca45bc02b1456b454231d2f2ed4ca6614e5242c7d7af0fe61e9af6ecfa76674ffbc29b858091cbfb4011538f0e894ce45d21d7fac04ba2ff57e9ff6db21e2afd9468ad785c262ec59d4a1a801c5ec2f95fc107dc9cb5f7869d70aa84450b8c350c2fa48bddef20752a1e43676b246c7f59f8f1f4aee43c1a15f36f7a36a9ec708320ea42089991551f2656ec62ea38233946b85616ff182cf17cd227e596329b546ea04d13b053be4cf3338de777b50bc6eca7a6185cf7a5022bc9be3749b1bb43e10ecc88a0c580f2b7373138ee49c7bafd8be6a64048887230480b0c85a045255494e04a9a81646369ce7a10e08da6fae27333ec0c16c8a74d93779a9e055395078d0b07286f9930203010001");
    private static final byte[] testNetFogAuthoritySpki = Base64.decode("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAvnB9wTbTOT5uoizRYaYbw7XIEkInl8E7MGOAQj+xnC+F1rIXiCnc/t1+5IIWjbRGhWzo7RAwI5sRajn2sT4rRn9NXbOzZMvIqE4hmhmEzy1YQNDnfALAWNQ+WBbYGW+Vqm3IlQvAFFjVN1YYIdYhbLjAPdkgeVsWfcLDforHn6rR3QBZYZIlSBQSKRMY/tywTxeTCvK2zWcS0kbbFPtBcVth7VFFVPAZXhPi9yy1AvnldO6n7KLiupVmojlEMtv4FQkk604nal+j/dOplTATV8a9AJBbPRBZ/yQg57EG2Y2MRiHOQifJx0S5VbNyMm9bkS8TD7Goi59aCW6OT1gyeotWwLg60JRZTfyJ7lYWBSOzh0OnaCytRpSWtNZ6barPUeOnftbnJtE8rFhF7M4F66et0LI/cuvXYecwVwykovEVBKRF4HOK9GgSm17mQMtzrD7c558TbaucOWabYR04uhdAc3s10MkuONWG0wIQhgIChYVAGnFLvSpp2/aQEq3xrRSETxsixUIjsZyWWROkuA0IFnc8d7AmcnUBvRW7FT/5thWyk5agdYUGZ+7C1o69ihR1YxmoGh69fLMPIEOhYh572+3ckgl2SaV4uo9Gvkz8MMGRBcMIMlRirSwhCfozV2RyT5Wn1NgPpyc8zJL7QdOhL7Qxb+5WjnCVrQYHI2cCAwEAAQ==", Base64.DEFAULT);

    private TestFogConfig(@NonNull Uri fogUri,
                          @NonNull List<Uri> consensusUris,
                          @NonNull String username,
                          @NonNull String password, @NonNull ClientConfig clientConfig,
                          @NonNull byte[] fogAuthoritySpki, @NonNull String fogReportId,
                          @NonNull TransportProtocol transportProtocol) {
        this.fogUri = fogUri;
        this.consensusUris = consensusUris;
        this.username = username;
        this.password = password;
        this.clientConfig = clientConfig;
        this.fogAuthoritySpki = fogAuthoritySpki;
        this.fogReportId = fogReportId;
        this.transportProtocol = transportProtocol;
    }

    @NonNull
    public Uri getFogUri() {
        return fogUri;
    }

    @NonNull
    public Uri getConsensusUri() {
        return consensusUris.get(0);
    }

    @NonNull
    public List<Uri> getConsensusUris() {
        return consensusUris;
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
    public TransportProtocol getTransportProtocol() {
        return transportProtocol;
    }

    @NonNull
    static TestFogConfig getFogConfig(Environment.TestEnvironment testEnvironment, StorageAdapter storageAdapter) {
        return getFogConfig(testEnvironment, Optional.of(storageAdapter));
    }

    @NonNull
    static TestFogConfig getFogConfig(Environment.TestEnvironment testEnvironment) {
      return getFogConfig(testEnvironment, Optional.empty());
    }

    @NonNull
    private static TestFogConfig getFogConfig(Environment.TestEnvironment testEnvironment, Optional<StorageAdapter> storageAdapter) {
        final Uri fogUri = Uri.parse(String.format(
                "fog://fog.%s.mobilecoin.com",
                testEnvironment.getName()
        ));
        final Uri consensusUri1 = Uri.parse(String.format(
                "mc://node1.%s.mobilecoin.com",
                testEnvironment.getName()
        ));
        final Uri consensusUri2 = Uri.parse(String.format(
            "mc://node2.%s.mobilecoin.com",
            testEnvironment.getName()
        ));
        final Uri consensusUri3 = Uri.parse(String.format(
            "mc://node3.%s.mobilecoin.com",
            testEnvironment.getName()
        ));

        List<Uri> consensusUris = Arrays.asList(consensusUri1, consensusUri2, consensusUri3);
        TransportProtocol transportProtocol = TransportProtocol.forGRPC();
        switch (testEnvironment) {
            case MOBILE_DEV:
                return new TestFogConfig(fogUri, consensusUris, TEST_USERNAME,
                        TEST_PASSWORD, getDevClientConfig(storageAdapter),
                        mobiledevFogAuthoritySpki, "", transportProtocol);
            case ALPHA:
                return new TestFogConfig(fogUri, consensusUris, TEST_USERNAME,
                        TEST_PASSWORD, getDevClientConfig(storageAdapter),
                        alphaFogAuthoritySpki, "", transportProtocol);
            case TEST_NET:
                return new TestFogConfig(fogUri, consensusUris, TEST_USERNAME,
                        TEST_PASSWORD, getTestNetClientConfig(storageAdapter),
                        testNetFogAuthoritySpki, "", transportProtocol);
        }
        throw new UnsupportedOperationException("Requested config does not exist");
    }

    @NonNull
    private static ClientConfig getDevClientConfig(Optional<StorageAdapter> storageAdapter) {
        try {
            ClientConfig clientConfig = new ClientConfig();
            if (storageAdapter.isPresent()) {
                clientConfig.storageAdapter = storageAdapter.get();
            }
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
    private static ClientConfig getTestNetClientConfig(Optional<StorageAdapter> storageAdapter) {
        try {
            ClientConfig clientConfig = new ClientConfig();
            if (storageAdapter.isPresent()) {
                clientConfig.storageAdapter = storageAdapter.get();
            }
            clientConfig.fogView = new ClientConfig.Service()
                    .withVerifier((new Verifier())
                            .withMrSigner(Hex.toByteArray(
                                    "bf7fa957a6a94acb588851bc8767e0ca57706c79f4fc2aa6bcb993012c3c386c"),
                                    FOG_VIEW_PRODUCT_ID, SECURITY_VERSION,
                                    null,
                                    new String[]{"INTEL-SA-00334"}))
                    .withTrustRoots(getDevTrustRoots());
            clientConfig.fogLedger = new ClientConfig.Service()
                    .withVerifier((new Verifier())
                            .withMrSigner(Hex.toByteArray(
                                    "bf7fa957a6a94acb588851bc8767e0ca57706c79f4fc2aa6bcb993012c3c386c"),
                                    FOG_LEDGER_PRODUCT_ID, SECURITY_VERSION,
                                    null,
                                    new String[]{"INTEL-SA-00334"}))
                    .withTrustRoots(getDevTrustRoots());
            clientConfig.consensus = new ClientConfig.Service()
                    .withVerifier((new Verifier())
                            .withMrSigner(Hex.toByteArray(
                                    "bf7fa957a6a94acb588851bc8767e0ca57706c79f4fc2aa6bcb993012c3c386c"),
                                    CONSENSUS_PRODUCT_ID, SECURITY_VERSION,
                                    null,
                                    new String[]{"INTEL-SA-00334"}))
                    .withTrustRoots(getDevTrustRoots());
            clientConfig.report = new ClientConfig.Service()
                    .withVerifier((new Verifier())
                            .withMrSigner(Hex.toByteArray(
                                    "bf7fa957a6a94acb588851bc8767e0ca57706c79f4fc2aa6bcb993012c3c386c"),
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
