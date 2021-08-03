// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

/** Allows SDK to serialize and deserialize SDK object using host-app provided storage. */
public interface StorageAdapter {

    boolean has(String key);

    byte[] get(String key);

    void set(String key, byte[] value);

    void clear(String key);
}