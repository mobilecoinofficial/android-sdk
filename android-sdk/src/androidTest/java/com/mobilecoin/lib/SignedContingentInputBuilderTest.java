package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;

@RunWith(AndroidJUnit4.class)
public class SignedContingentInputBuilderTest {

    @Test
    public void testTestTest() throws Exception {

        TestKeysManager.getNextAccountKey();
        TestKeysManager.getNextAccountKey();
        MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        client.createSignedContingentInput(
                Amount.ofMOB(BigInteger.TEN),
                new Amount(BigInteger.TEN, TokenId.from(UnsignedLong.ONE)),
                TestKeysManager.getNextAccountKey().getPublicAddress()
        );
        client.shutdown();

    }

}
