package com.mobilecoin.lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;

@RunWith(AndroidJUnit4.class)
public class SignedContingentInputTest {

    @Test
    public void testSignedContingentInputBuilder() throws Exception {

        MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        SignedContingentInput sci = client.createSignedContingentInput(
                Amount.ofMOB(new BigInteger("10000000000000")),
                new Amount(new BigInteger("10000000"), TokenId.from(UnsignedLong.ONE))
        );
        client.shutdown();

        Amount[] requiredAmounts = sci.getRequiredOutputAmounts();
        Amount pseudoOutputAmount = sci.getPseudoOutputAmount();

        assertTrue(sci.isValid());
        assertEquals(2, requiredAmounts.length);

        // Test Serialization
        byte[] serializedSci = sci.toByteArray();
        SignedContingentInput reconstructed = SignedContingentInput.fromByteArray(serializedSci);
        assertArrayEquals(serializedSci, reconstructed.toByteArray());
        assertEquals(sci.getPseudoOutputAmount(), reconstructed.getPseudoOutputAmount());
        assertArrayEquals(sci.getRequiredOutputAmounts(), reconstructed.getRequiredOutputAmounts());

    }

    @Test
    public void testBuildTransactionWithPresignedInput() throws Exception {
        //
    }

}
