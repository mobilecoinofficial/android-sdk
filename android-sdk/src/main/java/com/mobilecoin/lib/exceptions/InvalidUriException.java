// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public final class InvalidUriException extends MobileCoinException {
    public InvalidUriException() {
    }

    public InvalidUriException(@Nullable String message) {
        super(message);
    }

    public InvalidUriException(@Nullable String message, @Nullable Throwable throwable) {
        super(message, throwable);
    }
}
