package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.AuthInterceptor;
import com.mobilecoin.lib.network.CookieInterceptor;
import com.mobilecoin.lib.network.services.FogReportService;

import java.util.concurrent.ExecutorService;

import io.grpc.ManagedChannel;
import report.ReportAPIGrpc;
import report.ReportOuterClass;

public class GRPCFogReportService
        extends GRPCService<ReportAPIGrpc.ReportAPIBlockingStub>
        implements FogReportService {
    public GRPCFogReportService(@NonNull ManagedChannel managedChannel,
                                @NonNull CookieInterceptor cookieInterceptor,
                                @NonNull AuthInterceptor authInterceptor,
                                @NonNull ExecutorService executorService) {
        super(managedChannel, cookieInterceptor, authInterceptor, executorService);
    }

    @NonNull
    @Override
    ReportAPIGrpc.ReportAPIBlockingStub newBlockingStub(@NonNull ManagedChannel managedChannel) {
        return ReportAPIGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public ReportOuterClass.ReportResponse getReports(ReportOuterClass.ReportRequest request) {
        return getApiBlockingStub().getReports(request);
    }
}
