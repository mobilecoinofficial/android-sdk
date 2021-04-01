// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.VisibleForTesting;

import com.mobilecoin.lib.exceptions.InvalidUriException;

@VisibleForTesting
public class Environment {
    public static final TestEnvironment CURRENT_TEST_ENV = TestEnvironment.TEST_NET;

    static public MobileCoinClient makeFreshMobileCoinClient() throws InvalidUriException {
        AccountKey accountKey = TestKeysManager.getNextAccountKey();
        return makeFreshMobileCoinClient(accountKey);
    }

    static public TestFogConfig getTestFogConfig() {
        return TestFogConfig.getFogConfig(CURRENT_TEST_ENV);
    }

    static public MobileCoinClient makeFreshMobileCoinClient(AccountKey accountKey) throws InvalidUriException {
        TestFogConfig fogConfig = getTestFogConfig();
        MobileCoinClient mobileCoinClient = new MobileCoinClient(
                accountKey,
                fogConfig.getFogUri(),
                fogConfig.getConsensusUri(),
                fogConfig.getClientConfig()
        );
        mobileCoinClient.setAuthorization(
                fogConfig.getUsername(),
                fogConfig.getPassword()
        );
        return mobileCoinClient;
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