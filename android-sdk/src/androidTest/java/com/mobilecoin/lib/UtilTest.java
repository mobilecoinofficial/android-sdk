// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FogSyncException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidReceiptException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class UtilTest {
    private static final String TAG = Util.class.toString();
    private static final int STATUS_CHECK_DELAY_MS = 1000;
    // 5 minutes if the check delay is 1000
    private static final int STATUS_MAX_RETRIES = 300;

    @Test
    public void biging2string() {
        Assert.assertEquals(
                Util.bigint2string(BigInteger.valueOf(0)),
                "0"
        );
        Assert.assertEquals(
                Util.bigint2string(BigInteger.valueOf(1)),
                "1"
        );
        Assert.assertEquals(
                Util.bigint2string(BigInteger.valueOf(1020304050)),
                "1020304050"
        );
        Assert.assertEquals(
                Util.bigint2string(new BigInteger(
                        1,
                        new byte[]{
                                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                        }
                )),
                "18446744073709551615"
        );
        Assert.assertEquals(
                Util.bigint2string(new BigInteger(
                        1,
                        new byte[]{
                                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                        }
                ).subtract(BigInteger.ONE)),
                "18446744073709551614"
        );
    }

    static Receipt.Status waitForReceiptStatus(
            @NonNull MobileCoinClient mobileCoinClient,
            @NonNull Receipt receipt) throws TimeoutException, InterruptedException,
            NetworkException, InvalidFogResponse, AttestationException, InvalidReceiptException, FogSyncException {
        int receiptQueryTries = 0;
        Receipt.Status status;
        do {
            if (receiptQueryTries++ == STATUS_MAX_RETRIES) {
                throw new TimeoutException();
            }
            Thread.sleep(STATUS_CHECK_DELAY_MS);
            status = mobileCoinClient.getReceiptStatus(receipt);
            Assert.assertTrue(status.getBlockIndex().compareTo(UnsignedLong.ZERO) > 0);
            // transaction status will change to FAILED if the current block index becomes
            // higher than transaction maximum heights
        } while (status == Receipt.Status.UNKNOWN);
        return status;
    }

    static Transaction.Status waitForTransactionStatus(
            @NonNull MobileCoinClient mobileCoinClient,
            @NonNull Transaction tx) throws TimeoutException, InterruptedException,
            NetworkException, InvalidFogResponse, AttestationException, FogSyncException {
        int txQueryTries = 0;
        Transaction.Status status;
        do {
            if (txQueryTries++ == STATUS_MAX_RETRIES) {
                throw new TimeoutException();
            }
            Logger.i(TAG, "Waiting 1 second for the transaction status");
            Thread.sleep(STATUS_CHECK_DELAY_MS);
            status = mobileCoinClient.getTransactionStatus(tx);
            Assert.assertTrue(status.getBlockIndex().compareTo(UnsignedLong.ZERO) > 0);
            // receipt status will change to FAILED if the current block index becomes
            // higher than transaction maximum heights
        } while (status == Transaction.Status.UNKNOWN);
        return status;
    }


}
