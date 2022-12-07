package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public class SignedContingentInputBuilderException extends MobileCoinException {

    public SignedContingentInputBuilderException(@Nullable String message) {
        super(message);
    }

    public SignedContingentInputBuilderException(@Nullable String message, @Nullable Throwable throwable) {
        super(message, throwable);
    }

}
