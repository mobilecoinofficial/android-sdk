// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.log.Logger;

import org.junit.Assert;
import org.junit.Test;
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
    private static final Amount AMOUNT_TO_SEND = Amount.ofMOB(BigInteger.TEN);

    @Test
    public void test_balance_parallelism() throws InterruptedException, InvalidUriException {

        final ExecutorService executorService = Executors.newFixedThreadPool(TASKS_TO_TEST / 2);

        final MobileCoinClient aliceClient = MobileCoinClientBuilder.newBuilder().build();
        final MobileCoinClient bobClient = MobileCoinClientBuilder.newBuilder().build();

        for (int i = 0; i < TASKS_TO_TEST; ++i) {
            executorService.submit(() -> {
                try {
                    final Balance aliceBalance = aliceClient.getBalance(TokenId.MOB);
                    final Balance bobBalance = aliceClient.getBalance(TokenId.MOB);
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
                    Amount fee = aliceClient.estimateTotalFee(AMOUNT_TO_SEND);
                    PendingTransaction pending = aliceClient
                            .prepareTransaction(bobClient.getAccountKey().getPublicAddress(),
                                                AMOUNT_TO_SEND,
                                                fee,
                                                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(aliceClient.getAccountKey())
                            );
                    aliceClient.submitTransaction(pending.getTransaction());

                    // Bob sends coins to Alice
                    fee = bobClient.estimateTotalFee(AMOUNT_TO_SEND);
                    pending = aliceClient
                            .prepareTransaction(aliceClient.getAccountKey().getPublicAddress(),
                                                AMOUNT_TO_SEND,
                                                fee,
                                                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(aliceClient.getAccountKey())
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
