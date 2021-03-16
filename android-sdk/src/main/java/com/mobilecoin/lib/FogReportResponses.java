// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.log.Logger;

class FogReportResponses extends Native {
    private static final String TAG = FogReportResponses.class.getName();

    FogReportResponses() throws FogReportException {
        try {
            init_jni();
        } catch (Exception exception) {
            FogReportException fogReportException =
                    new FogReportException("Unable to create fog report responses container",
                        exception);
            Util.logException(TAG, fogReportException);
            throw fogReportException;
        }
    }

    synchronized void addResponse(@NonNull Uri fogUri, @NonNull ReportResponse reportResponse)
            throws FogReportException {
        try {
            add_response(fogUri.toString(), reportResponse);
        } catch (Exception exception) {
            FogReportException fogReportException =
                    new FogReportException("Unable to add response", exception);
            Util.logException(TAG, fogReportException);
            throw fogReportException;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            finalize_jni();
        } catch (Exception exception) {
            Logger.e(TAG, "Failed to free fog report data", exception);
        }
        super.finalize();
    }

    // native calls
    private native void init_jni();

    private native void add_response(@NonNull String fogUri,
                                     @NonNull ReportResponse report_response);

    private native void finalize_jni();
}
