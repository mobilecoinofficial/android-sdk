package com.mobilecoin.lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;

@RunWith(AndroidJUnit4.class)
public class SignedContingentInputTest {

    @Test
    public void testSignedContingentInputBuilder() throws Exception {

        MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        SignedContingentInputBuilder sciBuilder = SignedContingentInputBuilder.newBuilder(
                Amount.ofMOB(new BigInteger("10000000000000")),
                client
        );
        sciBuilder.addRequiredAmount(
                new Amount(new BigInteger("10000000"), TokenId.from(UnsignedLong.ONE)),
                client.getAccountKey().getPublicAddress()
        );
        SignedContingentInput sci = sciBuilder.build();

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

        // Test Parcelable
        Parcel parcel = Parcel.obtain();
        sci.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        SignedContingentInput deparceled = SignedContingentInput.CREATOR.createFromParcel(parcel);
        assertEquals(sci, deparceled);
        assertArrayEquals(sci.toByteArray(), deparceled.toByteArray());

        assertEquals(11, sci.getRing().length);
        client.shutdown();

    }

    @Test
    public void testBuildTransactionWithPresignedInput() throws Exception {
        //
    }

}
