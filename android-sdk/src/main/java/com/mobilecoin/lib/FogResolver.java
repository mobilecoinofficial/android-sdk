// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.log.Logger;

final class FogResolver extends Native {
    private final static String TAG = FogResolver.class.getName();

    FogResolver(@NonNull FogReportResponses responses, @NonNull TrustedIdentities trustedIdentities) throws FogReportException {
        try {
            init_jni(responses, trustedIdentities);
        } catch (Exception exception) {
            throw new FogReportException("Unable to create fog report responses container",
                    exception);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            finalize_jni();
        } catch (Exception exception) {
            Logger.e(TAG, "Failed to free fog report data", exception);
        }
        super.finalize();
    }

    // native calls
    private native void init_jni(@NonNull FogReportResponses responses, @NonNull TrustedIdentities trusted_identities);

    private native void finalize_jni();
}
