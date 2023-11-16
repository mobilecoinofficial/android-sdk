package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.util.Hex;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class TxOutMemoIntegrationTest {

    @Test
    public void txOutMemoIntegrationTest() throws Exception {
        final MobileCoinClient senderClient = MobileCoinClientBuilder.newBuilder().build();
        final AccountKey senderAccountKey = senderClient.getAccountKey();
        final AccountKey recipientAccountKey = TestKeysManager.getNextAccountKey();
        final Amount amountToSend = Amount.ofMOB(BigInteger.TEN);
        final Amount fee = senderClient.estimateTotalFee(amountToSend);

        final AddressHash senderAddressHash = senderAccountKey.getPublicAddress().calculateAddressHash();
        final AddressHash recipientAddressHash = recipientAccountKey.getPublicAddress().calculateAddressHash();
        final UnsignedLong totalOutlay = UnsignedLong.valueOf(fee.add(amountToSend).getValue());
        final UnsignedLong paymentRequestId = UnsignedLong.fromLongBits(322);
        final UnsignedLong paymentIntentId = UnsignedLong.fromLongBits(~322);

        /* Test sender and destination memo */

        // Build a transaction with sender and destination memos
        MobileCoinAPI.Tx tx = senderClient.prepareTransaction(
              recipientAccountKey.getPublicAddress(),
              amountToSend,
              fee,
              TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(senderAccountKey)
        ).getTransaction().toProtoBufObject();
        List<MobileCoinAPI.TxOut> outputsList = tx.getPrefix().getOutputsList();
        TxOut txOut1 = TxOut.fromProtoBufObject(outputsList.get(0));
        TxOut txOut2 = TxOut.fromProtoBufObject(outputsList.get(1));

        // Distinguish payload and change TxOut
        TxOut payloadTxOut, changeTxOut;
        try {
            txOut1.getMaskedAmount().unmaskAmount(recipientAccountKey.getViewKey(), txOut1.getPublicKey());
            payloadTxOut = txOut1;
            changeTxOut = txOut2;
        } catch(Exception e) {
            payloadTxOut = txOut2;
            changeTxOut = txOut1;
        }

        // Verify correct sender memo on payload TxOut
        byte[] sentMemoPayload = payloadTxOut.decryptMemoPayload(recipientAccountKey);
        final SenderMemo senderMemo = (SenderMemo) TxOutMemoParser
              .parseTxOutMemo(sentMemoPayload, recipientAccountKey, payloadTxOut);
        final SenderMemoData senderMemoData = senderMemo
              .getSenderMemoData(senderAccountKey.getPublicAddress(), recipientAccountKey.getDefaultSubAddressViewKey());
        assertEquals(senderAddressHash, senderMemo.getUnvalidatedAddressHash());
        assertEquals(senderAddressHash, senderMemoData.getAddressHash());

        // Verify correct destination memo on change TxOut
        byte[] changeMemoPayload = changeTxOut.decryptMemoPayload(senderAccountKey);
        final DestinationMemo destinationMemo = (DestinationMemo) TxOutMemoParser
                .parseTxOutMemo(changeMemoPayload, senderAccountKey, changeTxOut);
        final DestinationMemoData destinationMemoData = destinationMemo.getDestinationMemoData();
        assertEquals(UnsignedLong.valueOf(fee.getValue()), destinationMemoData.getFee());
        assertEquals(totalOutlay, destinationMemoData.getTotalOutlay());
        assertEquals(recipientAddressHash, destinationMemoData.getAddressHash());

        /* Test payment request memo */

        // Build a transaction with sender and destination memos
        tx = senderClient.prepareTransaction(
                recipientAccountKey.getPublicAddress(),
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderPaymentRequestAndDestinationRTHMemoBuilder(senderAccountKey, paymentRequestId)
        ).getTransaction().toProtoBufObject();
        outputsList = tx.getPrefix().getOutputsList();
        txOut1 = TxOut.fromProtoBufObject(outputsList.get(0));
        txOut2 = TxOut.fromProtoBufObject(outputsList.get(1));

        // Distinguish payload and change TxOut
        try {
            txOut1.getMaskedAmount().unmaskAmount(recipientAccountKey.getViewKey(), txOut1.getPublicKey());
            payloadTxOut = txOut1;
            changeTxOut = txOut2;
        } catch(Exception e) {
            payloadTxOut = txOut2;
            changeTxOut = txOut1;
        }

        // Verify correct sender memo on payload TxOut
        sentMemoPayload = payloadTxOut.decryptMemoPayload(recipientAccountKey);
        final SenderWithPaymentRequestMemo senderWithPaymentRequestMemo = (SenderWithPaymentRequestMemo) TxOutMemoParser
                .parseTxOutMemo(sentMemoPayload, recipientAccountKey, payloadTxOut);
        final SenderWithPaymentRequestMemoData senderWithPaymentRequestMemoData = senderWithPaymentRequestMemo
                .getSenderWithPaymentRequestMemoData(senderAccountKey.getPublicAddress(), recipientAccountKey.getDefaultSubAddressViewKey());
        assertEquals(senderAddressHash, senderWithPaymentRequestMemo.getUnvalidatedAddressHash());
        assertEquals(senderAddressHash, senderWithPaymentRequestMemoData.getAddressHash());
        assertEquals(paymentRequestId, senderWithPaymentRequestMemoData.getPaymentRequestId());

        // Verify correct destination memo on change TxOut
        changeMemoPayload = changeTxOut.decryptMemoPayload(senderAccountKey);
        final DestinationWithPaymentRequestMemo destinationWithPaymentRequestMemo = (DestinationWithPaymentRequestMemo) TxOutMemoParser
                .parseTxOutMemo(changeMemoPayload, senderAccountKey, changeTxOut);
        final DestinationWithPaymentRequestMemoData destinationWithPaymentRequestMemoData = destinationWithPaymentRequestMemo.getDestinationWithPaymentRequestMemoData();
        assertEquals(UnsignedLong.valueOf(fee.getValue()), destinationWithPaymentRequestMemoData.getFee());
        assertEquals(totalOutlay, destinationWithPaymentRequestMemoData.getTotalOutlay());
        assertEquals(recipientAddressHash, destinationWithPaymentRequestMemoData.getAddressHash());
        assertEquals(paymentRequestId, destinationWithPaymentRequestMemoData.getPaymentRequestId());

        /* Test payment intent memo */

        // Build a transaction with sender and destination memos
        tx = senderClient.prepareTransaction(
                recipientAccountKey.getPublicAddress(),
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderPaymentIntentAndDestinationRTHMemoBuilder(senderAccountKey, paymentIntentId)
        ).getTransaction().toProtoBufObject();
        outputsList = tx.getPrefix().getOutputsList();
        txOut1 = TxOut.fromProtoBufObject(outputsList.get(0));
        txOut2 = TxOut.fromProtoBufObject(outputsList.get(1));

        // Distinguish payload and change TxOut
        try {
            txOut1.getMaskedAmount().unmaskAmount(recipientAccountKey.getViewKey(), txOut1.getPublicKey());
            payloadTxOut = txOut1;
            changeTxOut = txOut2;
        } catch(Exception e) {
            payloadTxOut = txOut2;
            changeTxOut = txOut1;
        }

        // Verify correct sender memo on payload TxOut
        sentMemoPayload = payloadTxOut.decryptMemoPayload(recipientAccountKey);
        final SenderWithPaymentIntentMemo senderWithPaymentIntentMemo = (SenderWithPaymentIntentMemo) TxOutMemoParser
                .parseTxOutMemo(sentMemoPayload, recipientAccountKey, payloadTxOut);
        final SenderWithPaymentIntentMemoData senderWithPaymentIntentMemoData = senderWithPaymentIntentMemo
                .getSenderWithPaymentIntentMemoData(senderAccountKey.getPublicAddress(), recipientAccountKey.getDefaultSubAddressViewKey());
        assertEquals(senderAddressHash, senderWithPaymentIntentMemoData.getAddressHash());
        assertEquals(paymentIntentId, senderWithPaymentIntentMemoData.getPaymentIntentId());

        // Verify correct destination memo on change TxOut
        changeMemoPayload = changeTxOut.decryptMemoPayload(senderAccountKey);
        final DestinationWithPaymentIntentMemo destinationWithPaymentIntentMemo = (DestinationWithPaymentIntentMemo) TxOutMemoParser
                .parseTxOutMemo(changeMemoPayload, senderAccountKey, changeTxOut);
        final DestinationWithPaymentIntentMemoData destinationWithPaymentIntentMemoData = destinationWithPaymentIntentMemo.getDestinationWithPaymentIntentMemoData();
        assertEquals(UnsignedLong.valueOf(fee.getValue()), destinationWithPaymentRequestMemoData.getFee());
        assertEquals(totalOutlay, destinationWithPaymentIntentMemoData.getTotalOutlay());
        assertEquals(recipientAddressHash, destinationWithPaymentIntentMemoData.getAddressHash());
        assertEquals(paymentIntentId, destinationWithPaymentIntentMemoData.getPaymentIntentId());
    }
}