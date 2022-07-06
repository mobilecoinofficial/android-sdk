// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

public final class InvalidTransactionException extends MobileCoinException {
    public final String message;

    public InvalidTransactionException(String message) {
        super(message);

        this.message = message;
    }
}
