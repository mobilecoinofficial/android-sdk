// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static com.mobilecoin.lib.Environment.CURRENT_TEST_ENV;
import static com.mobilecoin.lib.Environment.getTestFogConfig;
import static com.mobilecoin.lib.UtilTest.waitForReceiptStatus;
import static com.mobilecoin.lib.UtilTest.waitForTransactionStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.mobilecoin.lib.exceptions.AmountDecoderException;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidTransactionException;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.uri.FogUri;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4.class)
public class MobileCoinClientTest {
    private static final String TAG = MobileCoinClient.class.toString();
    private static final String wrongTrustRootBase64 = "MIIDdzCCAl" +
            "+gAwIBAgIEAgAAuTANBgkqhkiG9w0BAQUFADBaMQswCQYDVQQGEwJJRTESMBAGA1UEChMJQmFsdGltb3JlMRMwEQYDVQQLEwpDeWJlclRydXN0MSIwIAYDVQQDExlCYWx0aW1vcmUgQ3liZXJUcnVzdCBSb290MB4XDTAwMDUxMjE4NDYwMFoXDTI1MDUxMjIzNTkwMFowWjELMAkGA1UEBhMCSUUxEjAQBgNVBAoTCUJhbHRpbW9yZTETMBEGA1UECxMKQ3liZXJUcnVzdDEiMCAGA1UEAxMZQmFsdGltb3JlIEN5YmVyVHJ1c3QgUm9vdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKMEuyKrmD1X6CZymrV51Cni4eiVgLGw41uOKymaZN+hXe2wCQVt2yguzmKiYv60iNoS6zjrIZ3AQSsBUnuId9Mcj8e6uYi1agnnc+gRQKfRzMpijS3ljwumUNKoUMMo6vWrJYeKmpYcqWe4PwzV9/lSEy/CG9VwcPCPwBLKBsua4dnKM3p31vjsufFoREJIE9LAwqSuXmD+tqYF/LTdB1kC1FkYmGP1pWPgkAx9XbIGevOF6uvUA65ehD5f/xXtabz5OTZydc93Uk3zyZAsuT3lySNTPx8kmCFcB5kpvcY67Oduhjprl3RjM71oGDHweI12v/yejl0qhqdNkNwnGjkCAwEAAaNFMEMwHQYDVR0OBBYEFOWdWTCCR1jMrPoIVDaGezq1BE3wMBIGA1UdEwEB/wQIMAYBAf8CAQMwDgYDVR0PAQH/BAQDAgEGMA0GCSqGSIb3DQEBBQUAA4IBAQCFDF2O5G9RaEIFoN27TyclhAO992T9Ldcw46QQF+vaKSm2eT929hkTI7gQCvlYpNRhcL0EYWoSihfVCr3FvDB81ukMJY2GQE/szKN+OMY3EU/t3WgxjkzSswF07r51XgdIGn9w/xZchMB5hbgF/X++ZRGjD8ACtPhSNzkE1akxehi/oCr0Epn3o0WC4zxe9Z2etciefC7IpJ5OCBRLbf1wbWsaY71k5h+3zvDyny67G7fyUIhzksLi4xaNmjICq44Y3ekQEe5+NauQrz4wlHrQMz2nZQ/1/I6eYs9HRCwBXbsdtTLSR9I4LtD+gdwyah617jzV/OeBHRnDJELqYzmp";

    @Rule
    public GrantPermissionRule mRuntimePermissionRule =
            GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    @Test
    public void mobile_coin_client_integration_test() throws Exception {
        final Amount amount = Amount.ofMOB(BigInteger.TEN);

        final MobileCoinClient senderClient = MobileCoinClientBuilder.newBuilder().build();
        final MobileCoinClient recipientClient = MobileCoinClientBuilder.newBuilder().build();
        final PublicAddress recipient = recipientClient.getAccountKey().getPublicAddress();
        try {

            // Create and submit the transaction
            final Amount fee = senderClient.estimateTotalFee(amount);
            final PendingTransaction pending = senderClient.prepareTransaction(
                    recipient,
                    amount,
                    fee,
                    TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(senderClient.getAccountKey())
            );
            final Balance senderBalanceBefore = senderClient.getBalance(TokenId.MOB);
            final Balance senderBalanceBefore2 = senderClient.getBalance(TokenId.MOB);

            // Test balance consistency
            assertNotSame(senderBalanceBefore, senderBalanceBefore2);
            assertEquals(senderBalanceBefore, senderBalanceBefore2);

            final Balance recipientBalanceBefore = recipientClient.getBalance(TokenId.MOB);
            senderClient.submitTransaction(pending.getTransaction());

            // Test Tx status
            final Transaction.Status txStatus = waitForTransactionStatus(senderClient,
                    pending.getTransaction());
            assertEquals(Transaction.Status.ACCEPTED, txStatus);

            Balance senderBalanceAfter;
            do {
                senderBalanceAfter = senderClient.getBalance(TokenId.MOB);
            } while (senderBalanceAfter.getBlockIndex().compareTo(txStatus.getBlockIndex()) < 0);
            final Balance recipientBalanceAfter = recipientClient.getBalance(TokenId.MOB);

            // Test Receipt status
            final Receipt receipt = pending.getReceipt();
            waitForReceiptStatus(recipientClient, receipt);
            assertTrue(
                    "A valid receipt is expected",
                    receipt.isValid(recipientClient.getAccountKey())
            );
            assertEquals(
                    receipt.getAmountData(recipientClient.getAccountKey()),
                    amount
            );
            assertEquals(Receipt.Status.RECEIVED, recipientClient.getReceiptStatus(receipt));

            try {
                // Decoding Amount with wrong key must fail
                receipt.getAmountData(TestKeysManager.getNextAccountKey());
                Assert.fail("Must throw an exception when the amount cannot be decoded");
            } catch (AmountDecoderException ignore) {}

            // Test balances update correctly after transaction
            assertEquals(Amount.ofMOB(senderBalanceBefore.getValue())
                            .subtract(amount)
                            .subtract(fee),
                    Amount.ofMOB(senderBalanceAfter.getValue()));
            assertEquals(Amount.ofMOB(recipientBalanceBefore.getValue())
                            .add(amount),
                    Amount.ofMOB(recipientBalanceAfter.getValue())
            );
        } finally {
            senderClient.shutdown();
            recipientClient.shutdown();
        }
    }

    /*
    @Test
    public void test_eran() throws Exception {
        HashMap<String, String> xxx = new HashMap<>();
        for (int i = 0; i < 10; ++i) {
            MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder().build();
            PublicAddress addr = mobileCoinClient.getAccountKey().getPublicAddress();
            PrintableWrapper printableWrapper = PrintableWrapper.fromPublicAddress(addr);
            String b58 = printableWrapper.toB58String();
       //     if (!b58.equals("jCDqZsEboYy8qhpJ9QKVp6ioVfm3quc6LFcgk9A4n3inawAuu4YoXE8TJDG3KBkwahPLFuj5SS3z1YTNspY8xVeHSSd9o24tPrCh8tCa8LQZwoWSLWBqER43fK5ST44qE8yscjUAdT5wThe225KfHHyDxji22Az8bAjEDybKpfTnWT1gKa47EzDRzjuwVHRymYVq3vsa2fhX4AmvV2hZ6Rmg4y6qJpBup7zrCxM4GJgC8S5q"))
       //         continue;

            Balance b = mobileCoinClient.getBalance(TokenId.MOB);
            xxx.put(b58, b.getValue().toString());
            System.out.println(b);
        }

        System.out.println("done: " + xxx.size());
    }
     */

    @Test
    public void test_post_to_serialized_public_address() throws Exception {
        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder().build();
        AccountKey recipient = TestKeysManager.getNextAccountKey();
        try {
            final byte[] serializedAddress = recipient.getPublicAddress().toByteArray();
            final PublicAddress recipientAddress = PublicAddress.fromBytes(serializedAddress);

            final Amount amount = Amount.ofMOB(BigInteger.TEN);
            final Amount minimumFee = mobileCoinClient.estimateTotalFee(
                    amount
            );
            PendingTransaction pending = mobileCoinClient.prepareTransaction(
                    recipientAddress,
                    amount,
                    minimumFee,
                    TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(mobileCoinClient.getAccountKey())
            );
            mobileCoinClient.submitTransaction(pending.getTransaction());
        } finally {
            mobileCoinClient.shutdown();
        }
    }

    @Test
    public void test_attestation_must_fail() throws Exception {
        TestFogConfig fogConfig = getTestFogConfig();
        ClientConfig clientConfig = fogConfig.getClientConfig();
        // change fog verifier to make balance call fail
        clientConfig.fogView = clientConfig.consensus;
        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder()
                .setAccountKey(TestKeysManager.getNextAccountKey())
                .setTestFogConfig(fogConfig)
                .build();
        try {
            mobileCoinClient.getBalance(TokenId.MOB);
            Assert.fail("Invalid verifier must fail the test");
        } catch (AttestationException ex) {
            // success
        }

        mobileCoinClient.shutdown();

    }

    @Test
    public void test_bad_trust_root_must_fail() throws Exception {
        TestFogConfig fogConfig = getTestFogConfig();
        ClientConfig clientConfig = fogConfig.getClientConfig();
        // change fog verifier to make balance call fail
        byte[] certificateBytes = Base64.decode(wrongTrustRootBase64, Base64.DEFAULT);
        Set<X509Certificate> certs = Util.makeCertificatesFromData(certificateBytes);
        clientConfig.fogView.withTrustRoots(certs);
        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder()
                .setAccountKey(TestKeysManager.getNextAccountKey())
                .setTestFogConfig(fogConfig)
                .build();
        try {
            mobileCoinClient.getBalance(TokenId.MOB);
            Assert.fail("Invalid trust root must fail the test");
        } catch (NetworkException ex) {
            // success
        }

        mobileCoinClient.shutdown();

    }

    @Test
    public void test_zero_coin_value() throws Exception {
        final MobileCoinClient senderClient = MobileCoinClientBuilder.newBuilder().build();
        final MobileCoinClient recipientClient = MobileCoinClientBuilder.newBuilder().build();

        final Balance initialBalance = recipientClient.getBalance(TokenId.MOB);
        try {
            final Amount amount = Amount.ofMOB(BigInteger.ZERO);
            final Amount minimumFee = senderClient.estimateTotalFee(
                    amount
            );
            final PendingTransaction pending = senderClient.prepareTransaction(
                    recipientClient.getAccountKey().getPublicAddress(),
                    amount,
                    minimumFee,
                    TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(senderClient.getAccountKey())
            );
            senderClient.submitTransaction(pending.getTransaction());

            final Receipt.Status status = waitForReceiptStatus(recipientClient, pending.getReceipt());
            assertEquals(status, Receipt.Status.RECEIVED);
            final Balance finalBalance = recipientClient.getBalance(TokenId.MOB);
            assertEquals(
                    initialBalance,
                    finalBalance
            );

            // make sure a zero value unspent TxOut exists
            final UnsignedLong startBlock = initialBalance.getBlockIndex();
            final UnsignedLong endBlock = finalBalance.getBlockIndex();
            final List<OwnedTxOut> txOuts = recipientClient.fogBlockClient.scanForTxOutsInBlockRange(
                    new BlockRange(startBlock, endBlock.add(UnsignedLong.ONE)),
                    recipientClient.getAccountKey()
            );
            OwnedTxOut zeroCoinTxOut = null;
            for (OwnedTxOut txOut : txOuts) {
                if (txOut.getAmount().equals(Amount.ofMOB(BigInteger.ZERO))) {
                    zeroCoinTxOut = txOut;
                    break;
                }
            }
            if (zeroCoinTxOut == null) {
                Assert.fail("Failed to retrieve zero value TxOut");
            }
        } finally {
            senderClient.shutdown();
            recipientClient.shutdown();
        }
    }

    /**
     * Test defragmentation flow
     * 1. Create a temporary account key
     * 2a. Send small denomination TxOuts to that key
     * 2b. Add additional amount to cover defragmentation fees
     * 3. Verify that the account needs defragmentation
     * 4. Defragment the temporary account
     * 5. Send the funds back into the original account
     * (fees will consumed since the test is not a mock)
     */
    @Test
    public void test_fragmented_account() throws Exception {

        final AccountKey coinSourceKey = TestKeysManager.getNextAccountKey();
        final MobileCoinClient coinSourceClient = MobileCoinClientBuilder.newBuilder()
            .setAccountKey(coinSourceKey).build();

        final int FRAGMENTS_TO_TEST = 20;
        final Amount MINIMUM_TX_FEE = coinSourceClient.getOrFetchMinimumTxFee(TokenId.MOB);
        final Amount FRAGMENT_AMOUNT = MINIMUM_TX_FEE.multiply(Amount.ofMOB(BigInteger.TEN));

        final TestFogConfig fogConfig = getTestFogConfig();
        // 1. Create a new fragmented account
        final AccountKey fragmentedAccount = AccountKey.createNew(
                fogConfig.getFogUri(),
                fogConfig.getFogReportId(),
                fogConfig.getFogAuthoritySpki()
        );

        MobileCoinClient fragmentedClient =
            MobileCoinClientBuilder.newBuilder().setAccountKey(fragmentedAccount).build();

        // 2a. Send small denomination TxOuts to the test account
        for (int i = 0; i < FRAGMENTS_TO_TEST; ++i) {
            final Amount fee = coinSourceClient.estimateTotalFee(FRAGMENT_AMOUNT);
            final PendingTransaction pendingTransaction = coinSourceClient.prepareTransaction(
                    fragmentedAccount.getPublicAddress(),
                    FRAGMENT_AMOUNT,
                    fee,
                    TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(coinSourceKey)
            );
            coinSourceClient.submitTransaction(pendingTransaction.getTransaction());
            waitForTransactionStatus(coinSourceClient,
                    pendingTransaction.getTransaction());
            Receipt.Status status = waitForReceiptStatus(
                    fragmentedClient,
                    pendingTransaction.getReceipt()
            );
            if (status != Receipt.Status.RECEIVED) {
                Assert.fail("Unable to receive transaction on a new account");
            }
        }

        // 2c. Add necessary amount to cover the fees (number of optimizations + actual Tx)
        // and verify the transferable amount before and after fees
        final int iterations = FRAGMENTS_TO_TEST / UTXOSelector.MAX_INPUTS + 1;
        final Amount futureFees = MINIMUM_TX_FEE.multiply(Amount.ofMOB(BigInteger.valueOf(iterations)))
                .add(Amount.ofMOB(MobileCoinClient.INPUT_FEE.multiply(BigInteger.valueOf(FRAGMENTS_TO_TEST))))
                .add(Amount.ofMOB(MobileCoinClient.OUTPUT_FEE.multiply(BigInteger.valueOf(iterations))));

        // Verify the transferable amount is calculated correctly
        Amount transferableAmount = fragmentedClient.getTransferableAmount(TokenId.MOB);
        Amount calculatedTransferableAmount =
                Amount.ofMOB(BigInteger.valueOf(FRAGMENTS_TO_TEST)).multiply(FRAGMENT_AMOUNT).subtract(futureFees);
        assertEquals(calculatedTransferableAmount, transferableAmount);

        final Amount txFee = coinSourceClient.estimateTotalFee(futureFees);
        PendingTransaction pendingTransaction = coinSourceClient.prepareTransaction(
                fragmentedAccount.getPublicAddress(),
                futureFees,
                txFee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(coinSourceKey)
        );
        coinSourceClient.submitTransaction(pendingTransaction.getTransaction());
        coinSourceClient.shutdown();
        final Receipt.Status status = waitForReceiptStatus(fragmentedClient,
                pendingTransaction.getReceipt());

        if (status != Receipt.Status.RECEIVED) {
            Assert.fail("Failed to send fees to the test account");
        }

        // Verify the transferable amount is calculated correctly with future fees
        transferableAmount = fragmentedClient.getTransferableAmount(TokenId.MOB);
        calculatedTransferableAmount =
                Amount.ofMOB(BigInteger.valueOf(FRAGMENTS_TO_TEST)).multiply(FRAGMENT_AMOUNT);
        assertEquals(calculatedTransferableAmount, transferableAmount);

        // 3. Verify the account needs defragmentation
        final Amount txAmount = FRAGMENT_AMOUNT.multiply(Amount.ofMOB(BigInteger.valueOf(FRAGMENTS_TO_TEST)));
        if (!fragmentedClient.requiresDefragmentation(txAmount)) {
            Assert.fail("Test account is not fragmented enough for the test");
        }
        // 4. Defragment the temporary account
        fragmentedClient.defragmentAccount(txAmount, new DefragmentationDelegate() {
            @Override
            public void onStart() {
                Logger.d(TAG, "Defragmentation process started");
            }

            @Override
            public boolean onStepReady(@NonNull PendingTransaction defragStepTx,
                                       @NonNull BigInteger fee)
                    throws NetworkException, InvalidTransactionException, AttestationException {
                Logger.d(TAG, "Defragmentation step");
                fragmentedClient.submitTransaction(defragStepTx.getTransaction());
                return true;
            }

            @Override
            public void onComplete() {
                Logger.d(TAG, "Defragmentation process has completed successfully");
            }

            @Override
            public void onCancel() {
                Logger.wtf(TAG, "Defragmentation process was cancelled");
                Assert.fail("Defragmentation process should not be cancelled in this test");
            }
        }, true);
        // 5. Send the funds back to the original wallet
        final Amount fee = fragmentedClient.estimateTotalFee(txAmount);
        pendingTransaction = fragmentedClient.prepareTransaction(
                coinSourceKey.getPublicAddress(),
                txAmount,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(fragmentedAccount)
        );
        fragmentedClient.submitTransaction(pendingTransaction.getTransaction());
        fragmentedClient.shutdown();
    }

    @Test
    public void test_internal_external_get_owned_tx_outs_api() throws Exception {
        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder().build();
        TxOutStore store = mobileCoinClient.getTxOutStore();
        store.refresh(
                mobileCoinClient.viewClient,
                mobileCoinClient.ledgerClient,
                mobileCoinClient.fogBlockClient
        );
        AccountActivity accountActivity = mobileCoinClient.getAccountActivity();
        UnsignedLong activityBlockCount = accountActivity.getBlockCount();
        UnsignedLong storeBlockCount = store.getCurrentBlockIndex().add(UnsignedLong.ONE);

        Set<OwnedTxOut> private_api_tx_outs = store.getSyncedTxOuts();
        Set<OwnedTxOut> public_api_tx_outs = accountActivity.getAllTokenTxOuts();
        mobileCoinClient.shutdown();

        if (!private_api_tx_outs.equals(public_api_tx_outs)) {
            Assert.fail("Internal and external APIs are inconsistent");
        }

        // check for timestamps
        public_api_tx_outs.forEach(ownedTxOut -> {
            Date receivedTimestamp = ownedTxOut.getReceivedBlockTimestamp();
            Assert.assertNotNull("Received timestamp should not be null", receivedTimestamp);
            Logger.d(TAG, "TxOut received at " + receivedTimestamp);
            if (ownedTxOut.isSpent(UnsignedLong.MAX_VALUE)) {
                Date spentTimestamp = ownedTxOut.getSpentBlockTimestamp();
                Assert.assertNotNull("Spent timestamp should not be null", spentTimestamp);
                Logger.d(TAG, "TxOut spent at " + spentTimestamp);
            }
        });

        assertEquals("Internal and external APIs are inconsistent",
                activityBlockCount, storeBlockCount);

    }

    @Test
    public void test_tx_and_receipt_accessors() throws Exception {
        final MobileCoinClient senderClient = MobileCoinClientBuilder.newBuilder().build();
        final MobileCoinClient recipientClient = MobileCoinClientBuilder.newBuilder().build();
        final Amount amount = Amount.ofMOB(BigInteger.TEN);
        try {
            final Amount minimumFee = senderClient.estimateTotalFee(
                    amount
            );
            final PendingTransaction pending = senderClient.prepareTransaction(
                    recipientClient.getAccountKey().getPublicAddress(),
                    amount,
                    minimumFee,
                    TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(senderClient.getAccountKey())
            );

            // verify input key images corresponding to owned txOuts
            final AccountActivity activity = senderClient.getAccountActivity();
            final Set<KeyImage> keyImages = pending.getTransaction().getKeyImages();
            final Set<KeyImage> matches = activity.getAllTokenTxOuts().stream()
                    .map(OwnedTxOut::getKeyImage)
                    .filter(keyImages::contains)
                    .collect(Collectors.toSet());
            assertEquals("account has to contain all key images used in the " +
                    "transaction", keyImages.size(), matches.size());

            senderClient.submitTransaction(pending.getTransaction());
            waitForTransactionStatus(senderClient, pending.getTransaction());
            waitForReceiptStatus(recipientClient, pending.getReceipt());

            // verify the output of the receipt is valid
            final OwnedTxOut receivedTxOut = pending.getReceipt().fetchOwnedTxOut(recipientClient);
            assertEquals("Receipt amount must be valid", receivedTxOut.getAmount(), amount);
            final AccountActivity recipientActivity = recipientClient.getAccountActivity();
            assertTrue("Recipient activity must contain received TxOut",
                    recipientActivity.getAllTokenTxOuts().contains(receivedTxOut));
        } finally {
            senderClient.shutdown();
            recipientClient.shutdown();
        }
    }

    // send a transaction to a non-fog public address
    // verify the validity of the sent TxOut by view key scanning
    @Test
    public void test_send_to_address_without_fog() throws Exception {
        final TestFogConfig fogConfig = getTestFogConfig();
        final AccountKey recipientAccount = TestKeysManager.getNextAccountKey();
        // remove fog info from the public address
        final PublicAddress addressWithFog = recipientAccount.getPublicAddress();
        final PublicAddress recipient = new PublicAddress(
                addressWithFog.getViewKey(),
                addressWithFog.getSpendKey()
        );
        // send a transaction
        final Amount amount = Amount.ofMOB(BigInteger.valueOf(1234));
        final MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder().build();
        final Amount fee = mobileCoinClient.estimateTotalFee(amount);
        final PendingTransaction pendingTransaction = mobileCoinClient.prepareTransaction(recipient,
                amount, fee, TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(mobileCoinClient.getAccountKey()));

        mobileCoinClient.submitTransaction(pendingTransaction.getTransaction());
        final Transaction.Status txStatus = waitForTransactionStatus(mobileCoinClient,
                pendingTransaction.getTransaction());
        final Receipt txReceipt = pendingTransaction.getReceipt();

        final UnsignedLong txBlockIndex = txStatus.getBlockIndex();
        final FogUri fogUri = new FogUri(fogConfig.getFogUri());
        final FogBlockClient blockClient = new FogBlockClient(
                RandomLoadBalancer.create(fogUri),
                ClientConfig.defaultConfig().fogLedger,
                fogConfig.getTransportProtocol());
        blockClient.setAuthorization(fogConfig.getUsername(), fogConfig.getPassword());
        final List<OwnedTxOut> txOuts = blockClient.scanForTxOutsInBlockRange(
                new BlockRange(
                        txBlockIndex,
                        txBlockIndex.add(UnsignedLong.ONE)),
                recipientAccount);

        // find our txOut
        boolean foundSentTxOut = false;
        for (OwnedTxOut ownedTxOut : txOuts) {
            if (ownedTxOut.getPublicKey().equals(txReceipt.getPublicKey())) {
                foundSentTxOut = true;
                break;
            }
        }
        assertTrue(foundSentTxOut);

        mobileCoinClient.shutdown();

    }

    @Test
    public void test_txOutStore_serialization() throws Exception {
        StorageAdapter storageAdapter = new TestStorageAdapter();
        TestFogConfig testFogConfig = TestFogConfig.getFogConfig(CURRENT_TEST_ENV, storageAdapter);
        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder()
            .setTestFogConfig(testFogConfig).build();

        TxOutStore txOutStore = mobileCoinClient.getTxOutStore();
        txOutStore.refresh(
            mobileCoinClient.viewClient,
            mobileCoinClient.ledgerClient,
            mobileCoinClient.fogBlockClient
        );

        mobileCoinClient.cacheUserData();

        String txOutStoreStorageKey = TxOutStore.createStorageKey(mobileCoinClient.getAccountKey());
        byte[] serializedTxOutStore = storageAdapter.get(txOutStoreStorageKey);
        TxOutStore deserializedTxOutStore = TxOutStore.fromBytes(serializedTxOutStore);
        deserializedTxOutStore.setAccountKey(mobileCoinClient.getAccountKey());

        assertEquals(txOutStore, deserializedTxOutStore);
        mobileCoinClient.shutdown();

    }

    private static final class TestStorageAdapter implements StorageAdapter {

        private final Map<String, byte[]> storage;

        private String key;
        private byte[] serializedObject;

        TestStorageAdapter() {
           storage = new HashMap<>();
        }

        @Override
        public boolean has(String key) {
           return storage.containsValue(key);
        }

        @Override
        public byte[] get(String key) {
          if (storage.containsKey(key)) {
              return storage.get(key);
          }
          throw new IllegalArgumentException("The provided key doesn't match any stored key.");
        }

        @Override
        public void set(String key, byte[] value) {
          storage.put(key, value);
        }

        @Override
        public void clear(String key) {
          storage.clear();
        }
    }

}
