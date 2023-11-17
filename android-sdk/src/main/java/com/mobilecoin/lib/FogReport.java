// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.log.Logger;

import java.util.List;

import report.ReportOuterClass;

final class FogReport extends Native {
    private final static String TAG = FogReport.class.getName();
    private final long publicKeyExpiry;

    FogReport(@NonNull String fogReportId,
              @NonNull VerificationReport verificationReport,
              long publicKeyExpiry
    ) throws FogReportException {
        this.publicKeyExpiry = publicKeyExpiry;
        try {
            init_with_verification_report(fogReportId, verificationReport, publicKeyExpiry);
        } catch (Exception exception) {
            throw new FogReportException("Unable to create report from the provided arguments",
                    exception);
        }
    }

    FogReport(@NonNull String fogReportId,
              @NonNull MobileCoinAPI.DcapEvidence dcapEvidence,
              long publicKeyExpiry
    ) throws FogReportException {
        this.publicKeyExpiry = publicKeyExpiry;
        try {
            init_with_dcap_evidence(fogReportId, dcapEvidence.toByteArray(), publicKeyExpiry);
        } catch (Exception exception) {
            throw new FogReportException("Unable to create report from the provided arguments",
                    exception);
        }
    }

    @NonNull
    static FogReport fromProtoBuf(@NonNull ReportOuterClass.Report proto) throws FogReportException {
        Logger.i(TAG, "Deserializing report from protobuf");
        if(!proto.hasDcapEvidence()) {
            // Initialize from VerificationReport
            // Signature
            VerificationSignature verificationSignature =
                    new VerificationSignature(proto.getVerificationReport().getSig().getContents().toByteArray());

            // Chain list
            List<ByteString> chainList = proto.getVerificationReport().getChainList();
            byte[][] chain = new byte[chainList.size()][];
            for (int i = 0; i < chainList.size(); i++) {
                chain[i] = chainList.get(i).toByteArray();
            }

            // VerificationReport
            VerificationReport verificationReport = new VerificationReport(
                    verificationSignature,
                    chain,
                    proto.getVerificationReport().getHttpBody());

            // Report
            return new FogReport(
                    proto.getFogReportId(),
                    verificationReport,
                    proto.getPubkeyExpiry()
            );
        }
        else {
            // Initialize from DcapEvidence
            return new FogReport(
                    proto.getFogReportId(),
                    proto.getDcapEvidence(),
                    proto.getPubkeyExpiry()
            );
        }
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
    private native void init_with_verification_report(
            @NonNull String report_id,
            @NonNull VerificationReport verification_report,
            long public_key_expiry
    );

    private native void init_with_dcap_evidence(
            @NonNull String report_id,
            byte[] dcap_evidence_bytes,
            long public_key_expiry
    );

    private native void finalize_jni();
}
