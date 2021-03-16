// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public class BadBip39SeedException extends MobileCoinException {
    public BadBip39SeedException() {
        super();
    }

    public BadBip39SeedException(@Nullable String message) {
        super(message);
    }

    public BadBip39SeedException(@Nullable String message, @Nullable Throwable exception) {
        super(message, exception);
    }
}
