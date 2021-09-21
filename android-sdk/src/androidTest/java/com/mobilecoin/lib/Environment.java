// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.VisibleForTesting;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.network.TransportProtocol;

@VisibleForTesting
public class Environment {
    public static final TestEnvironment CURRENT_TEST_ENV = TestEnvironment.ALPHA;

    static public MobileCoinClient makeFreshMobileCoinClient() throws InvalidUriException {
        AccountKey accountKey = TestKeysManager.getNextAccountKey();
        return makeFreshMobileCoinClient(accountKey);
    }

    static public TestFogConfig getTestFogConfig() {
        return TestFogConfig.getFogConfig(CURRENT_TEST_ENV);
    }

    static public TestFogConfig getTestFogConfig(StorageAdapter storageAdapter) {
        return TestFogConfig.getFogConfig(CURRENT_TEST_ENV, storageAdapter);
    }

    static public MobileCoinClient makeFreshMobileCoinClient(AccountKey accountKey) throws InvalidUriException {
        TestFogConfig fogConfig = getTestFogConfig();
        return makeFreshMobileCoinClient(fogConfig, accountKey);
    }

    static public MobileCoinClient makeFreshMobileCoinClient(StorageAdapter storageAdapter) throws InvalidUriException { AccountKey accountKey = TestKeysManager.getNextAccountKey();
        TestFogConfig fogConfig = getTestFogConfig(storageAdapter);
        return makeFreshMobileCoinClient(fogConfig, accountKey);
    }

    static private MobileCoinClient makeFreshMobileCoinClient(TestFogConfig fogConfig, AccountKey accountKey) throws  InvalidUriException {
        MobileCoinClient mobileCoinClient = new MobileCoinClient(
                accountKey,
                fogConfig.getFogUri(),
                fogConfig.getConsensusUri(),
                fogConfig.getClientConfig()
        );
        mobileCoinClient.setFogBasicAuthorization(
                fogConfig.getUsername(),
                fogConfig.getPassword()
        );
        mobileCoinClient.setConsensusBasicAuthorization(
                fogConfig.getUsername(),
                fogConfig.getPassword()
        );
        mobileCoinClient.setTransportProtocol(TransportProtocol.forHTTP(new SimpleRequester()));
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