package com.mobilecoin.lib.network.services.grpc;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.grpc.AuthInterceptor;
import com.mobilecoin.lib.network.grpc.CookieInterceptor;
import com.mobilecoin.lib.network.grpc.GRPCStatusResponse;
import com.mobilecoin.lib.network.services.FogReportService;

import java.util.concurrent.ExecutorService;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
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
    protected ReportAPIGrpc.ReportAPIBlockingStub newBlockingStub(@NonNull ManagedChannel managedChannel) {
        return ReportAPIGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public ReportOuterClass.ReportResponse getReports(ReportOuterClass.ReportRequest request) throws NetworkException {
        try {
            return getApiBlockingStub().getReports(request);
        } catch (StatusRuntimeException e) {
            throw new NetworkException(new NetworkResult(new GRPCStatusResponse(e.getStatus())), e);
        }
    }
}
