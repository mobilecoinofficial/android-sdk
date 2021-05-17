// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.network.uri.ConsensusUri;
import com.mobilecoin.lib.network.uri.FogUri;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MobileUrlTest {

    @Test
    public void test_constructors() throws InvalidUriException {
        // test secure constructors
        new FogUri("fog://server.com");
        new ConsensusUri("mc://server.com");

        // test insecure constructors
        new FogUri("insecure-fog://server.com");
        new ConsensusUri("insecure-mc://server.com");
    }

    @Test
    public void test_constructors_with_ports() throws InvalidUriException {
        // test secure constructors
        new FogUri("fog://server.com:1234");
        new ConsensusUri("mc://server.com:7321");

        // test insecure constructors
        new FogUri("insecure-fog://server.com:1337");
        new ConsensusUri("insecure-mc://server.com:2345");
    }

    @Test
    public void test_wrong_schemes() {
        try {
            new FogUri("mc://server.com");
            Assert.fail("Bad schemes should fail");
        } catch (Exception ignored) { }
        try {
            new ConsensusUri("fog-ledger://server.com");
            Assert.fail("Bad schemes should fail");
        } catch (Exception ignored) { }
    }

    @Test
    public void test_no_schemes() {
        try {
            new FogUri("server.com");
            Assert.fail("Empty schemes should fail");
        } catch (Exception ignored) { }
        try {
            new ConsensusUri("server.com");
            Assert.fail("Empty schemes should fail");
        } catch (Exception ignored) { }
    }

    @Test
    public void test_no_host() {
        try {
            new FogUri("fog://");
            Assert.fail("No host should fail");
        } catch (Exception ignored) { }
        try {
            new ConsensusUri("mc://");
            Assert.fail("No host should fail");
        } catch (Exception ignored) { }
    }
}


