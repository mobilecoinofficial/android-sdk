// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public final class InvalidTransactionException extends MobileCoinException {
    public InvalidTransactionException(@Nullable String message) {
        super(message);
    }
}
