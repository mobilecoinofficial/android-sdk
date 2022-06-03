// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.network.NetworkResult;

public final class NetworkException extends MobileCoinException {

    public final NetworkResult result;
    public final int statusCode;

    public NetworkException(@NonNull NetworkResult result) {
        this(result, null);
    }

    public NetworkException(@NonNull NetworkResult result, @Nullable Throwable throwable) {
        super(result.getResultCode().toString(), throwable);
        this.result = result;
        this.statusCode = result.getResultCode().intValue();
    }

    public NetworkResult getResult() {
        return this.result;
    }

    public int getResultCode() {
        return this.statusCode;
    }

}

