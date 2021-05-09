// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public final class InvalidFogResponse extends MobileCoinException {
    public InvalidFogResponse(@Nullable String message) {
        super(message);
    }

    public InvalidFogResponse(@Nullable String message, @Nullable Throwable throwable) {
        super(message, throwable);
    }
}

