// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.MobileCoinException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.uri.FogUri;
import com.mobilecoin.lib.util.Result;
import com.mobilecoin.lib.util.Task;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

final class FogReportsManager {
    private final static String TAG = FogReportsManager.class.getName();
    private static final int MAX_FETCH_THREADS = 10;
    // timeout the network call if it's more than 5 minutes
    private static final int MAX_WAIT_TIME_SECONDS = 300;
    private final HashMap<FogUri, ReportResponse> cachedResponses;

    private TransportProtocol transportProtocol;

    FogReportsManager(@NonNull TransportProtocol transportProtocol) {
        cachedResponses = new HashMap<>();
        this.transportProtocol = transportProtocol;
    }

    @Nullable
    synchronized ReportResponse getCachedReportResponse(@NonNull FogUri fogUri,
                                                        @NonNull UnsignedLong tombstoneBlockIndex
    ) {
        Logger.i(TAG, "Checking for cached report response");
        ReportResponse response = cachedResponses.get(fogUri);
        if (response != null) {
            Optional<Report> report = response.getReports().stream()
                    .min((o1, o2) -> Long.compare(
                            o1.getPublicKeyExpiry(),
                            o2.getPublicKeyExpiry()));
            if (report.isPresent()) {
                UnsignedLong reportExpiryBlockIndex =
                        UnsignedLong.fromLongBits(report.get().getPublicKeyExpiry());
                if (reportExpiryBlockIndex.compareTo(tombstoneBlockIndex) < 0) {
                    cachedResponses.remove(fogUri);
                    response = null;
                }
            }
            Logger.i(TAG, "Got cached report response", null,
                    "fogUri:", fogUri,
                    "response:", response);
        } else {
            Logger.i(TAG, "Found no cached responses");
        }
        return response;
    }

    synchronized void cacheReportResponse(@NonNull FogUri fogUri,
                                          @NonNull ReportResponse reportResponse
    ) {
        Logger.i(TAG, "Caching report response", null, fogUri, reportResponse);
        cachedResponses.put(fogUri, reportResponse);
    }

    @NonNull
    FogReportResponses fetchReports(@NonNull Set<FogUri> fogUriList,
                                    @NonNull UnsignedLong tombstoneBlockIndex,
                                    @NonNull ClientConfig.Service serviceConfig
    ) throws NetworkException, FogReportException {
        Logger.i(TAG, "Fetching reports from fogUriList and tombstone block index", null,
                fogUriList, tombstoneBlockIndex);
        FogReportResponses fogReportResponses = new FogReportResponses();
        ExecutorService fixedExecutorService =
                Executors.newFixedThreadPool(Math.min(fogUriList.size(),
                        MAX_FETCH_THREADS));
        HashMap<FogUri, Task<ReportResponse, Exception>> reportTasks = new HashMap<>();

        for (FogUri fogUri : fogUriList) {
            ReportResponse response = getCachedReportResponse(fogUri, tombstoneBlockIndex);
            // if there is no valid cached report for the fogUri,
            // create a task to fetch it
            if (response == null) {
                Task<ReportResponse, Exception> task = new Task<ReportResponse, Exception>() {
                    @Override
                    public ReportResponse execute() throws Exception {
                        ReportClient reportClient = new ReportClient(
                            RandomLoadBalancer.create(fogUri),
                            serviceConfig,
                            transportProtocol);
                        reportClient.setTransportProtocol(FogReportsManager.this.transportProtocol);
                        ReportResponse response = reportClient.getReports();
                        reportClient.shutdown();
                        return response;
                    }
                };
                reportTasks.put(fogUri, task);
            } else {
                fogReportResponses.addResponse(fogUri.getUri(), response);
            }
        }
        try {
            HashMap<FogUri, Future<Result<ReportResponse, Exception>>> futures = new HashMap<>();
            for (FogUri uri : reportTasks.keySet()) {
                Future<Result<ReportResponse, Exception>> future =
                        fixedExecutorService.submit(reportTasks.get(uri));
                futures.put(uri, future);
            }
            fixedExecutorService.shutdown();
            // wait for the executor service to complete all tasks
            fixedExecutorService.awaitTermination(MAX_WAIT_TIME_SECONDS, TimeUnit.SECONDS);

            HashMap<FogUri, Result<ReportResponse, Exception>> taskResults = new HashMap<>();
            for (FogUri uri : futures.keySet()) {
                Future<Result<ReportResponse, Exception>> future = futures.get(uri);
                if (future != null) {
                    Result<ReportResponse, Exception> responseResult = future.get();
                    taskResults.put(uri, responseResult);
                }
            }
            for (FogUri fogUri : taskResults.keySet()) {
                Result<ReportResponse, Exception> responseResult = taskResults.get(fogUri);
                if (responseResult != null && responseResult.isOk()) {
                    fogReportResponses.addResponse(fogUri.getUri(), responseResult.getValue());
                    cacheReportResponse(fogUri, responseResult.getValue());
                } else {
                    throw responseResult.getError();
                }
            }
        } catch (InterruptedException | ExecutionException exception) {
            NetworkException networkException =
                    new NetworkException(NetworkResult.DEADLINE_EXCEEDED
                            .withDescription("Timeout fetching fog reports")
                            .withCause(exception));
            Util.logException(TAG, networkException);
            throw networkException;
        } catch (MobileCoinException | RuntimeException exception) {
            FogReportException fogReportException =
                    new FogReportException("Unable to fetch Fog reports", exception);
            Util.logException(TAG, fogReportException);
            throw fogReportException;
        } catch (Exception exception) {
            Logger.wtf(TAG, "Unexpected exception", exception);
            throw new IllegalStateException(exception);
        }
        return fogReportResponses;
    }

    public void setTransportProtocol(TransportProtocol transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    public TransportProtocol getTransportProtocol() {
        return this.transportProtocol;
    }

}