// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import com.mobilecoin.lib.log.Logger;

class Native {
    private final static String MOBILECOIN_LIB_NAME = "mobilecoin";
    private static final String TAG = Native.class.toString();

    // Load JNI lib
    static {
        try {
            System.loadLibrary(MOBILECOIN_LIB_NAME);
        } catch (UnsatisfiedLinkError error) {
            Logger.wtf(TAG, "Unable to load mobilecoin library", error);
        }
    }

    protected long rustObj = 0;
}
