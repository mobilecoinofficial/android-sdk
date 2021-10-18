// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.VisibleForTesting;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.network.TransportProtocol;

@VisibleForTesting
public class Environment {
    public static final TestEnvironment CURRENT_TEST_ENV = TestEnvironment.ALPHA;

    static public TestFogConfig getTestFogConfig() {
        return TestFogConfig.getFogConfig(CURRENT_TEST_ENV);
    }

    static public TestFogConfig getTestFogConfig(StorageAdapter storageAdapter) {
        return TestFogConfig.getFogConfig(CURRENT_TEST_ENV, storageAdapter);
    }

    enum TestEnvironment {
        MOBILE_DEV("mobiledev"),
        ALPHA("alpha"),
        TEST_NET("test");

        private final String name;

        TestEnvironment(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}