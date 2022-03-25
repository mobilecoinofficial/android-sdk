package com.mobilecoin.lib.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.UnsignedLong;

public class FogSyncException extends MobileCoinException {

    public FogSyncException(@NonNull Throwable throwable) {
        super(throwable);
    }

    public FogSyncException(@Nullable String message) {
        super(message);
    }

    public FogSyncException(@Nullable String message, @Nullable Throwable exception) {
        super(message, exception);
    }

}
