// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.log.Logger;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final class Util extends Native {
    private final static String TAG = Util.class.getName();

    // Used in tests
    public native static String bigint2string(@NonNull BigInteger value);

    @NonNull
    private native static byte[] attest_verify_report(@NonNull byte[] report);

    static void logException(@NonNull String TAG, @NonNull Exception exception) {
        String message = exception.getMessage();
        if (null == message) {
            message = "";
        }
        Logger.w(TAG, message, exception);
    }

    static Set<X509Certificate> makeCertificatesFromData(byte[] certificateBytes) {
        HashSet<X509Certificate> trustRoots = new HashSet<>();
        ByteArrayInputStream bis = new ByteArrayInputStream(certificateBytes);
        try {
            Collection<? extends Certificate> certs =
                    CertificateFactory.getInstance("X.509")
                            .generateCertificates(bis);
            for (Certificate cert : certs) {
                trustRoots.add((X509Certificate) cert);
            }
        } catch (CertificateException exception) {
            throw new IllegalStateException("Valid certificate creation failed");
        }
        return trustRoots;
    }

    private static native int compute_commitment_crc32(byte committment_bytes[]);

    static int computeCommittmentCrc32(byte committmentBytes[]) {
        return compute_commitment_crc32(committmentBytes);
    }

}
