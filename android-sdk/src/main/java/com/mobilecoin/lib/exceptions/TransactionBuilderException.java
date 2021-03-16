// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public class TransactionBuilderException extends MobileCoinException {
    public TransactionBuilderException(@Nullable String message) {
        super(message);
    }

    public TransactionBuilderException(@Nullable String message, @Nullable Throwable throwable) {
        super(message, throwable);
    }
}
