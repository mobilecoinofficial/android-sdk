package com.mobilecoin.lib.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This class represents the success state of @{link Result}.
 */
public class Ok<V, E> implements Result<V, E> {
    private final V value;

    Ok(final V value) {
        super();
        this.value = value;
    }

    @NonNull
    @Override
    public V getValue() {
        return value;
    }

    @Nullable
    @Override
    public E getError() {
        return null;
    }

    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public boolean isErr() {
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("Ok(%s)", value);
    }
}