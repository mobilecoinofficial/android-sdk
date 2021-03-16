// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.log.Logger;

import java.util.Arrays;

public class KeyImage {
    private final static String TAG = KeyImage.class.getName();
    private final byte[] data;

    private KeyImage(@NonNull byte[] data) {
        this.data = data;
    }

    @NonNull
    public static KeyImage fromBytes(@NonNull byte[] bytes) {
        Logger.i(TAG, "Getting KeyImage from bytes");
        return new KeyImage(bytes);
    }

    @NonNull
    public byte[] getData() {
        Logger.i(TAG, "KeyImage data", null, data);
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyImage keyImage = (KeyImage) o;
        return Arrays.equals(getData(), keyImage.getData());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
