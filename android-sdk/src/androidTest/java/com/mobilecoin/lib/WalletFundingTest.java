package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Names the test wallets that need MOB, and fails if there are any.
 * <p>
 * The suite spends real MOB and {@link TestKeysManager} rotates through the
 * accounts, so a drained wallet surfaces as {@code InsufficientFundsException}
 * in whichever test happened to draw it — naming neither the wallet nor the
 * token. Rather than track that per test, this asks every wallet directly.
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

    private static final BigInteger PICO_MOB_PER_MOB = BigInteger.TEN.pow(12);

    /**
     * MOB each wallet needs, in picoMOB. The suite spends MOB and nothing
     * else: the largest single transfer is 52398457942 picoMOB, and
     * {@code test_fragmented_account} pays twenty fees on top. This is
     * roughly double that, so a wallet at the line still finishes a run.
     */
    private static final BigInteger MINIMUM = BigInteger.valueOf(100_000_000_000L);

    @Test
    public void allTestWalletsAreFunded() {
        final StringBuilder needsMob = new StringBuilder();

        // A full rotation lands the shared index back where it started, so
        // running this does not shift which account any other test draws.
        for (int i = 0; i < TestKeysManager.getTotalTestKeysCount(); i++) {
            final AccountKey accountKey = TestKeysManager.getNextAccountKey();
            final BigInteger mob = mobBalanceOf(accountKey);
            if (mob != null && mob.compareTo(MINIMUM) >= 0) {
                continue;
            }
            needsMob.append("\n  ").append(addressOf(accountKey))
                    .append("\n    has ")
                    .append(mob == null ? "an unreadable balance" : asMob(mob) + " MOB");
        }

        if (needsMob.length() > 0) {
            Assert.fail("Test wallets on " + Environment.CURRENT_TEST_ENV
                    + " below " + asMob(MINIMUM) + " MOB. Send MOB to:" + needsMob);
        }
    }

    /** The wallet's MOB balance in picoMOB, or null if it could not be read. */
    private static BigInteger mobBalanceOf(final AccountKey accountKey) {
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

            for (final Map.Entry<TokenId, Balance> balance : client.getBalances().entrySet()) {
                if (TokenId.MOB.equals(balance.getKey())) {
                    return balance.getValue().getValue();
                }
            }
            // No MOB entry at all means no MOB outputs.
            return BigInteger.ZERO;
        } catch (final Exception exception) {
            // One unreachable wallet must not hide the rest of the report.
            return null;
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    private static String asMob(final BigInteger picoMob) {
        if (picoMob.signum() == 0) {
            return "0";
        }
        return new BigDecimal(picoMob)
                .divide(new BigDecimal(PICO_MOB_PER_MOB))
                .stripTrailingZeros()
                .toPlainString();
    }

    private static String addressOf(final AccountKey accountKey) {
        try {
            return PrintableWrapper.fromPublicAddress(accountKey.getPublicAddress()).toB58String();
        } catch (final Exception exception) {
            return "<unencodable: " + exception + ">";
        }
    }

}
