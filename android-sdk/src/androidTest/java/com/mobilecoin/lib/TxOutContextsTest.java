package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;

/**
 * Covers deriving TxOut public keys ahead of the transaction that will carry
 * them, via {@link MobileCoinClient#getTxOutContexts}.
 * <p>
 * Deriving is only safe because inputs consume none of the builder's RNG, so
 * the outputs a seed produces do not depend on what is being spent. These pin
 * that: if a draw is ever added ahead of the output keys, or the derive and
 * build paths stop sharing {@code addOutputs}, the first test here fails.
 */
@RunWith(AndroidJUnit4.class)
public class TxOutContextsTest {

    private static final byte[] SEED = new byte[] {103, 111, 116, 111, 32, 104, 116, 116, 112,
            115, 58, 47, 47, 98, 117, 121, 46, 109, 111, 98, 105, 108, 101, 99, 111, 105, 110,
            46, 99, 111, 109, 0};

    @Test
    public void testDerivedKeysMatchTheBuiltTransaction() throws Exception {
        final ChaCha20Rng rng = ChaCha20Rng.fromSeed(SEED);
        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final Amount amountToSend = Amount.ofMOB(BigInteger.valueOf(52398457942L));
        final Amount fee = client.estimateTotalFee(amountToSend);
        final PublicAddress recipient = TestKeysManager.getNextAccountKey().getPublicAddress();

        rng.setWordPos(BigInteger.ZERO);
        final TxOutContexts derived = client.getTxOutContexts(
                recipient,
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                rng
        );

        rng.setWordPos(BigInteger.ZERO);
        final PendingTransaction built = client.prepareTransaction(
                recipient,
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                rng
        );

        assertEquals(
                built.getPayloadTxOutContext().getTxOutPublicKey(),
                derived.getPayload().getTxOutPublicKey());
        // The derivation reports no change, having no inputs to leave a
        // remainder. Matching anyway is what shows amounts never reach the RNG.
        assertEquals(
                built.getChangeTxOutContext().getTxOutPublicKey(),
                derived.getChange().getTxOutPublicKey());
    }

    @Test
    public void testSameSeedDerivesTheSameKeys() throws Exception {
        final ChaCha20Rng rng = ChaCha20Rng.fromSeed(SEED);
        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final Amount amountToSend = Amount.ofMOB(BigInteger.valueOf(52398457942L));
        final Amount fee = client.estimateTotalFee(amountToSend);
        final PublicAddress recipient = TestKeysManager.getNextAccountKey().getPublicAddress();

        rng.setWordPos(BigInteger.ZERO);
        final TxOutContexts first = client.getTxOutContexts(recipient, amountToSend, fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                rng);
        rng.setWordPos(BigInteger.ZERO);
        final TxOutContexts second = client.getTxOutContexts(recipient, amountToSend, fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                rng);

        assertEquals(
                first.getPayload().getTxOutPublicKey(),
                second.getPayload().getTxOutPublicKey());
        assertEquals(
                first.getChange().getTxOutPublicKey(),
                second.getChange().getTxOutPublicKey());
    }

    @Test
    public void testDifferentSeedsDeriveDifferentKeys() throws Exception {
        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final Amount amountToSend = Amount.ofMOB(BigInteger.valueOf(52398457942L));
        final Amount fee = client.estimateTotalFee(amountToSend);
        final PublicAddress recipient = TestKeysManager.getNextAccountKey().getPublicAddress();

        final TxOutContexts first = client.getTxOutContexts(recipient, amountToSend, fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                ChaCha20Rng.fromSeed(SEED));
        final TxOutContexts second = client.getTxOutContexts(recipient, amountToSend, fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                ChaCha20Rng.withRandomSeed());

        assertNotEquals(
                first.getPayload().getTxOutPublicKey(),
                second.getPayload().getTxOutPublicKey());
    }

    /**
     * The public key is {@code r * D}, so the recipient is as much an input to
     * it as the seed. A caller holding only a seed cannot know the key.
     */
    @Test
    public void testRecipientChangesThePayloadKey() throws Exception {
        final ChaCha20Rng rng = ChaCha20Rng.fromSeed(SEED);
        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final Amount amountToSend = Amount.ofMOB(BigInteger.valueOf(52398457942L));
        final Amount fee = client.estimateTotalFee(amountToSend);

        rng.setWordPos(BigInteger.ZERO);
        final TxOutContexts toRecipient = client.getTxOutContexts(
                TestKeysManager.getNextAccountKey().getPublicAddress(), amountToSend, fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                rng);
        rng.setWordPos(BigInteger.ZERO);
        final TxOutContexts toSelf = client.getTxOutContexts(
                client.getAccountKey().getPublicAddress(), amountToSend, fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                rng);

        assertNotEquals(
                toRecipient.getPayload().getTxOutPublicKey(),
                toSelf.getPayload().getTxOutPublicKey());
    }

}
