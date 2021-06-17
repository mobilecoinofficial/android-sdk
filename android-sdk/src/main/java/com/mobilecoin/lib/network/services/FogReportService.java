package com.mobilecoin.lib.network.services;

import report.ReportOuterClass;

public interface FogReportService {
   ReportOuterClass.ReportResponse getReports(ReportOuterClass.ReportRequest request);
}
