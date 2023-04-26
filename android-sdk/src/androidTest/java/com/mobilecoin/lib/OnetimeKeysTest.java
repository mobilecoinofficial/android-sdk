package com.mobilecoin.lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.mobilecoin.lib.log.Logger;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

public class OnetimeKeysTest {

    @Test
    public void testTxOutPublicKeyGeneration() throws Exception {
        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final PublicAddress recipient = TestKeysManager.getNextAccountKey().getPublicAddress();
        final byte[] rngSeed = DefaultRng.createInstance().nextBytes(ChaCha20Rng.SEED_SIZE_BYTES);
        final PendingTransaction tx = client.prepareTransaction(
                recipient,
                Amount.ofMOB(BigInteger.ZERO),
                Amount.ofMOB(BigInteger.TEN),
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey()),
                ChaCha20Rng.fromSeed(rngSeed)
        );
        final RistrettoPublic txOutPublicKey = tx.getPayloadTxOutContext().getTxOutPublicKey();
        final RistrettoPublic changePublicKey = tx.getChangeTxOutContext().getTxOutPublicKey();

        final RistrettoPublic recipientSpendPublic = recipient.getSpendKey();
        final RistrettoPublic senderSpendPublic = client.getAccountKey().getChangeSubAddressSpendKey().getPublicKey();
        // prepareTransaction uses the RNG we pass to generate a seed which TxBuilder uses to create a dedicated output RNG
        final ChaCha20Rng outputRng = ChaCha20Rng.fromSeed(ChaCha20Rng.fromSeed(rngSeed).nextBytes(ChaCha20Rng.SEED_SIZE_BYTES));

        outputRng.setWordPos(BigInteger.valueOf(16L));
        final RistrettoPrivate txOutPrivateKey = RistrettoPrivate.fromRandom(outputRng);

        outputRng.setWordPos(BigInteger.valueOf(48L));
        final RistrettoPrivate changePrivateKey = RistrettoPrivate.fromRandom(outputRng);

        final RistrettoPublic generatedTxOutPublicKey = OnetimeKeys.createTxOutPublicKey(txOutPrivateKey, recipientSpendPublic);
        final RistrettoPublic generatedChangePublicKey = OnetimeKeys.createTxOutPublicKey(changePrivateKey, senderSpendPublic);

        assertEquals(txOutPublicKey, generatedTxOutPublicKey);
        assertEquals(changePublicKey, generatedChangePublicKey);
        assertArrayEquals(txOutPublicKey.getKeyBytes(), generatedTxOutPublicKey.getKeyBytes());
        assertArrayEquals(changePublicKey.getKeyBytes(), generatedChangePublicKey.getKeyBytes());

    }

}
