// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public final class AmountDecoderException extends MobileCoinException {
    public AmountDecoderException(@Nullable String message) {
        super(message);
    }

    public AmountDecoderException(@Nullable String message, @Nullable Throwable throwable) {
        super(message, throwable);
    }
}
