// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public final class BadBip39EntropyException extends MobileCoinException {
    public BadBip39EntropyException() {
        super();
    }

    public BadBip39EntropyException(@Nullable String message) {
        super(message);
    }

    public BadBip39EntropyException(@Nullable String message, @Nullable Throwable exception) {
        super(message, exception);
    }
}
