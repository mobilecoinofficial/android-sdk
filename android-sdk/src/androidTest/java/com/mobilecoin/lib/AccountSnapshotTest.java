// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static com.mobilecoin.lib.UtilTest.waitForTransactionStatus;

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

public class AccountSnapshotTest {
    // check snapshot balance, spent some coins and check again, the balance for that snapshot
    // should remain constant
    @Test
    public void test_balance() throws Exception {

        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder().build();
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
    public void test_snapshot_prep() throws Exception {

        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder().build();
        AccountSnapshot snapshot = mobileCoinClient.getAccountSnapshot();
        Balance balanceBefore = snapshot.getBalance();
        BigInteger amount = BigInteger.valueOf(100);
        BigInteger fee = snapshot.estimateTotalFee(amount, mobileCoinClient.getOrFetchMinimumTxFee());
        PendingTransaction pendingTransaction = snapshot.prepareTransaction(
                TestKeysManager.getNextAccountKey().getPublicAddress(),
                amount,
                fee
        );
        mobileCoinClient.submitTransaction(pendingTransaction.getTransaction());
        waitForTransactionStatus(mobileCoinClient, pendingTransaction.getTransaction());
        // make sure the snapshot didn't change
        Balance balanceAfter = snapshot.getBalance();
        Assert.assertEquals(balanceBefore, balanceAfter);
    }

    @Test
    public void test_tx_status() throws Exception {

        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder().build();
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
    public void test_tx_receipts() throws Exception {

        MobileCoinClient senderClient = MobileCoinClientBuilder.newBuilder().build();
        MobileCoinClient recipientClient = MobileCoinClientBuilder.newBuilder().build();
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
    public void test_null_return() throws Exception {
        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder().build();
        AccountSnapshot snapshot =
                mobileCoinClient.getAccountSnapshot(UnsignedLong.MAX_VALUE.sub(UnsignedLong.ONE));
        Assert.assertNull(snapshot);
    }
}
