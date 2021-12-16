// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.network.NetworkResult;

import io.grpc.Status;

public final class NetworkException extends MobileCoinException {

    public final NetworkResult result;
    public final int resultCode;

    public NetworkException(@NonNull NetworkResult result) {
        this(result, null);
    }

    public NetworkException(@NonNull NetworkResult result, @Nullable Throwable throwable) {
        super(result.getCode().toString(), throwable);
        this.result = result;
        switch (result.getCode()) {
            case OK:
                resultCode = 200;
                break;
            case INVALID_ARGUMENT:
            case FAILED_PRECONDITION:
            case OUT_OF_RANGE:
                resultCode = 400;
                break;
            case UNAUTHENTICATED:
                resultCode = 401;
                break;
            case PERMISSION_DENIED:
                resultCode = 403;
                break;
            case NOT_FOUND:
                resultCode = 404;
                break;
            case ABORTED:
            case ALREADY_EXISTS:
                resultCode = 409;
                break;
            case RESOURCE_EXHAUSTED:
                resultCode = 429;
                break;
            case CANCELLED:
                resultCode = 499;
                break;
            case DATA_LOSS:
            case UNKNOWN:
            case INTERNAL:
                resultCode = 500;
                break;
            case UNAVAILABLE:
                resultCode = 503;
                break;
            case DEADLINE_EXCEEDED:
                resultCode = 504;
                break;
            default:
                // UNIMPLEMENTED
                resultCode = 501;
        }
    }

    public NetworkException(@NonNull Status status) {
        this(status, null);
    }

    public NetworkException(@NonNull Status status, Throwable throwable) {
        this(NetworkResult.from(status), throwable);
    }

    public NetworkResult getResult() {
        return this.result;
    }

    public int getResultCode() {
        return this.resultCode;
    }

}
