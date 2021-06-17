// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.network.uri;

public interface MobileCoinScheme {
    String secureScheme();

    String insecureScheme();

    int securePort();

    int insecurePort();
}
