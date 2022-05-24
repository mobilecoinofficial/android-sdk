// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.test.platform.app.InstrumentationRegistry;

import com.mobilecoin.lib.util.Hex;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

class TestKeysManager {
    private static final int DEFAULT_ACCOUNT_INDEX = 0;
    private static int currentAccountIndex = 0;

    private static final String testNetMnemonics[] =
            loadTestStrings(com.mobilecoin.lib.test.R.raw.test_net_mnemonics);
    private static final String devNetRootEntropies[] =
            loadTestStrings(com.mobilecoin.lib.test.R.raw.dev_net_root_entropies);
    private static final String devNetMnemonics[] =
            loadTestStrings(com.mobilecoin.lib.test.R.raw.dev_net_mnemonics);

    private static String[] loadTestStrings(int resource) {
        InputStream inputStream = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getResources().openRawResource(resource);
        Scanner scanner = new Scanner(inputStream).useDelimiter(",");
        ArrayList<String> strings = new ArrayList<>();
        while (scanner.hasNext()) {
            String string = scanner.next();
            strings.add(string);
        }
        return strings.toArray(new String[0]);
    }

    static int getTotalTestKeysCount() {
        switch(Environment.CURRENT_TEST_ENV) {
            case ALPHA:
                return devNetRootEntropies.length;
            case TEST_NET:
                return testNetMnemonics.length;
            default:
                return devNetMnemonics.length;
        }
    }

    static AccountKey getNextAccountKey() {
        TestFogConfig fogConfig = Environment.getTestFogConfig();
        switch (Environment.CURRENT_TEST_ENV) {
            case TEST_NET:
                if (currentAccountIndex >= testNetMnemonics.length) {
                    currentAccountIndex = 0;
                }
                try {
                    return AccountKey.fromMnemonicPhrase(
                            testNetMnemonics[currentAccountIndex++],
                            DEFAULT_ACCOUNT_INDEX,
                            fogConfig.getFogUri(),
                            fogConfig.getFogReportId(),
                            fogConfig.getFogAuthoritySpki()
                    );
                } catch (Exception exception) {
                    throw new IllegalStateException("Bug: All test keys must be valid");
                }
            case ALPHA:
                if (currentAccountIndex >= devNetRootEntropies.length) {
                    currentAccountIndex = 0;
                }
                try {
                    return AccountKey.fromRootEntropy(
                            Hex.toByteArray(devNetRootEntropies[currentAccountIndex++]),
                            fogConfig.getFogUri(),
                            fogConfig.getFogReportId(),
                            fogConfig.getFogAuthoritySpki()
                    );
                } catch (Exception exception) {
                    throw new IllegalStateException("Bug: All test keys must be valid");
                }
            case MOBILE_DEV:
            default:
                if (currentAccountIndex >= devNetMnemonics.length) {
                    currentAccountIndex = 0;
                }
                try {
                    return AccountKey.fromMnemonicPhrase(
                            devNetMnemonics[currentAccountIndex++],
                            DEFAULT_ACCOUNT_INDEX,
                            fogConfig.getFogUri(),
                            fogConfig.getFogReportId(),
                            fogConfig.getFogAuthoritySpki()
                    );
                } catch (Exception exception) {
                    throw new IllegalStateException("Bug: All test keys must be valid");
                }
        }
    }
}
