// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Logger class delivers log messages from the lib/app modules to the provided {@link LogAdapter}s
 */
public final class Logger {
    @NonNull
    private static final ArrayList<LogAdapter> adapters =
            new ArrayList<>(Collections.singletonList(new DebugLogAdapter()));

    private Logger() { /* no public constructor */ }

    /**
     * Add a log adapter to receive logs
     */
    public static synchronized void addAdapter(@NonNull LogAdapter adapter) {
        adapters.add(adapter);
    }

    /**
     * Remove previously added log adapter
     */
    public static synchronized void removeAdapter(@NonNull LogAdapter adapter) {
        adapters.remove(adapter);
    }

    /**
     * Remove all previously added log adapters
     */
    public static synchronized void clearAllAdapters() {
        adapters.clear();
    }

    /**
     * Log an informational message
     */
    public static void i(@NonNull String tag, @NonNull String message,
                         @Nullable Throwable throwable, Object... metadata) {
        logMessage(Level.INFO, tag, message, throwable, metadata);
    }

    public static void i(@NonNull String tag, @NonNull String message) {
        i(tag, message, null);
    }

    /**
     * Log a verbose message
     */
    public static void v(@NonNull String tag, @NonNull String message,
                         @Nullable Throwable throwable, Object... metadata) {
        logMessage(Level.VERBOSE, tag, message, throwable, metadata);
    }

    public static void v(@NonNull String tag, @NonNull String message) {
        v(tag, message, null);
    }

    /**
     * Log debug message
     */
    public static void d(@NonNull String tag, @NonNull String message,
                         @Nullable Throwable throwable, Object... metadata) {
        logMessage(Level.DEBUG, tag, message, throwable, metadata);
    }

    public static void d(@NonNull String tag, @NonNull String message) {
        d(tag, message, null);
    }

    /**
     * Log warning message
     */
    public static void w(@NonNull String tag, @NonNull String message,
                         @Nullable Throwable throwable, Object... metadata) {
        logMessage(Level.WARNING, tag, message, throwable, metadata);
    }

    public static void w(@NonNull String tag, @NonNull String message) {
        w(tag, message, null);
    }

    /**
     * Log error message
     */
    public static void e(@NonNull String tag, @NonNull String message,
                         @Nullable Throwable throwable, Object... metadata) {
        logMessage(Level.ERROR, tag, message, throwable, metadata);
    }

    public static void e(@NonNull String tag, @NonNull String message) {
        e(tag, message, null);
    }

    public static void e(@NonNull String tag, @NonNull Throwable throwable, Object... metadata) {
        e(tag, throwable.getMessage(), throwable, metadata);
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     */
    public static void wtf(@NonNull String tag, @NonNull String message,
                           @Nullable Throwable throwable, Object... metadata) {
        logMessage(Level.WTF, tag, message, throwable, metadata);
    }

    public static void wtf(@NonNull String tag, @NonNull String message) {
        wtf(tag, message, null);
    }

    private static synchronized void logMessage(Level logLevel, @NonNull String tag,
                                                @NonNull String message,
                                                @Nullable Throwable throwable,
                                                @NonNull Object... metadata) {
        for (LogAdapter logAdapter : adapters) {
            if (logAdapter.isLoggable(logLevel, tag)) {
                logAdapter.log(logLevel, tag, message, throwable, metadata);
            }
        }
    }

    public enum Level {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        WTF
    }
}
