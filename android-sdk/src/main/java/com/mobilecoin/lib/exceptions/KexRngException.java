// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public final class KexRngException extends MobileCoinException {
    public KexRngException() {
    }

    public KexRngException(@Nullable String message) {
        super(message);
    }

    public KexRngException(@Nullable String message, @Nullable Throwable throwable) {
        super(message, throwable);
    }
}
