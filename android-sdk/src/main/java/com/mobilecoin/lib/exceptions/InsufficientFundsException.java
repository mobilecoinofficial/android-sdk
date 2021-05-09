// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public final class InsufficientFundsException extends MobileCoinException {
    InsufficientFundsException(@Nullable String message) {
        super(message);
    }

    public InsufficientFundsException() {
        this("Insufficient funds");
    }
}
