// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public final class AttestationException extends MobileCoinException {

    public AttestationException(@Nullable String message) {
        super(message);
    }

    public AttestationException(@Nullable String message, @Nullable Throwable throwable) {
        super(message, throwable);
    }
}
