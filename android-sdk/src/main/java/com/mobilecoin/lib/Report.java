// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.log.Logger;

import java.util.List;

import report.ReportOuterClass;

class Report extends Native {
    private final static String TAG = Report.class.getName();
    private final long publicKeyExpiry;

    Report(@NonNull String fogReportId,
           @NonNull VerificationReport verificationReport,
           long publicKeyExpiry
    ) throws FogReportException {
        this.publicKeyExpiry = publicKeyExpiry;
        try {
            init_jni(fogReportId, verificationReport, publicKeyExpiry);
        } catch (Exception exception) {
            throw new FogReportException("Unable to create report from the provided arguments",
                    exception);
        }
    }

    @NonNull
    static Report fromProtoBuf(@NonNull ReportOuterClass.Report proto) throws FogReportException {
        Logger.i(TAG, "Deserializing report from protobuf");
        // Signature
        VerificationSignature verificationSignature =
                new VerificationSignature(proto.getReport().getSig().getContents().toByteArray());

        // Chain list
        List<ByteString> chainList = proto.getReport().getChainList();
        byte[][] chain = new byte[chainList.size()][];
        for (int i = 0; i < chainList.size(); i++) {
            chain[i] = chainList.get(i).toByteArray();
        }

        // VerificationReport
        VerificationReport verificationReport = new VerificationReport(
                verificationSignature,
                chain,
                proto.getReport().getHttpBody());

        // Report
        return new Report(
                proto.getFogReportId(),
                verificationReport,
                proto.getPubkeyExpiry()
        );
    }

    public long getPublicKeyExpiry() {
        Logger.i(TAG, "Getting public key expiry", null, publicKeyExpiry);
        return publicKeyExpiry;
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
    private native void init_jni(@NonNull String report_id,
                                 @NonNull VerificationReport verification_report,
                                 long public_key_expiry);

    private native void finalize_jni();
}
