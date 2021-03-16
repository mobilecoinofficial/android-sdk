// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Configurable log output conduit
 */
public interface LogAdapter {

    /**
     * Determine whether the log entry should be logged or not
     *
     * @param logLevel is the level of log priority and importance e.g. DEBUG, WARNING
     * @param tag      is the given tag for the log message
     */
    boolean isLoggable(Logger.Level logLevel, @NonNull String tag);

    /**
     * Each log will use this pipeline
     *
     * @param logLevel  is the level of log priority and importance e.g. DEBUG, WARNING
     * @param tag       is the given tag for the log message.
     * @param message   is the given message for the log message.
     * @param throwable (optional) an exception to log
     */
    void log(Logger.Level logLevel, @NonNull String tag, @NonNull String message,
             @Nullable Throwable throwable, @NonNull Object... metadata);
}
