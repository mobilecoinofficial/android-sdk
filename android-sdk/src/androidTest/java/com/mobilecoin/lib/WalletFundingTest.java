package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.math.BigInteger;

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

    /**
     * MOB each wallet needs, in picoMOB. The suite spends MOB and nothing
     * else: the largest single transfer is 52398457942 picoMOB, and
     * {@code test_fragmented_account} pays twenty fees on top. This is
     * roughly double that, so a wallet at the line still finishes a run.
     */
    private static final BigInteger MINIMUM_MOB = BigInteger.valueOf(100_000_000_000L);

    @Test
    public void allTestWalletsAreFunded() {
        final StringBuilder report = new StringBuilder("Test wallets on ")
                .append(Environment.CURRENT_TEST_ENV)
                .append(" (each needs at least ").append(MINIMUM_MOB)
                .append(" picoMOB):");
        int underfunded = 0;

        // A full rotation lands the shared index back where it started, so
        // running this does not shift which account any other test draws.
        for (int i = 0; i < TestKeysManager.getTotalTestKeysCount(); i++) {
            final AccountKey accountKey = TestKeysManager.getNextAccountKey();
            final Wallet wallet = read(accountKey);
            if (wallet.mob.compareTo(MINIMUM_MOB) < 0) {
                underfunded++;
            }
            report.append("\n  account[").append(i).append("] ")
                    .append(addressOf(accountKey))
                    .append("\n    holds ").append(wallet.balances)
                    .append(wallet.mob.compareTo(MINIMUM_MOB) < 0 ? "  <-- SEND MOB HERE" : "");
        }

        Assert.assertEquals(
                report.append("\n\n").append(underfunded)
                        .append(" wallet(s) need MOB.").toString(),
                0,
                underfunded);
    }

    /** A wallet's MOB balance, and every balance it holds for the report. */
    private static final class Wallet {
        private final BigInteger mob;
        private final String balances;

        private Wallet(final BigInteger mob, final String balances) {
            this.mob = mob;
            this.balances = balances;
        }
    }

    /**
     * What {@code accountKey} holds. MOB is called out separately because it
     * is the only token the suite spends, so a wallet rich in anything else
     * is still unusable. A token missing from the listing holds nothing — the
     * balance map only covers tokens the account has outputs for.
     */
    private static Wallet read(final AccountKey accountKey) {
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

            BigInteger mob = BigInteger.ZERO;
            final StringBuilder held = new StringBuilder();
            for (final Map.Entry<TokenId, Balance> balance : client.getBalances().entrySet()) {
                final BigInteger value = balance.getValue().getValue();
                if (TokenId.MOB.equals(balance.getKey())) {
                    mob = value;
                }
                if (value.signum() == 0) {
                    continue;
                }
                if (held.length() > 0) {
                    held.append(", ");
                }
                held.append(balance.getKey().getName()).append(' ').append(value);
            }
            // Output count separates "send MOB" from "the MOB is there but in
            // too many pieces to assemble a transaction from".
            held.append(held.length() > 0 ? "" : "nothing, in any token")
                    .append(" across ").append(client.getUnspentTxOuts(TokenId.MOB).size())
                    .append(" MOB output(s)");
            return new Wallet(mob, held.toString());
        } catch (final Exception exception) {
            // One unreachable wallet must not hide the rest of the report.
            return new Wallet(BigInteger.ZERO, "<unknown: " + exception + ">");
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
