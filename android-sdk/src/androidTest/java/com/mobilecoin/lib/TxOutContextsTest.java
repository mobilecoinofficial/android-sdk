package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.util.Hex;

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

    /**
     * Arbitrary. These tests assert relationships between keys rather than any
     * particular key, so the seed's value carries no meaning — it is fixed
     * only so that a failure can be re-run.
     */
    private static final byte[] SEED = new byte[] {103, 111, 116, 111, 32, 104, 116, 116, 112,
            115, 58, 47, 47, 98, 117, 121, 46, 109, 111, 98, 105, 108, 101, 99, 111, 105, 110,
            46, 99, 111, 109, 0};

    /** A second arbitrary seed, differing from {@link #SEED} in one byte. */
    private static final byte[] OTHER_SEED = new byte[] {103, 111, 116, 111, 32, 104, 116, 116,
            112, 115, 58, 47, 47, 98, 117, 121, 46, 109, 111, 98, 105, 108, 101, 99, 111, 105,
            110, 46, 99, 111, 109, 1};

    @Test
    public void testDerivedKeysMatchTheBuiltTransaction() throws Exception {
        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final Amount amountToSend = Amount.ofMOB(BigInteger.valueOf(52398457942L));
        final Amount fee = client.estimateTotalFee(amountToSend);
        final PublicAddress recipient = TestKeysManager.getNextAccountKey().getPublicAddress();

        // One seed, two fresh streams — the contract a caller actually has.
        // No rewind between them: the derivation takes the seed rather than an
        // Rng precisely so nothing has to be wound back.
        final TxOutContexts derived = client.getTxOutContexts(recipient, SEED);

        final PendingTransaction built = client.prepareTransaction(
                recipient,
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                ChaCha20Rng.fromSeed(SEED)
        );

        assertEquals(
                built.getPayloadTxOutContext().getTxOutPublicKey(),
                derived.getPayload().getTxOutPublicKey());
        // The derivation names no amount, fee or memo — it has no parameters
        // for them — while the built transaction carries all three. Matching
        // anyway is what shows none of them reaches a draw.
        assertEquals(
                built.getChangeTxOutContext().getTxOutPublicKey(),
                derived.getChange().getTxOutPublicKey());
    }

    @Test
    public void testSameSeedDerivesTheSameKeys() throws Exception {
        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final Amount amountToSend = Amount.ofMOB(BigInteger.valueOf(52398457942L));
        final Amount fee = client.estimateTotalFee(amountToSend);
        final PublicAddress recipient = TestKeysManager.getNextAccountKey().getPublicAddress();

        final TxOutContexts first = client.getTxOutContexts(recipient, SEED);
        final TxOutContexts second = client.getTxOutContexts(recipient, SEED);

        assertEquals(
                first.getPayload().getTxOutPublicKey(),
                second.getPayload().getTxOutPublicKey());
        assertEquals(
                first.getChange().getTxOutPublicKey(),
                second.getChange().getTxOutPublicKey());
    }

    /**
     * The token id is a constant in the public entry point, so a caller
     * deriving for a non-MOB transaction runs the builder with a token id the
     * real send will not use. This pins that it makes no difference.
     *
     * <p>Both sides run at block version two, the first version accepting a
     * token id other than MOB — below that the non-MOB build is rejected
     * outright rather than producing keys to compare.
     */
    @Test
    public void testTokenIdDoesNotMoveTheKeys() throws Exception {
        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final PublicAddress recipient = TestKeysManager.getNextAccountKey().getPublicAddress();

        final TxOutContexts inMob =
                client.getTxOutContexts(recipient, SEED, 2, TokenId.MOB);
        final TxOutContexts inMobUsd =
                client.getTxOutContexts(recipient, SEED, 2, TokenId.from(UnsignedLong.ONE));

        assertEquals(
                inMob.getPayload().getTxOutPublicKey(),
                inMobUsd.getPayload().getTxOutPublicKey());
        assertEquals(
                inMob.getChange().getTxOutPublicKey(),
                inMobUsd.getChange().getTxOutPublicKey());
    }

    /**
     * The derivation reads the network's block version when it runs, and the
     * send reads it again later. If the network advanced in between the two
     * would build at different versions, so this pins that the version does
     * not move the keys and a caller need not derive and send close together.
     */
    @Test
    public void testBlockVersionDoesNotMoveTheKeys() throws Exception {
        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final PublicAddress recipient = TestKeysManager.getNextAccountKey().getPublicAddress();

        final TxOutContexts atOne =
                client.getTxOutContexts(recipient, SEED, 1, TokenId.MOB);
        final TxOutContexts atTwo =
                client.getTxOutContexts(recipient, SEED, 2, TokenId.MOB);

        assertEquals(
                atOne.getPayload().getTxOutPublicKey(),
                atTwo.getPayload().getTxOutPublicKey());
        assertEquals(
                atOne.getChange().getTxOutPublicKey(),
                atTwo.getChange().getTxOutPublicKey());
    }

    @Test
    public void testDifferentSeedsDeriveDifferentKeys() throws Exception {
        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final Amount amountToSend = Amount.ofMOB(BigInteger.valueOf(52398457942L));
        final Amount fee = client.estimateTotalFee(amountToSend);
        final PublicAddress recipient = TestKeysManager.getNextAccountKey().getPublicAddress();

        final TxOutContexts first = client.getTxOutContexts(recipient, SEED);
        final TxOutContexts second = client.getTxOutContexts(recipient, OTHER_SEED);

        assertNotEquals(
                first.getPayload().getTxOutPublicKey(),
                second.getPayload().getTxOutPublicKey());
    }

    /**
     * The seed a {@link TransactionBuilder} runs on is derived from the
     * caller's RNG, and the two platforms derive it differently: this takes
     * {@code nextBytes(32)}, where MobileCoin-Swift takes four {@code next()}
     * UInt64s reduced into {@code Data}. Both should yield the same 32 bytes
     * off the same stream, because {@code next_u64} combines two u32 words as
     * {@code w[i+1] << 32 | w[i]} and its little-endian bytes are exactly what
     * {@code fill_bytes} writes for those words.
     * <p>
     * Nothing else in the derivation is platform-specific — the builder's own
     * stream and {@code add_output} are the same Rust through FFI — so this
     * hop is the only place Android and iOS can disagree about a TxOut public
     * key. {@code SeedableRngUnitTests.testBuilderSeedIsPlatformIndependent}
     * in MobileCoin-Swift asserts the same seed against these same bytes; the
     * two must be changed together or not at all.
     */
    @Test
    public void testBuilderSeedIsPlatformIndependent() {
        final byte[] builderSeed =
                ChaCha20Rng.fromSeed(SEED).nextBytes(ChaCha20Rng.SEED_SIZE_BYTES);

        assertEquals(
                "7606151ea291727acfbea41cc1e71d57b1e219e3aeb8accfa3a9bcbc190bc3f5",
                Hex.toString(builderSeed));
    }

    /**
     * The public key is {@code r * D}, so the recipient is as much an input to
     * it as the seed. A caller holding only a seed cannot know the key.
     */
    @Test
    public void testRecipientChangesThePayloadKey() throws Exception {
        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final Amount amountToSend = Amount.ofMOB(BigInteger.valueOf(52398457942L));
        final Amount fee = client.estimateTotalFee(amountToSend);

        final TxOutContexts toRecipient = client.getTxOutContexts(TestKeysManager.getNextAccountKey().getPublicAddress(), SEED);
        final TxOutContexts toSelf = client.getTxOutContexts(client.getAccountKey().getPublicAddress(), SEED);

        assertNotEquals(
                toRecipient.getPayload().getTxOutPublicKey(),
                toSelf.getPayload().getTxOutPublicKey());
    }

}
