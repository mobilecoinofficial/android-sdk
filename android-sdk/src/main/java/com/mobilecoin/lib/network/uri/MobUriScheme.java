// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.network.uri;

import androidx.annotation.NonNull;

public final class MobUriScheme implements MobileCoinScheme {
    @NonNull
    @Override
    public String secureScheme() {
        return "mob";
    }

    @NonNull
    @Override
    public String insecureScheme() {
        return "mob";
    }

    @Override
    public int securePort() {
        return -1;
    }

    @Override
    public int insecurePort() {
        return -1;
    }

    public String payloadType() {
        return "b58";
    }
}
