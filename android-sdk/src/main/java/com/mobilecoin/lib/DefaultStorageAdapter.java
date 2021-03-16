// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

class DefaultStorageAdapter extends StorageAdapter {
    @Override
    public byte[] get(String key) {
        return null;
    }

    @Override
    public void set(
            String key,
            byte[] value
    ) {
    }

    @Override
    public void clear(String key) {
    }
}
