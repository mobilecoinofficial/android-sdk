// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public class BadEntropyException extends MobileCoinException {
    public BadEntropyException() {
        super();
    }

    public BadEntropyException(@Nullable String message) {
        super(message);
    }

    public BadEntropyException(@Nullable String message, @Nullable Throwable exception) {
        super(message, exception);
    }
}
