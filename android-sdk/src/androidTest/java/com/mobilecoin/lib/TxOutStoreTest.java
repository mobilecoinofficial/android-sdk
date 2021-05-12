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
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;
import com.mobilecoin.lib.uri.FogUri;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TxOutStoreTest {
    private final TestFogConfig fogConfig = Environment.getTestFogConfig();

    @Test
    public void test_serialize_roundtrip()
            throws SerializationException, InvalidFogResponse, NetworkException,
            AttestationException, InvalidUriException {
        AccountKey accountKey = TestKeysManager.getNextAccountKey();
        MobileCoinClientImpl mobileCoinClient = Environment.makeFreshMobileCoinClient(accountKey);
        TxOutStore store = new TxOutStore(accountKey);
        store.refresh(
                mobileCoinClient.viewClient,
                mobileCoinClient.ledgerClient,
                mobileCoinClient.fogBlockClient
        );
        mobileCoinClient.shutdown();
        Set<OwnedTxOut> utxos = store.getSyncedTxOuts();
        int storeSize = utxos.size();
        if (storeSize <= 0) {
            Assert.fail("UTXO store must contain non-zero TXOs");
        }

        BigInteger balance = BigInteger.ZERO;
        for (OwnedTxOut utxo : utxos) {
            balance = balance.add(utxo.getValue());
        }

        byte[] serialized = store.toByteArray();
        store = TxOutStore.fromBytes(
                serialized,
                accountKey
        );
        Set<OwnedTxOut> restoredUtxos = store.getSyncedTxOuts();
        int restoredStoreSize = restoredUtxos.size();
        Assert.assertEquals("Serialized and Deserialized stores sizes must be the same",
                storeSize,
                restoredStoreSize
        );

        BigInteger restoredBalance = BigInteger.ZERO;
        for (OwnedTxOut utxo : restoredUtxos) {
            restoredBalance = restoredBalance.add(utxo.getValue());
        }
        Assert.assertEquals("Balance must remain the same after serialization round trip",
                balance,
                restoredBalance
        );
    }

    @Test
    public void fetch_fog_misses_test()
            throws NetworkException, AttestationException, FragmentedAccountException,
            InsufficientFundsException, InvalidFogResponse, TransactionBuilderException,
            FeeRejectedException, InvalidTransactionException, InterruptedException,
            FogReportException, InvalidReceiptException, InvalidUriException {

        MobileCoinClient senderClient = Environment.makeFreshMobileCoinClient();
        MobileCoinClient recipientClient = Environment.makeFreshMobileCoinClient();

        // send a random amount
        BigInteger amount = BigInteger.valueOf(Math.abs(new SecureRandom().nextInt() % 100) + 1);
        BigInteger minimumFee = senderClient.estimateTotalFee(
                amount
        );
        PendingTransaction pending = senderClient.prepareTransaction(
                recipientClient.getAccountKey().getPublicAddress(),
                amount,
                minimumFee
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

        FogBlockClient blockClient = new FogBlockClient(
                new FogUri(fogConfig.getFogUri()),
                fogConfig.getClientConfig().fogLedger
        );

        blockClient.setAuthorization(
                fogConfig.getUsername(),
                fogConfig.getPassword()
        );

        TxOutStore store = new TxOutStore(recipientClient.getAccountKey());

        // add two small ranges to check
        Set<BlockRange> fakeFogMisses = new HashSet<>(Arrays.asList(new BlockRange(txoBlock,
                        txoBlock.add(UnsignedLong.ONE)),
                new BlockRange(25, 50)));
        Set<OwnedTxOut> records = store.fetchFogMisses(
                fakeFogMisses,
                blockClient

        );
        blockClient.shutdown();

        // search for the specific amount sent earlier
        boolean found = false;
        for (OwnedTxOut txOut : records) {
            found = txOut.getValue().equals(amount);
            if (found) break;
        }
        if (!found) {
            Assert.fail("Unable to retrieve account TxOuts from the ledger");
        }
    }

}
