// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import java.util.Arrays;

/**
 * A 32-byte image of a private key `x`: I = x * H(x * G) = x * H(P).
 */
final public class KeyImage {
    private final byte[] data;

    private KeyImage(@NonNull byte[] data) {
        this.data = data;
    }

    @NonNull
    public static KeyImage fromBytes(@NonNull byte[] bytes) {
        return new KeyImage(bytes);
    }

    @NonNull
    public byte[] getData() {
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
