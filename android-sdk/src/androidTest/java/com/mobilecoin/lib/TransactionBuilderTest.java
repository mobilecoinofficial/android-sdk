package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.util.Hex;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;

@RunWith(AndroidJUnit4.class)
public class TransactionBuilderTest {

    @Test
    public void testReproducibleTransactions() throws Exception {
        final ChaCha20Rng rng = ChaCha20Rng.fromSeed(new byte[] {103, 111, 116, 111, 32, 104, 116, 116, 112, 115, 58, 47, 47, 98, 117, 121, 46, 109, 111, 98, 105, 108, 101, 99, 111, 105, 110, 46, 99, 111, 109, 0});
        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
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

        assertEquals(transaction1.getOutputPublicKeys(), transaction2.getOutputPublicKeys());

        final AccountSnapshot snapshot = client.getAccountSnapshot();
        rng.setWordPos(BigInteger.ZERO);
        transaction1 = snapshot.prepareTransaction(
                recipient,
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                rng
        ).getTransaction();
        rng.setWordPos(BigInteger.ZERO);
        transaction2 = snapshot.prepareTransaction(
                recipient,
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                rng
        ).getTransaction();

        assertEquals(transaction1.getOutputPublicKeys(), transaction2.getOutputPublicKeys());

        // Rebuild transaction2 WITHOUT resetting rng word pos
        transaction2 = snapshot.prepareTransaction(
                recipient,
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                rng
        ).getTransaction();

        assertNotEquals(transaction1.getOutputPublicKeys(), transaction2.getOutputPublicKeys());

    }

    @Test
    public void testPredictableOutputPublicKey() throws Exception {
        // Given RNG seed and recipient public address
        final ChaCha20Rng rng = ChaCha20Rng.fromSeed(Hex.toByteArray("676f746f2068747470733a2f2f6275792e6d6f62696c65636f696e2e636f6d00"));
        final PublicAddress recipient = PublicAddress.fromBytes(Hex.toByteArray("0a220a2048d9e6aa836d7aa57f7a9da9709603d8f5071faff4908c86ed829e68c0129f6312220a207ee52b7741a8b9f3701ab716fa361483b03ddcaeecb64ba64355a3411c87d43d"));
        // TxOut public key that should appear
        final RistrettoPublic expectedTxOutPublicKey = RistrettoPublic.fromBytes(Hex.toByteArray("d47d4f6525cee846a47106e92de3e63e5b3cb677b8ae4df7efd667e2bad15719"));

        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final Amount amountToSend = Amount.ofMOB(BigInteger.valueOf(52398457942L));
        final Amount fee = client.estimateTotalFee(amountToSend);
        Transaction client1Transaction = client.prepareTransaction(
                recipient,
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                rng
        ).getTransaction();

        rng.setWordPos(BigInteger.ZERO);

        MobileCoinClient client2 = MobileCoinClientBuilder.newBuilder().build();
        Transaction client2Transaction = client2.prepareTransaction(
                recipient,
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client2.getAccountKey()),
                rng
        ).getTransaction();

        assertTrue(client1Transaction.getOutputPublicKeys().contains(expectedTxOutPublicKey));
        assertTrue(client2Transaction.getOutputPublicKeys().contains(expectedTxOutPublicKey));

    }

}
