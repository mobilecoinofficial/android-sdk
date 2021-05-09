// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;
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

    static RistrettoPrivate recoverOnetimePrivateKey(
            @NonNull RistrettoPublic tx_pub_key,
            @NonNull RistrettoPrivate view_key,
            @NonNull RistrettoPrivate spend_key
    ) throws TransactionBuilderException {
        Logger.i(TAG, "Recovering onetime private key", null, "tx_pub_key:", tx_pub_key);
        try {
            long rustObj = recover_onetime_private_key(tx_pub_key,
                    view_key,
                    spend_key
            );
            return RistrettoPrivate.fromJNI(rustObj);
        } catch (Exception ex) {
            throw new TransactionBuilderException(ex.getLocalizedMessage(), ex);
        }
    }

    @NonNull
    public static byte[] versionedCryptoBoxDecrypt(
            @NonNull RistrettoPrivate viewKey,
            @NonNull byte[] cipherText
    ) throws InvalidFogResponse {
        Logger.i(TAG, "Decrypting with view key", null, "viewKey public:", viewKey.getPublicKey());
        try {
            return versioned_crypto_box_decrypt(
                    viewKey,
                    cipherText
            );
        } catch (Exception ex) {
            throw new InvalidFogResponse(ex.getLocalizedMessage(), ex);
        }
    }

    // Used in tests
    public native static String bigint2string(@NonNull BigInteger value);

    private native static long recover_onetime_private_key(
            @NonNull RistrettoPublic tx_pub_key,
            @NonNull RistrettoPrivate view_key,
            @NonNull RistrettoPrivate spend_key
    );

    @NonNull
    private native static byte[] attest_verify_report(@NonNull byte[] report);

    @NonNull
    private native static byte[] versioned_crypto_box_decrypt(
            @NonNull RistrettoPrivate viewKey,
            @NonNull byte[] cipherText
    );

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
}
