package com.mobilecoin.lib.network.services;

import com.mobilecoin.lib.exceptions.NetworkException;

import report.ReportOuterClass;

public interface FogReportService {
   ReportOuterClass.ReportResponse getReports(ReportOuterClass.ReportRequest request) throws NetworkException;
}
