// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.uri;

import androidx.annotation.NonNull;

public final class FogUriScheme implements MobileCoinScheme {
    @NonNull
    @Override
    public String secureScheme() {
        return "fog";
    }

    @NonNull
    @Override
    public String insecureScheme() {
        return "insecure-fog";
    }

    @Override
    public int securePort() {
        return 443;
    }

    @Override
    public int insecurePort() {
        return 3225;
    }
}
