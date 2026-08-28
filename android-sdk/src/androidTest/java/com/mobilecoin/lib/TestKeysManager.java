// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import com.mobilecoin.lib.util.Hex;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

class TestKeysManager {
    private static final int DEFAULT_ACCOUNT_INDEX = 0;
    private static int currentAccountIndex = 0;

    /**
     * Accounts handed out since {@link #forgetIssuedKeys()}, by their index in
     * the configured list.
     * <p>
     * {@link #getNextAccountKey()} rotates, so which account a test runs
     * against depends on how many ran before it and is not recoverable from a
     * failure. See {@link FundingDiagnosticRule}.
     */
    private static final Map<Integer, AccountKey> issuedKeys = new LinkedHashMap<>();

    static void forgetIssuedKeys() {
        synchronized (issuedKeys) {
            issuedKeys.clear();
        }
    }

    /**
     * A snapshot, not a view: {@link FundingDiagnosticRule} builds a client per
     * entry while it reports, and a draw landing during that would otherwise
     * throw a {@code ConcurrentModificationException} in place of the failure
     * being diagnosed.
     */
    @NonNull
    static Map<Integer, AccountKey> getIssuedKeys() {
        synchronized (issuedKeys) {
            return new LinkedHashMap<>(issuedKeys);
        }
    }

    @NonNull
    private static AccountKey recordIssued(final int index, @NonNull final AccountKey key) {
        synchronized (issuedKeys) {
            issuedKeys.put(index, key);
        }
        return key;
    }

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
            strings.add(string.trim());
        }
        return strings.toArray(new String[0]);
    }

    static int getTotalTestKeysCount() {
        switch(Environment.CURRENT_TEST_ENV) {
            case ALPHA:
            case MOBILE_DEV:
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
                    final int index = currentAccountIndex++;
                    return recordIssued(index, AccountKey.fromMnemonicPhrase(
                            testNetMnemonics[index],
                            DEFAULT_ACCOUNT_INDEX,
                            fogConfig.getFogUri(),
                            fogConfig.getFogReportId(),
                            fogConfig.getFogAuthoritySpki()
                    ));
                } catch (Exception exception) {
                    throw new IllegalStateException("Bug: All test keys must be valid");
                }
            case ALPHA:
            case MOBILE_DEV:
                if (currentAccountIndex >= devNetRootEntropies.length) {
                    currentAccountIndex = 0;
                }
                try {
                    final int index = currentAccountIndex++;
                    return recordIssued(index, AccountKey.fromRootEntropy(
                            Hex.toByteArray(devNetRootEntropies[index]),
                            fogConfig.getFogUri(),
                            fogConfig.getFogReportId(),
                            fogConfig.getFogAuthoritySpki()
                    ));
                } catch (Exception exception) {
                    throw new IllegalStateException("Bug: All test keys must be valid. \"" +
                            devNetRootEntropies[currentAccountIndex - 1] + "\", " +
                            getTotalTestKeysCount(), exception);
                }
            default:
                if (currentAccountIndex >= devNetMnemonics.length) {
                    currentAccountIndex = 0;
                }
                try {
                    final int index = currentAccountIndex++;
                    return recordIssued(index, AccountKey.fromMnemonicPhrase(
                            devNetMnemonics[index],
                            DEFAULT_ACCOUNT_INDEX,
                            fogConfig.getFogUri(),
                            fogConfig.getFogReportId(),
                            fogConfig.getFogAuthoritySpki()
                    ));
                } catch (Exception exception) {
                    throw new IllegalStateException("Bug: All test keys must be valid");
                }
        }
    }
}
