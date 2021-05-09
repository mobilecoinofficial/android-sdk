// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.log.Logger;

final class VerificationReport extends Native {
    private final static String TAG = VerificationReport.class.getName();

    VerificationReport(@NonNull VerificationSignature signature,
                       @NonNull byte[][] chain,
                       @NonNull String http_body
    ) throws FogReportException {
        try {
            init_jni(signature, chain, http_body);
        } catch (Exception exception) {
            throw new FogReportException("Unable to create verification signature", exception);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            finalize_jni();
        } catch (Exception exception) {
            Logger.e(TAG, "Failed to free verification report data", exception);
        }
        super.finalize();
    }

    // native calls
    private native void init_jni(@NonNull VerificationSignature verification_signature,
                                 @NonNull byte[][] chain,
                                 @NonNull String http_body);

    private native void finalize_jni();
}
