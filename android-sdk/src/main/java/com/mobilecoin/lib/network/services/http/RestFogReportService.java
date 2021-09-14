package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.network.services.FogReportService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import report.ReportOuterClass;

public class RestFogReportService extends RestService implements FogReportService {
    public static final String SERVICE_NAME = "report.ReportAPI";

    public RestFogReportService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public ReportOuterClass.ReportResponse getReports(ReportOuterClass.ReportRequest request) {
        try {
            byte[] responseData = getRestClient().makeRequest(
                    PREFIX + SERVICE_NAME + "/" + "GetReports",
                    request.toByteArray()
            );
            return ReportOuterClass.ReportResponse.parseFrom(responseData);
        } catch (InvalidProtocolBufferException exception) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
        }
    }
}
