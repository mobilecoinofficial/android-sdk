// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

class TestKeysManager {
    private static final int DEFAULT_ACCOUNT_INDEX = 0;
    private static int currentAccountIndex = 0;

    private final static String[] testNetMnemonics = loadTestMnemonics(com.mobilecoin.lib.test.R.raw.test_net_mnemonics);

    private static String[] loadTestMnemonics(int resource) {
        InputStream inputStream = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getResources().openRawResource(resource);
       Scanner scanner = new Scanner(inputStream).useDelimiter(",");
        ArrayList<String> mnemonics = new ArrayList<>();
        while (scanner.hasNext()) {
            String mnemonic = scanner.next();
            mnemonics.add(mnemonic);
        }
        return mnemonics.toArray(new String[0]);
    }

    synchronized static String getNextMnemonicPhrase() {
        String[] currentMnemonics;
        switch (Environment.CURRENT_TEST_ENV) {
            case TEST_NET:
                currentMnemonics = testNetMnemonics;
                break;
            case MOBILE_DEV:
            case ALPHA:
            default:
                // TODO: enter the new dev env keys
                throw new UnsupportedOperationException();
        }
        if (currentAccountIndex >= currentMnemonics.length) {
            currentAccountIndex = 0;
        }
        return currentMnemonics[currentAccountIndex++];
    }

    static AccountKey getNextAccountKey() {
        TestFogConfig fogConfig = Environment.getTestFogConfig();
        try {
            return AccountKey.fromMnemonicPhrase(
                    getNextMnemonicPhrase(),
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
