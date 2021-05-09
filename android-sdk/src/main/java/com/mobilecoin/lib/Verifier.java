// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.log.Logger;

/**
 * Verifier is used for attestation connection verification.
 */

public final class Verifier extends Native {
    private static final String TAG = Verifier.class.getName();

    public Verifier() throws AttestationException {
        Logger.i(TAG, "Creating a new Verifier");
        try {
            init_jni();
        } catch (Exception ex) {
            Logger.wtf(TAG, "Unable to create a native verifier", ex);
            throw new AttestationException(ex.getLocalizedMessage(), ex);
        }
    }

    public @NonNull
    Verifier withMrSigner(@NonNull byte[] mrSigner, short productId, short securityVersion,
                          @Nullable String[] configAdvisories,
                          @Nullable String[] hardeningAdvisories) throws AttestationException {
        Logger.i(TAG, "Adding a MrSigner to the Verifier");
        try {
            // method parameters are nullable, however, native API isn't
            if (null == configAdvisories) configAdvisories = new String[0];
            if (null == hardeningAdvisories) hardeningAdvisories = new String[0];

            add_mr_signer(mrSigner, productId, securityVersion, configAdvisories,
                    hardeningAdvisories);
        } catch (Exception ex) {
            Logger.e(TAG, "Unable to add a MrSigner to the verifier", ex);
            throw new AttestationException(ex.getLocalizedMessage(), ex);
        }
        return this;
    }

    public @NonNull
    Verifier withMrEnclave(@NonNull byte[] mrEnclave, @Nullable String[] configAdvisories,
                           @Nullable String[] hardeningAdvisories) throws AttestationException {
        Logger.i(TAG, "Adding a MrEnclave to the Verifier");
        try {
            // method parameters are nullable, however, native API isn't
            if (null == configAdvisories) configAdvisories = new String[0];
            if (null == hardeningAdvisories) hardeningAdvisories = new String[0];

            add_mr_enclave(mrEnclave, configAdvisories, hardeningAdvisories);
        } catch (Exception ex) {
            Logger.e(TAG, "Unable to add a MrEnclave to the verifier", ex);
            throw new AttestationException(ex.getLocalizedMessage(), ex);
        }
        return this;
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    // JNI calls
    private native void init_jni();

    private native void add_mr_signer(@NonNull byte[] mr_signer, short product_id,
                                      short security_version, @NonNull String[] configAdvisories,
                                      @NonNull String[] hardeningAdvisories);

    private native void add_mr_enclave(@NonNull byte[] mr_enclave,
                                       @NonNull String[] configAdvisories,
                                       @NonNull String[] hardeningAdvisories);

    private native void finalize_jni();
}
