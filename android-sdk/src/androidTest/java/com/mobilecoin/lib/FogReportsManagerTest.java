package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.uri.FogUri;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class FogReportsManagerTest {

    @Test
    public void testFogUri() throws InvalidUriException, NetworkException, FogReportException {
        FogReportsManager reportsManager = new FogReportsManager(Environment.getTestFogConfig().getTransportProtocol());
        Set<FogUri> fogUris = new HashSet<FogUri>();
        fogUris.add(new FogUri(Environment.getTestFogConfig().getFogUri()));
        reportsManager.fetchReports(fogUris, UnsignedLong.MAX_VALUE, TestFogConfig.getFogConfig(Environment.CURRENT_TEST_ENV).getClientConfig().report);
    }

    @Test
    public void testShortFogUri() throws InvalidUriException, NetworkException, FogReportException {
        FogReportsManager reportsManager = new FogReportsManager(Environment.getTestFogConfig().getTransportProtocol());
        Set<FogUri> fogUris = new HashSet<FogUri>();
        TestFogConfig testNetConfig = TestFogConfig.getFogConfig(Environment.TestEnvironment.TEST_NET);
        fogUris.add(new FogUri(testNetConfig.getShortFogUri()));
        reportsManager.fetchReports(fogUris, UnsignedLong.MAX_VALUE, TestFogConfig.getFogConfig(Environment.CURRENT_TEST_ENV).getClientConfig().report);
    }

}
