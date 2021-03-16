// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

public abstract class StorageAdapter {
    public abstract byte[] get(String key);

    public abstract void set(
            String key,
            byte[] value
    );

    public abstract void clear(String key);
}
