// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.network.uri;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidUriException;

public final class FogUri extends MobileCoinUri {

    public FogUri(@NonNull Uri uri) throws InvalidUriException {
        super(uri, new FogUriScheme());
    }

    public FogUri(@NonNull String uriString) throws InvalidUriException {
        super(uriString, new FogUriScheme());
    }
}
