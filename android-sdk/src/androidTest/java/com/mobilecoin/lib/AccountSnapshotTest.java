// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static com.mobilecoin.lib.UtilTest.waitForTransactionStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.math.BigInteger;

public class AccountSnapshotTest {

    @Test
    public void account_snapshot_integration_test() throws Exception {

        final MobileCoinClient senderClient = MobileCoinClientBuilder.newBuilder().build();
        final MobileCoinClient recipientClient = MobileCoinClientBuilder.newBuilder().build();

        // Test null return for too high block index
        assertNull(senderClient.getAccountSnapshot(UnsignedLong.MAX_VALUE.sub(UnsignedLong.ONE)));

        // Create initial snapshots
        final AccountSnapshot snapshotBefore = senderClient.getAccountSnapshot();
        final Balance balanceBefore = snapshotBefore.getBalance(TokenId.MOB);
        final AccountSnapshot recipientSnapshotBefore = recipientClient.getAccountSnapshot();

        // Send a transaction which should change new balance on account
        final Amount amount = Amount.ofMOB(BigInteger.valueOf(100L));
        final Amount fee = snapshotBefore.estimateTotalFee(amount, senderClient.getOrFetchMinimumTxFee(TokenId.MOB));
        final PendingTransaction pendingTransaction = snapshotBefore.prepareTransaction(
                recipientClient.getAccountKey().getPublicAddress(),
                amount,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(senderClient.getAccountKey())
        );

        // Submit the transaction and wait until it completes
        senderClient.submitTransaction(pendingTransaction.getTransaction());
        waitForTransactionStatus(senderClient, pendingTransaction.getTransaction());

        // Check to make sure balance of snapshotBefore didn't change
        assertEquals(balanceBefore, snapshotBefore.getBalance(TokenId.MOB));

        // Create a new snapshot after transaction is accepted but at the block index before it is
        final AccountSnapshot snapshotAfter = senderClient.getAccountSnapshot(balanceBefore.getBlockIndex());
        assertNotSame(snapshotBefore, snapshotAfter);
        final Balance balanceAfter = snapshotAfter.getBalance(TokenId.MOB);

        // Make sure new and old snapshots at the same index have same balance
        assertEquals(balanceBefore, balanceAfter);

        // Test Tx and Receipt Status with before and after snapshots
        assertEquals(Transaction.Status.UNKNOWN, snapshotBefore.getTransactionStatus(pendingTransaction.getTransaction()));
        assertEquals(Transaction.Status.UNKNOWN, snapshotAfter.getTransactionStatus(pendingTransaction.getTransaction()));
        assertEquals(Receipt.Status.UNKNOWN, recipientSnapshotBefore.getReceiptStatus(pendingTransaction.getReceipt()));

        // Test Tx Status with new snapshot
        final AccountSnapshot newSnapshot = senderClient.getAccountSnapshot();
        final AccountSnapshot newRecipientSnapshot = recipientClient.getAccountSnapshot();
        assertEquals(Transaction.Status.ACCEPTED, newSnapshot.getTransactionStatus(pendingTransaction.getTransaction()));
        assertEquals(Receipt.Status.RECEIVED, newRecipientSnapshot.getReceiptStatus(pendingTransaction.getReceipt()));

        senderClient.shutdown();
        recipientClient.shutdown();

    }

}
