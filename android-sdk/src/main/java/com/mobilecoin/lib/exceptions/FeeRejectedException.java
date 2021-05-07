// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public final class FeeRejectedException extends MobileCoinException {

    public FeeRejectedException(@Nullable String message) {
        super(message);
    }
}
