package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.FogReportService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import report.ReportAPIHttp;
import report.ReportOuterClass;

public class RestFogReportService extends RestService implements FogReportService {

    public RestFogReportService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public ReportOuterClass.ReportResponse getReports(ReportOuterClass.ReportRequest request) throws NetworkException {
        try {
            return ReportAPIHttp.getReports(request, getRestClient());
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
        }
    }
}
