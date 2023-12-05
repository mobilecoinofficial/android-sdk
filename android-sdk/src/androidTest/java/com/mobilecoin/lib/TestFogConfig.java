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
    public static final String CONFIG_ADVISORIES[] = null;
    public static final String HARDENING_ADVISORIES[] = {"INTEL-SA-00334", "INTEL-SA-00615"};

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

    private static final byte[] alphaFogAuthoritySpki = Base64.decode("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAyFOockvCEc9TcO1NvsiUfFVzvtDsR64UIRRUl3tBM2Bh8KBA932/Up86RtgJVnbslxuUCrTJZCV4dgd5hAo/mzuJOy9lAGxUTpwWWG0zZJdpt8HJRVLX76CBpWrWEt7JMoEmduvsCR8q7WkSNgT0iIoSXgT/hfWnJ8KGZkN4WBzzTH7hPrAcxPrzMI7TwHqUFfmOX7/gc+bDV5ZyRORrpuu+OR2BVObkocgFJLGmcz7KRuN7/dYtdYFpiKearGvbYqBrEjeo/15chI0Bu/9oQkjPBtkvMBYjyJPrD7oPP67i0ZfqV6xCj4nWwAD3bVjVqsw9cCBHgaykW8ArFFa0VCMdLy7UymYU5SQsfXrw/mHpr27Pp2Z0/7wpuFgJHL+0ARU48OiUzkXSHX+sBLov9X6f9tsh4q/ZRorXhcJi7FnUoagBxewvlfwQfcnLX3hp1wqoRFC4w1DC+ki93vIHUqHkNnayRsf1n48fSu5DwaFfNvejap7HCDIOpCCJmRVR8mVuxi6jgjOUa4Vhb/GCzxfNIn5ZYym1RuoE0TsFO+TPMzjed3tQvG7KemGFz3pQIryb43SbG7Q+EOzIigxYDytzcxOO5Jx7r9i+amQEiIcjBICwyFoEUlVJTgSpqBZGNpznoQ4I2m+uJzM+wMFsinTZN3mp4FU5UHjQsHKG+ZMCAwEAAQ==", Base64.DEFAULT);
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
                    .withTrustedIdentities((new TrustedIdentities())
                            .addMrSignerIdentity(Hex.toByteArray(
                                    "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411"),
                                    FOG_VIEW_PRODUCT_ID, SECURITY_VERSION,
                                    CONFIG_ADVISORIES,
                                    HARDENING_ADVISORIES))
                    .withTrustRoots(getDevTrustRoots());
            clientConfig.fogLedger = new ClientConfig.Service()
                    .withTrustedIdentities((new TrustedIdentities())
                            .addMrSignerIdentity(Hex.toByteArray(
                                    "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411"),
                                    FOG_LEDGER_PRODUCT_ID, SECURITY_VERSION,
                                    CONFIG_ADVISORIES,
                                    HARDENING_ADVISORIES))
                    .withTrustRoots(getDevTrustRoots());
            clientConfig.consensus = new ClientConfig.Service()
                    .withTrustedIdentities((new TrustedIdentities())
                            .addMrSignerIdentity(Hex.toByteArray(
                                    "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411"),
                                    CONSENSUS_PRODUCT_ID, SECURITY_VERSION,
                                    CONFIG_ADVISORIES,
                                    HARDENING_ADVISORIES))
                    .withTrustRoots(getDevTrustRoots());
            clientConfig.report = new ClientConfig.Service()
                    .withTrustedIdentities((new TrustedIdentities())
                            .addMrSignerIdentity(Hex.toByteArray(
                                    "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411"),
                                    FOG_REPORT_PRODUCT_ID, SECURITY_VERSION,
                                    CONFIG_ADVISORIES,
                                    HARDENING_ADVISORIES)
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
                    .withTrustedIdentities((new TrustedIdentities())
                            .addMrSignerIdentity(Hex.toByteArray(
                                    "bf7fa957a6a94acb588851bc8767e0ca57706c79f4fc2aa6bcb993012c3c386c"),
                                    FOG_VIEW_PRODUCT_ID, SECURITY_VERSION,
                                    CONFIG_ADVISORIES,
                                    HARDENING_ADVISORIES))
                    .withTrustRoots(getTestTrustRoots());
            clientConfig.fogLedger = new ClientConfig.Service()
                    .withTrustedIdentities((new TrustedIdentities())
                            .addMrSignerIdentity(Hex.toByteArray(
                                    "bf7fa957a6a94acb588851bc8767e0ca57706c79f4fc2aa6bcb993012c3c386c"),
                                    FOG_LEDGER_PRODUCT_ID, SECURITY_VERSION,
                                    CONFIG_ADVISORIES,
                                    HARDENING_ADVISORIES))
                    .withTrustRoots(getTestTrustRoots());
            clientConfig.consensus = new ClientConfig.Service()
                    .withTrustedIdentities((new TrustedIdentities())
                            .addMrSignerIdentity(Hex.toByteArray(
                                    "bf7fa957a6a94acb588851bc8767e0ca57706c79f4fc2aa6bcb993012c3c386c"),
                                    CONSENSUS_PRODUCT_ID, SECURITY_VERSION,
                                    CONFIG_ADVISORIES,
                                    HARDENING_ADVISORIES))
                    .withTrustRoots(getTestTrustRoots());
            clientConfig.report = new ClientConfig.Service()
                    .withTrustedIdentities((new TrustedIdentities())
                            .addMrSignerIdentity(Hex.toByteArray(
                                    "bf7fa957a6a94acb588851bc8767e0ca57706c79f4fc2aa6bcb993012c3c386c"),
                                    FOG_REPORT_PRODUCT_ID, SECURITY_VERSION,
                                    CONFIG_ADVISORIES,
                                    HARDENING_ADVISORIES)
                    );
            return clientConfig;
        } catch (AttestationException ex) {
            throw new IllegalStateException("BUG: Unreachable code");
        }
    }

    @NonNull
    private static Set<X509Certificate> getTestTrustRoots() {
        String trustRootBase64String = "MIIDSjCCAjKgAwIBAgIQRK+wgNajJ7qJMDmGLvhAazANBgkqhkiG9w0BAQUFADA/MSQwIgYDVQQKExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMTDkRTVCBSb290IENBIFgzMB4XDTAwMDkzMDIxMTIxOVoXDTIxMDkzMDE0MDExNVowPzEkMCIGA1UEChMbRGlnaXRhbCBTaWduYXR1cmUgVHJ1c3QgQ28uMRcwFQYDVQQDEw5EU1QgUm9vdCBDQSBYMzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAN+v6ZdQCINXtMxiZfaQguzH0yxrMMpb7NnDfcdAwRgUi+DoM3ZJKuM/IUmTrE4Orz5Iy2Xu/NMhD2XSKtkyj4zl93ewEnu1lcCJo6m67XMuegwGMoOifooUMM0RoOEqOLl5CjH9UL2AZd+3UWODyOKIYepLYYHsUmu5ouJLGiifSKOeDNoJjj4XLh7dIN9bxiqKqy69cK3FCxolkHRyxXtqqzTWMIn/5WgTe1QLyNau7Fqckh49ZLOMxt+/yUFw7BZy1SbsOFU5Q9D8/RhcQPGX69Wam40dutolucbY38EVAjqr2m7xPi71XAicPNaDaeQQmxkqtilX4+U9m5/wAl0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAQYwHQYDVR0OBBYEFMSnsaR7LHH62+FLkHX/xBVghYkQMA0GCSqGSIb3DQEBBQUAA4IBAQCjGiybFwBcqR7uKGY3Or+Dxz9LwwmglSBd49lZRNI+DT69ikugdB/OEIKcdBodfpga3csTS7MgROSR6cz8faXbauX+5v3gTt23ADq1cEmv8uXrAvHRAosZy5Q6XkjEGB5YGV8eAlrwDPGxrancWYaLbumR9YbK+rlmM6pZW87ipxZzR8srzJmwN0jP41ZL9c8PDHIyh8bwRLtTcm1D9SZImlJnt1ir/md2cXjbDaJWFBM5JDGFoqgCWjBH4d1QB7wCCZAA62RjYJsWvIjJEubSfZGL+T0yjWW06XyxV3bqxbYoOb8VZRzI9neWagqNdwvYkQsEjgfbKbYK7p2CNTUQ";
        byte[] trustRootBytes = Base64.decode(trustRootBase64String, Base64.DEFAULT);
        return Util.makeCertificatesFromData(trustRootBytes);
    }

    @NonNull
    private static Set<X509Certificate> getDevTrustRoots() {
        String trustRootBase64String = "MIIFjDCCA3SgAwIBAgINAgO8UKMnU/CRgCLt8TANBgkqhkiG9w0BAQsFADBHMQswCQYDVQQGEwJVUzEiMCAGA1UEChMZR29vZ2xlIFRydXN0IFNlcnZpY2VzIExMQzEUMBIGA1UEAxMLR1RTIFJvb3QgUjEwHhcNMjAwODEzMDAwMDQyWhcNMjcwOTMwMDAwMDQyWjBGMQswCQYDVQQGEwJVUzEiMCAGA1UEChMZR29vZ2xlIFRydXN0IFNlcnZpY2VzIExMQzETMBEGA1UEAxMKR1RTIENBIDFQNTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALOC8CSMvy2Hr7LZp676yrpE1ls+/rL3smUW3N4Q6E8tEFhaKIaHoe5qs6DZdU9/oVIBi1WoSlsGSMg2EiWrifnyI1+dYGX5XNq+OuhcbX2c0IQYhTDNTpvsPNiz4ZbU88ULZduPsHTL9h7zePGslcXdc8MxiIGvdKpv/QzjBZXwxRBPZWP6oK/GGD3Fod+XedcFibMwsHSuPZIQa4wVd90LBFf7gQPd6iI01eVWsvDEjUGxwwLbYuyA0P921IbkBBq2tgwrYnF92a/Z8V76wB7KoBlcVfCA0SoMB4aQnzXjKCtb7yPIox2kozru/oPcgkwlsE3FUa2em9NbhMIaWukCAwEAAaOCAXYwggFyMA4GA1UdDwEB/wQEAwIBhjAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwEgYDVR0TAQH/BAgwBgEB/wIBADAdBgNVHQ4EFgQU1fyeDd8eyt0Il5duK8VfxSv17LgwHwYDVR0jBBgwFoAU5K8rJnEaK0gnhS9SZizv8IkTcT4waAYIKwYBBQUHAQEEXDBaMCYGCCsGAQUFBzABhhpodHRwOi8vb2NzcC5wa2kuZ29vZy9ndHNyMTAwBggrBgEFBQcwAoYkaHR0cDovL3BraS5nb29nL3JlcG8vY2VydHMvZ3RzcjEuZGVyMDQGA1UdHwQtMCswKaAnoCWGI2h0dHA6Ly9jcmwucGtpLmdvb2cvZ3RzcjEvZ3RzcjEuY3JsME0GA1UdIARGMEQwOAYKKwYBBAHWeQIFAzAqMCgGCCsGAQUFBwIBFhxodHRwczovL3BraS5nb29nL3JlcG9zaXRvcnkvMAgGBmeBDAECATANBgkqhkiG9w0BAQsFAAOCAgEAbGMn7iPf5VJoTYFmkYXffWXlWzcxCCayB12avrHKAbmtv5139lEd15jFC0mhe6HX02jlRA+LujbdQoJ30o3d9T/768gHmJPuWtC1Pd5LHC2MTex+jHv+TkD98LSzWQIQUVzjwCv9twZIUX4JXj8P3Kf+l+d5xQ5EiXjFaVkpoJo6SDYpppSTVS24R7XplrWfB82mqz4yisCGg8XBQcifLzWODcAHeuGsyWW1y4qn3XHYYWU5hKwyPvd6NvFWn1epQW1akKfbOup1gAxjC2l0bwdMFfM3KKUZpG719iDNY7J+xCsJdYna0Twuck82GqGeRNDNm6YjCD+XoaeeWqX3CZStXXZdKFbRGmZRUQd73j2wyO8weiQtvrizhvZL9/C1T//Oxvn2PyonCA8JPiNax+NCLXo25D2YlmA5mOrR22Mq63gJsU4hs463zj6S8ZVcpDnQwCvIUxX10i+CzQZ0Z5mQdzcKly3FHB700FvpFePqAgnIE9cTcGW/+4ibWiW+dwnhp2pOEXW5Hk3xABtqZnmOw27YbaIiom0F+yzy8VDloNHYnzV9/HCrWSoC8b6w0/H4zRK5aiWQW+OFIOb12stAHBk0IANhd7p/SA9JCynr52Fkx2PRR+sc4e6URu85c8zuTyuN3PtYp7NlIJmVuftVb9eWbpQ99HqSjmMd320=";
        byte[] trustRootBytes = Base64.decode(trustRootBase64String, Base64.DEFAULT);
        return Util.makeCertificatesFromData(trustRootBytes);
    }
}
