package com.mobilecoin.lib.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MobileCoinException extends Exception {

    public MobileCoinException() {

    }
    public MobileCoinException(@NonNull Throwable throwable) {
        super(throwable);
    }

    public MobileCoinException(@Nullable String message) {
        super(message);
    }

    public MobileCoinException(@Nullable String message, @Nullable Throwable exception) {
        super(message, exception);
    }
}

