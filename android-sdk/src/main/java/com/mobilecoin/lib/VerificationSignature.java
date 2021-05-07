// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.log.Logger;

final class VerificationSignature extends Native {
    private final static String TAG = VerificationSignature.class.getName();

    VerificationSignature(@NonNull byte[] sigBytes) throws FogReportException {
        try {
            init_jni(sigBytes);
        } catch (Exception exception) {
            throw new FogReportException("Unable to create verification signature", exception);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            finalize_jni();
        } catch (Exception exception) {
            Logger.e(TAG, "Failed to free verification signature data", exception);
        }
        super.finalize();
    }

    // native calls
    private native void init_jni(@NonNull byte[] sig_bytes);

    private native void finalize_jni();
}
