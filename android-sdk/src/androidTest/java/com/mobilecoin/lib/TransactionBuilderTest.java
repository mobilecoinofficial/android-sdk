package com.mobilecoin.lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.api.MobileCoinAPI;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;

@RunWith(AndroidJUnit4.class)
public class TransactionBuilderTest {

    @Test
    public void testReproducibleTransactions() throws Exception {
        ChaCha20Rng rng = ChaCha20Rng.withRandomSeed();
        MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final Amount amountToSend = Amount.ofMOB(BigInteger.valueOf(52398457942L));
        final Amount fee = client.estimateTotalFee(amountToSend);
        final PublicAddress recipient = TestKeysManager.getNextAccountKey().getPublicAddress();
        rng.setWordPos(BigInteger.ZERO);
        Transaction transaction1 = client.prepareTransaction(
                recipient,
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                rng
        ).getTransaction();
        rng.setWordPos(BigInteger.ZERO);
        Transaction transaction2 = client.prepareTransaction(
                recipient,
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                rng
        ).getTransaction();
        assertEquals(transaction1.getTombstoneBlockIndex(), transaction2.getTombstoneBlockIndex());
        assertEquals(transaction1.getFee(), transaction2.getFee());
        assertEquals(transaction1.getKeyImages(), transaction2.getKeyImages());
        assertEquals(transaction1.getOutputPublicKeys(), transaction2.getOutputPublicKeys());
        assertArrayEquals(transaction1.toByteArray(), transaction2.toByteArray());
    }

}
