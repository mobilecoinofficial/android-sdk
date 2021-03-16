// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.log;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.BuildConfig;

/**
 * Debug log adapter logs only in debug mode
 */
public class DebugLogAdapter implements LogAdapter {
    @Override
    public boolean isLoggable(Logger.Level logLevel, @NonNull String tag) {
        // Log only in debug mode
        return BuildConfig.DEBUG;
    }

    @Override
    public void log(Logger.Level logLevel, @NonNull String tag, @NonNull String message,
                    @Nullable Throwable throwable, @NonNull Object... metadata) {
        switch (logLevel) {
            case INFO:
                Log.i(tag, message + printMetadataObjects(metadata), throwable);
                break;
            case VERBOSE:
                Log.v(tag, message + printMetadataObjects(metadata), throwable);
                break;
            case DEBUG:
                Log.d(tag, message + printMetadataObjects(metadata), throwable);
                break;
            case WARNING:
                Log.w(tag, message + printMetadataObjects(metadata), throwable);
                break;
            case ERROR:
                Log.e(tag, message + printMetadataObjects(metadata), throwable);
                break;
            case WTF:
                Log.wtf(tag, message + printMetadataObjects(metadata), throwable);
                break;
        }
    }

    @NonNull
    public String printMetadataObject(@Nullable Object object) {
        return object == null ? "(null)" : object.toString();
    }

    @NonNull
    public String printMetadataObjects(@NonNull Object... objects) {
        StringBuilder metadataStringBuilder = new StringBuilder();
        for (Object object : objects) {
            String metadataString = printMetadataObject(object);
            metadataStringBuilder
                    .append(" ")
                    .append(metadataString);
        }
        return metadataStringBuilder.toString();
    }

}
