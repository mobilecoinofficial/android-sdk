// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.log.Logger;

import java.util.List;

class ReportResponse extends Native {
    private final static String TAG = ReportResponse.class.getName();
    private final List<Report> reports;
    private final byte[][] chain;
    private final byte[] signature;

    ReportResponse(@NonNull List<Report> reports, @NonNull byte[][] chain,
                   @NonNull byte[] signature) throws FogReportException {
        this.reports = reports;
        this.chain = chain;
        this.signature = signature;
        try {
            init_jni((reports.toArray(new Report[0])), chain, signature);
        } catch (Exception exception) {
            FogReportException fogReportException =
                    new FogReportException("Unable to create report response", exception);
            Util.logException(TAG, fogReportException);
            throw fogReportException;
        }
    }

    @NonNull
    public List<Report> getReports() {
        Logger.i(TAG, "Getting reports", null, reports);
        return reports;
    }

    @NonNull
    public byte[][] getChain() {
        Logger.i(TAG, "Getting chain");
        return chain;
    }

    @NonNull
    public byte[] getSignature() {
        Logger.i(TAG, "Getting signature", null, signature);
        return signature;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            finalize_jni();
        } catch (Exception exception) {
            Logger.e(TAG, "Failed to free fog report response data", exception);
        }
        super.finalize();
    }

    // native calls
    private native void init_jni(@NonNull Report[] reports, @NonNull byte[][] chain,
                                 @NonNull byte[] signature);

    private native void finalize_jni();
}
