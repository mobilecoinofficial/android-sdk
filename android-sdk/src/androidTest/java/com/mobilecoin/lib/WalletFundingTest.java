package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

/**
 * Reports what every configured test wallet holds, and fails if any is empty.
 * <p>
 * This is a diagnostic, not a behavioural test. The suite spends real balance
 * and {@link TestKeysManager} rotates through the accounts, so a drained
 * wallet surfaces as {@code InsufficientFundsException} in whichever test
 * happened to draw it — naming neither the wallet nor the token. Rather than
 * track that per test, this asks every wallet directly.
 * <p>
 * It runs with the rest of the suite rather than only after a failure: a
 * second Test Lab matrix is a second test execution against a daily quota,
 * and that quota runs out precisely on the days something is failing.
 * <p>
 * The report rides in the failure message because Firebase Test Lab's JUnit
 * results carry a {@code failure} element and no {@code system-out} —
 * anything merely printed reaches the logcat artifact and nowhere else.
 * {@code run_connected_tests.sh} pulls that XML back to the CI console.
 */
@RunWith(AndroidJUnit4.class)
public class WalletFundingTest {

    @Test
    public void allTestWalletsAreFunded() {
        final StringBuilder report = new StringBuilder("Test wallets on ")
                .append(Environment.CURRENT_TEST_ENV).append(':');
        int empty = 0;

        // A full rotation lands the shared index back where it started, so
        // running this does not shift which account any other test draws.
        for (int i = 0; i < TestKeysManager.getTotalTestKeysCount(); i++) {
            final AccountKey accountKey = TestKeysManager.getNextAccountKey();
            final String balances = balancesOf(accountKey);
            if (balances == null) {
                empty++;
            }
            report.append("\n  account[").append(i).append("] ")
                    .append(addressOf(accountKey))
                    .append("\n    holds ")
                    .append(balances == null ? "NOTHING — fund this one" : balances);
        }

        Assert.assertEquals(
                report.append("\n\n").append(empty).append(" wallet(s) need funding.")
                        .toString(),
                0,
                empty);
    }

    /**
     * What {@code accountKey} holds, per token, or null when it holds nothing.
     * A token missing from the list holds nothing — the balance map only
     * covers tokens the account has outputs for.
     */
    private static String balancesOf(final AccountKey accountKey) {
        final TestFogConfig fogConfig = Environment.getTestFogConfig();
        MobileCoinClient client = null;
        try {
            client = new MobileCoinClient(
                    accountKey,
                    fogConfig.getFogUri(),
                    fogConfig.getConsensusUris(),
                    fogConfig.getClientConfig(),
                    fogConfig.getTransportProtocol());
            client.setFogBasicAuthorization(fogConfig.getUsername(), fogConfig.getPassword());
            client.setConsensusBasicAuthorization(
                    fogConfig.getUsername(), fogConfig.getPassword());

            final StringBuilder held = new StringBuilder();
            for (final Map.Entry<TokenId, Balance> balance : client.getBalances().entrySet()) {
                if (balance.getValue().getValue().signum() == 0) {
                    continue;
                }
                if (held.length() > 0) {
                    held.append(", ");
                }
                held.append(balance.getKey().getName()).append(' ')
                        .append(balance.getValue().getValue());
            }
            return held.length() > 0 ? held.toString() : null;
        } catch (final Exception exception) {
            // One unreachable wallet must not hide the rest of the report.
            return "<unknown: " + exception + ">";
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    private static String addressOf(final AccountKey accountKey) {
        try {
            return PrintableWrapper.fromPublicAddress(accountKey.getPublicAddress()).toB58String();
        } catch (final Exception exception) {
            return "<unencodable: " + exception + ">";
        }
    }

}
