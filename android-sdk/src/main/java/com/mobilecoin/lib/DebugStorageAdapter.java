// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import java.util.HashMap;

class DebugStorageAdapter extends StorageAdapter {
    HashMap<String, byte[]> cache = new HashMap<>();
    @Override
    public byte[] get(@NonNull String key) {
        return cache.get(key);
    }

    @Override
    public void set(
            @NonNull String key,
            byte[] value
    ) {
        cache.put(
                key,
                value
        );
    }

    @Override
    public void clear(@NonNull String key) {
        cache.remove(key);
    }

}
