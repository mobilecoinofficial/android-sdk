// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.uri;

import androidx.annotation.NonNull;

public class ConsensusUriScheme implements MobileCoinScheme {
    @NonNull
    @Override
    public String secureScheme() {
        return "mc";
    }

    @NonNull
    @Override
    public String insecureScheme() {
        return "insecure-mc";
    }

    @Override
    public int securePort() {
        return 443;
    }

    @Override
    public int insecurePort() {
        return 3223;
    }
}
