// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.VisibleForTesting;

@VisibleForTesting
public class Environment {
    public static final TestEnvironment CURRENT_TEST_ENV = TestEnvironment.MOBILE_DEV;

    static public TestFogConfig getTestFogConfig() {
        return TestFogConfig.getFogConfig(CURRENT_TEST_ENV);
    }

    static public TestFogConfig getTestFogConfig(StorageAdapter storageAdapter) {
        return TestFogConfig.getFogConfig(CURRENT_TEST_ENV, storageAdapter);
    }

    enum TestEnvironment {
        MOBILE_DEV("mc-dev-testing.development"),
        ALPHA("alpha.development"),
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