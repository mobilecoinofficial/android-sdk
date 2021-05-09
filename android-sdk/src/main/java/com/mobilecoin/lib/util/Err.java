package com.mobilecoin.lib.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This class represents failed state of @{link Result}.
 */
public final class Err<V, E> implements Result<V, E> {
    private final E error;

    Err(final E error) {
        super();
        this.error = error;
    }

    @Nullable
    @Override
    public V getValue() {
        return null;
    }

    @NonNull
    @Override
    public E getError() {
        return error;
    }

    @Override
    public boolean isOk() {
        return false;
    }

    @Override
    public boolean isErr() {
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("Err(%s)", error);
    }
}