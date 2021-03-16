package com.mobilecoin.lib.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface Result<V, E> {

    /**
     * Creates a successful Result with a return value
     * @param <V> value of the result
     */
    static <V, E> Result<V, E> ok(@NonNull final V value) {
        return new Ok<>(value);
    }

    /**
     * Creates a failure Result with an embedded error
     * @param <E> error
     */
    static <V, E> Result<V, E> err(@NonNull final E error) {
        return new Err<>(error);
    }

    /**
     * Returns the Result's value or null if the Result contains error
     */
    @Nullable
    V getValue();

    /**
     * Returns the Result's error or null if the Result contains no error
     */
    @Nullable
    E getError();

    /**
     * Returns {@code true} if the Result contains a valid value, {@code false} if it has error.
     */
    boolean isOk();

    /**
     * Returns {@code true} if the Result contains error, {@code false} otherwise.
     */
    boolean isErr();
}