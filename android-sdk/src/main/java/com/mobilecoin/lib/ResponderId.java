// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;

final class ResponderId extends Native {

    ResponderId(@NonNull Uri serviceUri) {
        init_jni(String.format(
                "%s:%s",
                serviceUri.getHost(),
                serviceUri.getPort()
        ));
    }

    private ResponderId(long existingRustObj) {
        rustObj = existingRustObj;
    }

    @NonNull
    static ResponderId fromJNI(long rustObj) {
        return new ResponderId(rustObj);
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    private native void init_jni(@NonNull String address);

    private native void finalize_jni();
}
