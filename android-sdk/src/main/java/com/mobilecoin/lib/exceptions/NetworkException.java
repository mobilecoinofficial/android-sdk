// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.network.NetworkResult;

public final class NetworkException extends MobileCoinException {

    public final NetworkResult result;

    public NetworkException(@NonNull NetworkResult result) {
        this(result, null);
    }

    public NetworkException(@NonNull NetworkResult result, @Nullable Throwable throwable) {
        super(result.getCode().toString(), throwable);
        this.result = result;
    }

    public NetworkResult getResult() {
        return this.result;
    }

    public int getResultCode() {
        return this.result.getCode().intValue();
    }

}

