// Copyright (c) 2020-2026 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.InsufficientFundsException;
import com.mobilecoin.lib.exceptions.InvalidTransactionException;

import consensus_common.ConsensusCommon;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Names the wallet behind a failure a drained account would produce.
 * <p>
 * The suite spends real balance and {@link TestKeysManager} rotates through
 * the configured accounts, so which one a given test drew is not recoverable
 * from the failure alone — and the answer shifts as tests are added or
 * reordered, which has caught this suite out twice. {@link
 * InsufficientFundsException} carries no state either, so the token is not
 * recoverable, which is why this reports balances rather than only
 * addresses. Those balances are also what separate a starved wallet from a
 * genuine double spend; see {@link #looksLikeStarvedWallet}.
 * <p>
 * It reads those balances inside the failure, so a passing run pays nothing
 * at all. Sweeping every wallet up front costs about 20s each on every run,
 * green ones included; this pays that only for the one to three accounts a
 * failing test drew, on a client built for the occasion.
 * <p>
 * The report is folded into the failure message rather than logged. Firebase
 * Test Lab's JUnit results carry a {@code failure} element and no
 * {@code system-out}, so anything merely printed reaches the logcat artifact
 * and nowhere else. The original exception is kept as the cause, so its type
 * and stack survive; {@code run_connected_tests.sh} prints the XML to the CI
 * console.
 */
public class FundingDiagnosticRule implements TestRule {

    private static final BigInteger PICO_MOB_PER_MOB = BigInteger.TEN.pow(12);

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                TestKeysManager.forgetIssuedKeys();
                try {
                    base.evaluate();
                } catch (final Throwable failure) {
                    if (!looksLikeStarvedWallet(failure)) {
                        throw failure;
                    }
                    final String said = failure.getMessage() != null
                            ? failure.getMessage()
                            : failure.toString();
                    throw new AssertionError(said + "\n\n" + walletsToFund(), failure);
                }
            }
        };
    }

    private static String walletsToFund() {
        final StringBuilder message = new StringBuilder("Wallet trouble on ")
                .append(Environment.CURRENT_TEST_ENV).append('.');

        final Map<Integer, AccountKey> issued = TestKeysManager.getIssuedKeys();
        if (issued.isEmpty()) {
            return message.append(" No account was issued to this test.").toString();
        }

        // Every account the test drew, balance and all. The one to fund is the
        // one sitting at nothing, so there is no need to guess at how each was
        // used — a recipient that never spends reads as funded either way. The
        // index is the account's position in the write-only mnemonics secret,
        // which an address in the log cannot otherwise be traced back to.
        message.append(" This test drew:");
        for (final Map.Entry<Integer, AccountKey> drawn : issued.entrySet()) {
            message.append("\n  account ").append(drawn.getKey()).append(": ")
                    .append(addressOf(drawn.getValue()))
                    .append("\n    holds ").append(balancesOf(drawn.getValue()));
        }
        return message.toString();
    }

    /**
     * What {@code accountKey} holds, per token. A token missing from the
     * listing holds nothing — the balance map only covers tokens the account
     * has outputs for.
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
                held.append(asAmount(balance.getKey(), balance.getValue().getValue()))
                        .append(' ').append(balance.getKey().getName());
            }
            return held.length() > 0 ? held.toString() : "nothing, in any token";
        } catch (final Exception exception) {
            // Diagnostics must not replace the failure that got us here.
            return "<balance unavailable: " + exception + ">";
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    /**
     * MOB in whole coin, every other token in its smallest unit. Decimals are
     * per-token and the SDK carries none of them — MOB is twelve, eUSD is six —
     * so anything but MOB is quoted raw rather than guessed at, which would
     * otherwise print a funded eUSD wallet as drained.
     */
    private static String asAmount(final TokenId tokenId, final BigInteger value) {
        if (!TokenId.MOB.equals(tokenId)) {
            return value.toString();
        }
        return new BigDecimal(value)
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

    /**
     * Whether {@code failure} is the kind a drained wallet produces.
     *
     * <p>{@link InsufficientFundsException} is the honest one. A wallet down
     * to a single output also reports {@code ContainsSpentKeyImage}: a test
     * that spends in a loop consumes that output, prepares the next
     * transaction against a view that has not yet seen the change, and
     * re-selects the output it just spent. Consensus rejects it for the spent
     * key image, so the funding problem arrives wearing the wrong name — and
     * the balances below tell the two apart. Healthy balances mean it really
     * is a double spend.
     */
    private static boolean looksLikeStarvedWallet(final Throwable failure) {
        for (Throwable t = failure; t != null; t = t.getCause()) {
            if (t instanceof InsufficientFundsException) {
                return true;
            }
            if (t instanceof InvalidTransactionException
                    && ((InvalidTransactionException) t).getResult()
                    == ConsensusCommon.ProposeTxResult.ContainsSpentKeyImage) {
                return true;
            }
        }
        return false;
    }

}
