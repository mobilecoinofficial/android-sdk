// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.mobilecoin.lib.ClientConfig.Service;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.FogReportService;

import java.util.ArrayList;
import java.util.List;

import report.ReportOuterClass;

/**
 * ReportClient
 */
final class ReportClient extends AnyClient {
    private static final String TAG = ReportClient.class.getName();

    /**
     * Creates and initializes an instance of {@link ReportClient}
     *
     * @param loadBalancer address of the service.
     */
    ReportClient(@NonNull LoadBalancer loadBalancer,
                 @NonNull Service serviceConfig,
                 @NonNull TransportProtocol transportProtocol) {
        super(loadBalancer, serviceConfig, transportProtocol);
    }

    /**
     * Retrieve the public fog key for the current service
     *
     * @return fog's {@link RistrettoPublic} or null if a corresponding key cannot be found
     */
    @NonNull
    synchronized ReportResponse getReports()
            throws InvalidFogResponse, AttestationException, NetworkException {
        Logger.i(TAG, "Retrieving the fog public key");
        ReportOuterClass.ReportRequest reportRequest =
                ReportOuterClass.ReportRequest.newBuilder().build();
        FogReportService service =
                getAPIManager().getFogReportService(getNetworkTransport());
        try {
            ReportOuterClass.ReportResponse response = service.getReports(reportRequest);
            List<ReportOuterClass.Report> protoReports = response.getReportsList();
            ArrayList<FogReport> reports = new ArrayList<>();
            for (ReportOuterClass.Report report : protoReports) {
                reports.add(FogReport.fromProtoBuf(report));
            }
            if (reports.isEmpty()) {
                InvalidFogResponse invalidFogResponse =
                        new InvalidFogResponse("No valid reports can be found");
                Util.logException(TAG, invalidFogResponse);
                throw invalidFogResponse;
            }
            List<ByteString> chainList = response.getChainList();
            byte[][] chain = new byte[chainList.size()][];

            for (int i = 0; i < chainList.size(); i++) {
                chain[i] = chainList.get(i).toByteArray();
            }
            return new ReportResponse(reports, chain, response.getSignature().toByteArray());
        } catch (NetworkException exception) {
            Logger.w(TAG, "Error retrieving the fog public key", exception);
            throw exception;
        } catch (Throwable throwable) {
            Logger.w(TAG, "Error retrieving the fog reports", throwable);
            throw new InvalidFogResponse("Unable to retrieve the fog report", throwable);
        }
    }
}
