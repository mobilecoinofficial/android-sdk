// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.util;

import java.util.concurrent.Callable;

public abstract class Task<V, E extends Exception> implements Callable<Result<V, E>> {

    public abstract V execute() throws E;

    @SuppressWarnings("unchecked")
    public Result<V, E> call() {
        try {
            return Result.ok(execute());
        } catch (Exception exception) {
            return Result.err((E)exception);
        }
    }
}
