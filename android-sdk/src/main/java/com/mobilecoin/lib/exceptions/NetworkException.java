// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.network.ApiResult;

public final class CheckedNetworkException extends MobileCoinException {
    public final int statusCode;

    public CheckedNetworkException(int statusCode, @Nullable String message, @Nullable Throwable throwable) {
        super(message, throwable);
        this.statusCode = statusCode;
    }

    public CheckedNetworkException(
            int statusCode,
            @Nullable String message
    ) {
        super(message);
        this.statusCode = statusCode;
    }

    public CheckedNetworkException(@NonNull ApiResult.ResultCode resultCode) {
        switch (resultCode) {
            case OK:
                statusCode = 200;
                break;
            case INVALID_ARGUMENT:
            case FAILED_PRECONDITION:
            case OUT_OF_RANGE:
                statusCode = 400;
                break;
            case UNAUTHENTICATED:
                statusCode = 401;
                break;
            case PERMISSION_DENIED:
                statusCode = 403;
                break;
            case NOT_FOUND:
                statusCode = 404;
                break;
            case ABORTED:
            case ALREADY_EXISTS:
                statusCode = 409;
                break;
            case RESOURCE_EXHAUSTED:
                statusCode = 429;
                break;
            case CANCELLED:
                statusCode = 499;
                break;
            case DATA_LOSS:
            case UNKNOWN:
            case INTERNAL:
                statusCode = 500;
                break;
            case UNAVAILABLE:
                statusCode = 503;
                break;
            case DEADLINE_EXCEEDED:
                statusCode = 504;
                break;
            default:
                // UNIMPLEMENTED
                statusCode = 501;
        }
    }
}
