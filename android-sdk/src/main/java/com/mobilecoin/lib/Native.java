// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

class Native {
    private final static String MOBILECOIN_LIB_NAME = "mobilecoin";

    // Load JNI lib
    static {
        System.loadLibrary(MOBILECOIN_LIB_NAME);
    }

    protected long rustObj = 0;
}
