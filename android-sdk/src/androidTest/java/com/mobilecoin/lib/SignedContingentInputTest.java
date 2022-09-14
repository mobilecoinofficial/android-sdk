package com.mobilecoin.lib;

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
                Amount.ofMOB(BigInteger.TEN),
                new Amount(BigInteger.TEN, TokenId.from(UnsignedLong.ZERO))
        );
        client.shutdown();

        Amount requiredAmounts[] = sci.getRequiredOutputAmounts();
        Amount pseudoOutputAmount = sci.getPseudoOutputAmount();

        assertTrue(sci.isValid());
        assertEquals(2, requiredAmounts.length);

    }

    @Test
    public void testBuildTransactionWithPresignedInput() throws Exception {
        //
    }

}
