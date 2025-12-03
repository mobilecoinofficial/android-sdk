// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class PrintableWrapperTest {
    private static final String MEMO = "test memo";
    private static final UnsignedLong PAYLOAD_AMOUNT = UnsignedLong.valueOf(100000);
    private static final String TAG = PrintableWrapper.class.getName();

    @Test
    public void test_public_address_payload() throws SerializationException {
        PublicAddress publicAddress = TestKeysManager.getNextAccountKey().getPublicAddress();
        PrintableWrapper printableWrapper = PrintableWrapper.fromPublicAddress(publicAddress);
        Assert.assertTrue(
                "Must have public address",
                printableWrapper.hasPublicAddress()
        );
        Assert.assertFalse(
                "Must not have payment request",
                printableWrapper.hasPaymentRequest()
        );
        Assert.assertFalse(
                "Must not have transfer payload",
                printableWrapper.hasTransferPayload()
        );
        String b58String = printableWrapper.toB58String();
        printableWrapper = PrintableWrapper.fromB58String(b58String);
        Assert.assertTrue(
                "Must have public address",
                printableWrapper.hasPublicAddress()
        );
        Assert.assertFalse(
                "Must not have payment request",
                printableWrapper.hasPaymentRequest()
        );
        Assert.assertFalse(
                "Must not have transfer payload",
                printableWrapper.hasTransferPayload()
        );
        Assert.assertEquals("Must be equal to the originally encoded public address",
                printableWrapper.getPublicAddress(),
                publicAddress
        );
    }

    @Test
    public void test_payment_request_payload() throws SerializationException {
        PublicAddress publicAddress = TestKeysManager.getNextAccountKey().getPublicAddress();
        TokenId tokenId = TokenId.MOB;
        UnsignedLong paymentId = UnsignedLong.valueOf(32);
        PaymentRequest paymentRequest = new PaymentRequest(publicAddress,
                PAYLOAD_AMOUNT,
                MEMO,
                tokenId,
                paymentId
        );
        PrintableWrapper printableWrapper = PrintableWrapper.fromPaymentRequest(paymentRequest);
        Assert.assertFalse(
                "Must not have public address",
                printableWrapper.hasPublicAddress()
        );
        Assert.assertTrue(
                "Must have payment request",
                printableWrapper.hasPaymentRequest()
        );
        Assert.assertFalse(
                "Must not have transfer payload",
                printableWrapper.hasTransferPayload()
        );
        String b58String = printableWrapper.toB58String();
        printableWrapper = PrintableWrapper.fromB58String(b58String);
        Assert.assertFalse(
                "Must not have public address",
                printableWrapper.hasPublicAddress()
        );
        Assert.assertTrue(
                "Must have payment request",
                printableWrapper.hasPaymentRequest()
        );
        Assert.assertFalse(
                "Must not have transfer payload",
                printableWrapper.hasTransferPayload()
        );
        Assert.assertEquals("Must be equal to the originally encoded payment request",
                printableWrapper.getPaymentRequest(),
                paymentRequest
        );
        Assert.assertEquals("Payment request must include token id",
                printableWrapper.getPaymentRequest().getTokenId(),
                tokenId);
        Assert.assertEquals("Payment request must include payment id",
                printableWrapper.getPaymentRequest().getPaymentId(),
                paymentId);
    }

    @Test
    public void test_payment_request_with_null_payment_id() throws SerializationException {
        PublicAddress publicAddress = TestKeysManager.getNextAccountKey().getPublicAddress();
        TokenId tokenId = TokenId.MOB;
        PaymentRequest paymentRequest = new PaymentRequest(publicAddress,
                PAYLOAD_AMOUNT,
                MEMO,
                tokenId,
                null
        );
        String b58String = PrintableWrapper.fromPaymentRequest(paymentRequest).toB58String();
        PrintableWrapper printableWrapper = PrintableWrapper.fromB58String(b58String);
        Assert.assertNull("Payment request payment id must be null",
                printableWrapper.getPaymentRequest().getPaymentId());
    }

    @Test
    public void test_transfer_payload() throws SerializationException {
        byte[] rootEntropy = new byte[32];
        Arrays.fill(rootEntropy, (byte)0);
        TestFogConfig fogConfig = Environment.getTestFogConfig();
        AccountKey accountKey = AccountKey.fromRootEntropy(rootEntropy,
                fogConfig.getFogUri(),
                fogConfig.getFogReportId(),
                fogConfig.getFogAuthoritySpki()
        );
        RistrettoPublic publicKey = accountKey.getViewKey().getPublicKey();
        TransferPayload transferPayload = TransferPayload.fromRootEntropy(rootEntropy,
                publicKey,
                MEMO
        );
        PrintableWrapper printableWrapper = PrintableWrapper.fromTransferPayload(transferPayload);
        Assert.assertFalse(
                "Must not have public address",
                printableWrapper.hasPublicAddress()
        );
        Assert.assertFalse(
                "Must not payment request",
                printableWrapper.hasPaymentRequest()
        );
        Assert.assertTrue(
                "Must have transfer payload",
                printableWrapper.hasTransferPayload()
        );
        String b58String = printableWrapper.toB58String();
        printableWrapper = PrintableWrapper.fromB58String(b58String);
        Assert.assertFalse(
                "Must not have public address",
                printableWrapper.hasPublicAddress()
        );
        Assert.assertFalse(
                "Must not have payment request",
                printableWrapper.hasPaymentRequest()
        );
        Assert.assertTrue(
                "Must have transfer payload",
                printableWrapper.hasTransferPayload()
        );
        Assert.assertEquals("Must be equal to the originally encoded transfer payload",
                printableWrapper.getTransferPayload(),
                transferPayload
        );
    }

    @Test
    public void test_mob_uri_round_trip() throws SerializationException, InvalidUriException {
        PublicAddress publicAddress = TestKeysManager.getNextAccountKey().getPublicAddress();
        PrintableWrapper printableWrapper = PrintableWrapper.fromPublicAddress(publicAddress);
        Uri shareableUri = printableWrapper.toUri();
        Logger.i(TAG, "Produced shareable uri: " + shareableUri.toString());
        printableWrapper = PrintableWrapper.fromUri(shareableUri);
        PublicAddress restoredPublicAddress = printableWrapper.getPublicAddress();
        Assert.assertEquals(publicAddress, restoredPublicAddress);
    }
}
