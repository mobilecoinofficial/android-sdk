// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InvalidFogResponse extends MobileCoinException {
    public InvalidFogResponse(@Nullable String message) {
        super(message);
    }

    public InvalidFogResponse(@NonNull Throwable throwable) {
        super(throwable);
    }

    public InvalidFogResponse(@Nullable String message, @Nullable Throwable throwable) {
        super(message, throwable);
    }
}

