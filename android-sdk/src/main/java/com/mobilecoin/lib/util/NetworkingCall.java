// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.util;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.NetworkException;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class NetworkingCall<T> {
    private final RetryPolicy retryPolicy;
    private final Callable<T> callable;

    public NetworkingCall(@NonNull RetryPolicy retryPolicy, @NonNull Callable<T> callable) {
        this.retryPolicy = retryPolicy;
        this.callable = callable;
    }

    public NetworkingCall(@NonNull Callable<T> callable) {
        this(new DefaultRetryPolicy(), callable);
    }

    @NonNull
    public T run() throws Exception {
        int count = 0;
        while (true) {
            try {
                return callable.call();
            } catch (NetworkException exception) {
                // handle exception
                if (Arrays.stream(retryPolicy.statusCodes).anyMatch(i -> i == exception.getResultCode())) {
                    if (++count == retryPolicy.retryCount) throw exception;
                } else {
                    throw exception;
                }
            }
        }
    }

    public static class RetryPolicy {
        int[] statusCodes;
        int retryCount;
    }

    public static class DefaultRetryPolicy extends RetryPolicy {
        DefaultRetryPolicy() {
            statusCodes = new int[]{403, 500};
            retryCount = 1;
        }
    }
}
