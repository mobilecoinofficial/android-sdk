// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FeeRejectedException;
import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.FragmentedAccountException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidReceiptException;
import com.mobilecoin.lib.exceptions.InvalidTransactionException;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.TimeoutException;

import static com.mobilecoin.lib.UtilTest.waitForTransactionStatus;

public class AccountSnapshotTest {
    private static final int STATUS_CHECK_DELAY_MS = 1000;
    // 5 minutes if the check delay is 1000
    private static final int STATUS_MAX_RETRIES = 300;

    // check snapshot balance, spent some coins and check again, the balance for that snapshot
    // should remain constant
    @Test
    public void test_balance() throws NetworkException, InvalidFogResponse, AttestationException,
            InsufficientFundsException, FogReportException, TransactionBuilderException,
            FragmentedAccountException, FeeRejectedException, InvalidTransactionException,
            TimeoutException, InterruptedException, InvalidUriException {

        MobileCoinClient mobileCoinClient = Environment.makeFreshMobileCoinClient();
        AccountSnapshot snapshot = mobileCoinClient.getAccountSnapshot();
        Balance balanceBefore = snapshot.getBalance();
        BigInteger amount = BigInteger.valueOf(100);
        BigInteger fee = mobileCoinClient.estimateTotalFee(amount);
        PendingTransaction pendingTransaction = mobileCoinClient.prepareTransaction(
                TestKeysManager.getNextAccountKey().getPublicAddress(),
                amount,
                fee
        );
        mobileCoinClient.submitTransaction(pendingTransaction.getTransaction());
        waitForTransactionStatus(mobileCoinClient, pendingTransaction.getTransaction());
        Balance balanceAfter = snapshot.getBalance();
        Assert.assertEquals(balanceBefore, balanceAfter);

        // snapshot balance consistency
        AccountSnapshot oldSnapshot =
                mobileCoinClient.getAccountSnapshot(balanceBefore.getBlockIndex());
        Assert.assertNotNull(oldSnapshot);
        Assert.assertEquals(balanceBefore, oldSnapshot.getBalance());
    }

    @Test
    public void test_tx_status() throws NetworkException, InvalidFogResponse, AttestationException,
            InsufficientFundsException, FogReportException, TransactionBuilderException,
            FragmentedAccountException, FeeRejectedException, InvalidTransactionException,
            TimeoutException, InterruptedException, InvalidUriException {

        MobileCoinClient mobileCoinClient = Environment.makeFreshMobileCoinClient();
        AccountSnapshot snapshotBefore =
                mobileCoinClient.getAccountSnapshot();
        BigInteger amount = BigInteger.valueOf(100);
        BigInteger fee = mobileCoinClient.estimateTotalFee(amount);
        PendingTransaction pendingTransaction = mobileCoinClient.prepareTransaction(
                TestKeysManager.getNextAccountKey().getPublicAddress(),
                amount,
                fee
        );
        mobileCoinClient.submitTransaction(pendingTransaction.getTransaction());
        Transaction.Status status = waitForTransactionStatus(mobileCoinClient,
                pendingTransaction.getTransaction());

        AccountSnapshot snapshotAfter;
        do {
            snapshotAfter = mobileCoinClient.getAccountSnapshot();
        } while (snapshotAfter.getBlockIndex().compareTo(status.getBlockIndex()) < 0);

        Transaction.Status statusBefore =
                snapshotBefore.getTransactionStatus(pendingTransaction.getTransaction());
        Assert.assertEquals(Transaction.Status.UNKNOWN, statusBefore);

        Transaction.Status statusAfter =
                snapshotAfter.getTransactionStatus(pendingTransaction.getTransaction());
        Assert.assertEquals(Transaction.Status.ACCEPTED, statusAfter);
    }

    @Test
    public void test_tx_receipts() throws NetworkException, InvalidFogResponse,
            AttestationException,
            InsufficientFundsException, FogReportException, TransactionBuilderException,
            FragmentedAccountException, FeeRejectedException, InvalidTransactionException,
            TimeoutException, InterruptedException, InvalidUriException, InvalidReceiptException {

        MobileCoinClient senderClient = Environment.makeFreshMobileCoinClient();
        MobileCoinClient recipientClient = Environment.makeFreshMobileCoinClient();
        AccountSnapshot snapshotBefore =
                recipientClient.getAccountSnapshot();
        BigInteger amount = BigInteger.valueOf(100);
        BigInteger fee = senderClient.estimateTotalFee(amount);
        PendingTransaction pendingTransaction = senderClient.prepareTransaction(
                recipientClient.getAccountKey().getPublicAddress(),
                amount,
                fee
        );
        senderClient.submitTransaction(pendingTransaction.getTransaction());
        Transaction.Status txStatus = waitForTransactionStatus(senderClient,
                pendingTransaction.getTransaction());

        AccountSnapshot snapshotAfter;
        do {
            snapshotAfter = recipientClient.getAccountSnapshot();
        } while (snapshotAfter.getBlockIndex().compareTo(txStatus.getBlockIndex()) < 0);

        Receipt.Status statusBefore =
                snapshotBefore.getReceiptStatus(pendingTransaction.getReceipt());
        Assert.assertEquals(Receipt.Status.UNKNOWN, statusBefore);

        Receipt.Status statusAfter =
                snapshotAfter.getReceiptStatus(pendingTransaction.getReceipt());
        Assert.assertEquals(Receipt.Status.RECEIVED, statusAfter);
    }

    @Test
    public void test_null_return() throws InvalidUriException, NetworkException,
            InvalidFogResponse, AttestationException {
        MobileCoinClient mobileCoinClient = Environment.makeFreshMobileCoinClient();
        AccountSnapshot snapshot =
                mobileCoinClient.getAccountSnapshot(UnsignedLong.MAX_VALUE.sub(UnsignedLong.ONE));
        Assert.assertNull(snapshot);
    }
}
