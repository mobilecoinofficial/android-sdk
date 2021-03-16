// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public class FogReportException extends MobileCoinException {

    public FogReportException() {
        super();
    }

    public FogReportException(@Nullable String message) {
        super(message);
    }

    public FogReportException(@Nullable String message, @Nullable Throwable exception) {
        super(message, exception);
    }
}
