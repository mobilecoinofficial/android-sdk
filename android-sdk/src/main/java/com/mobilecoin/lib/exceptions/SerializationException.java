// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public class SerializationException extends MobileCoinException {
    public SerializationException(@Nullable String message) {
        super(message);
    }

    public SerializationException() {
        super("Unable to construct object from the provided data");
    }

    public SerializationException(@Nullable String message, @Nullable Throwable throwable) {
        super(message, throwable);
    }
}
