// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public class BadMnemonicException extends MobileCoinException {
    public BadMnemonicException() {
        super();
    }

    public BadMnemonicException(@Nullable String message) {
        super(message);
    }

    public BadMnemonicException(@Nullable String message, @Nullable Throwable exception) {
        super(message, exception);
    }
}
