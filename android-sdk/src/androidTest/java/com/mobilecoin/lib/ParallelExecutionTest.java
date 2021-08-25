// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.log.Logger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4.class)
public class ParallelExecutionTest {
    private static final String TAG = MobileCoinClient.class.toString();
    private static final int TASKS_TO_TEST = 10;
    private static final int AMOUNT_TO_SEND = 10;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_balance_parallelism() throws InterruptedException, InvalidUriException {

        ExecutorService executorService = Executors.newFixedThreadPool(TASKS_TO_TEST / 2);

        MobileCoinClient aliceClient = Environment.makeFreshMobileCoinClient();
        MobileCoinClient bobClient = Environment.makeFreshMobileCoinClient();

        for (int i = 0; i < TASKS_TO_TEST; ++i) {
            executorService.submit(() -> {
                try {
                    Balance aliceBalance = aliceClient.getBalance();
                    Balance bobBalance = aliceClient.getBalance();
                    // introduce random delays up to a 100 ms
                    Thread.sleep(new SecureRandom().nextInt() % 100);
                    Logger.d(
                            TAG,
                            "Alice Balance: " + aliceBalance.toString()
                    );
                    Logger.d(
                            TAG,
                            "Bob Balance: " + bobBalance.toString()
                    );
                } catch (Exception e) {
                    Assert.fail(e.getLocalizedMessage());
                }
            });
            executorService.submit(() -> {
                try {
                    // Alice sends coins to Bob
                    BigInteger fee = aliceClient.estimateTotalFee(
                            BigInteger.valueOf(AMOUNT_TO_SEND)
                    );
                    PendingTransaction pending = aliceClient
                            .prepareTransaction(bobClient.getAccountKey().getPublicAddress(),
                                                BigInteger.valueOf(AMOUNT_TO_SEND),
                                                fee,
                                                TxOutMemoBuilder.createDefaultRTHMemoBuilder()
                            );
                    aliceClient.submitTransaction(pending.getTransaction());

                    // Bob sends coins to Alice
                    fee = bobClient.estimateTotalFee(
                            BigInteger.valueOf(AMOUNT_TO_SEND)
                    );
                    pending = aliceClient
                            .prepareTransaction(aliceClient.getAccountKey().getPublicAddress(),
                                                BigInteger.valueOf(AMOUNT_TO_SEND),
                                                fee,
                                                TxOutMemoBuilder.createDefaultRTHMemoBuilder()
                            );
                    bobClient.submitTransaction(pending.getTransaction());
                } catch (Exception e) {
                    Assert.fail(e.getLocalizedMessage());
                }
            });

        }
        executorService.shutdown();
        // Wait a while for existing tasks to terminate
        // allow up to 30 seconds per task
        executorService.awaitTermination(
                TASKS_TO_TEST * 30,
                TimeUnit.SECONDS
        );
        bobClient.shutdown();
        aliceClient.shutdown();
    }
}
