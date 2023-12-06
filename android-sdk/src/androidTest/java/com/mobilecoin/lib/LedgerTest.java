// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.uri.FogUri;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import fog_view.View;

@RunWith(AndroidJUnit4.class)
public class LedgerTest {
    private static final String TAG = MobileCoinClient.class.toString();
    private final TestFogConfig fogConfig = Environment.getTestFogConfig();

    @Test
    public void fetch_block_records_test() throws NetworkException, AttestationException,
            InvalidUriException {
        FogUri fogUri = new FogUri(fogConfig.getFogUri());
        FogBlockClient blockClient = new FogBlockClient(
                RandomLoadBalancer.create(fogUri),
                fogConfig.getClientConfig().fogLedger,
                fogConfig.getTransportProtocol()
        );

        blockClient.setAuthorization(
                fogConfig.getUsername(),
                fogConfig.getPassword()
        );
        // avoid block #0, it is very large and will take much longer to process
        List<View.TxOutRecord> records =
                blockClient.fetchTxRecordsInBlockRange(new BlockRange(1, 5));
        blockClient.shutdown();
        if (records.isEmpty()) {
            Assert.fail("Unable to retrieve any TxOuts from the ledger");
        }
    }

    /**
     * Send a Tx and wait for completion then check current block index
     * and scan blocks around that index (in case the current block index advanced during the test)
     */
    @Test
    public void view_key_scanning_test() throws Exception {

        MobileCoinClient senderClient = MobileCoinClientBuilder.newBuilder().build();
        MobileCoinClient recipientClient = MobileCoinClientBuilder.newBuilder().build();

        // randomize the amount for each test run, up to a 100 picoMob
        Amount amount = new Amount(
                BigInteger.valueOf(Math.abs(new SecureRandom().nextInt() % 100) + 1),
                TokenId.MOB
        );
        Amount minimumFee = senderClient.estimateTotalFee(
                amount
        );
        PendingTransaction pending = senderClient.prepareTransaction(
                recipientClient.getAccountKey().getPublicAddress(),
                amount,
                minimumFee,
                TxOutMemoBuilder.createDefaultRTHMemoBuilder()
        );
        senderClient.submitTransaction(pending.getTransaction());

        Transaction.Status status;
        do {
            Thread.sleep(1000);
            status = senderClient.getTransactionStatus(pending.getTransaction());
            Assert.assertTrue(status.getBlockIndex().compareTo(UnsignedLong.ZERO) > 0);
            // transaction status will change to FAILED if the current block index becomes
            // higher than transaction maximum heights
        } while (status == Transaction.Status.UNKNOWN);


        Receipt.Status receiptStatus;
        UnsignedLong txoBlock;
        do {
            receiptStatus = recipientClient.getReceiptStatus(pending.getReceipt());
            txoBlock = receiptStatus.getBlockIndex();
            Thread.sleep(1000);
        } while (receiptStatus == Receipt.Status.UNKNOWN);
        FogUri fogUri = new FogUri(fogConfig.getFogUri());
        FogBlockClient blockClient = new FogBlockClient(
                RandomLoadBalancer.create(fogUri),
                fogConfig.getClientConfig().fogLedger,
                fogConfig.getTransportProtocol()
        );

        blockClient.setAuthorization(
                fogConfig.getUsername(),
                fogConfig.getPassword()
        );

        // scan a small range of blocks in case
        List<OwnedTxOut> records = blockClient.scanForTxOutsInBlockRange(
                new BlockRange(txoBlock.longValue() - 2, txoBlock.longValue() + 1),
                recipientClient.getAccountKey()
        );

        // search for the specific amount sent earlier
        boolean found = false;
        for (OwnedTxOut txOut : records) {
            found = txOut.getAmount().equals(amount);
            if (found) break;
        }

        // clean up
        blockClient.shutdown();
        senderClient.shutdown();
        recipientClient.shutdown();

        if (records.isEmpty() || !found) {
            Assert.fail("Unable to find posted TxOut");
        }
    }
}
