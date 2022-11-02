package com.mobilecoin.lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class SignedContingentInputTest {


    private static final TokenId eUSD = TokenId.from(UnsignedLong.fromLongBits(8192));

    @Test
    public void testSignedContingentInputBuilder() throws Exception {

        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();

        final SignedContingentInput sci = client.createSignedContingentInput(
                Amount.ofMOB(new BigInteger("10000000000000")),
                new Amount(new BigInteger("10000000"),TokenId.from(UnsignedLong.ONE))
        );

        final Amount[] requiredAmounts = sci.getRequiredOutputAmounts();
        final Amount pseudoOutputAmount = sci.getPseudoOutputAmount();

        assertTrue(sci.isValid());
        assertTrue((requiredAmounts.length > 0) && (requiredAmounts.length <= 2));

        // Test Serialization
        byte[] serializedSci = sci.toByteArray();
        final SignedContingentInput reconstructed = SignedContingentInput.fromByteArray(serializedSci);
        assertArrayEquals(serializedSci, reconstructed.toByteArray());
        assertEquals(sci.getPseudoOutputAmount(), reconstructed.getPseudoOutputAmount());
        assertArrayEquals(sci.getRequiredOutputAmounts(), reconstructed.getRequiredOutputAmounts());

        // Test Parcelable
        final Parcel parcel = Parcel.obtain();
        sci.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        final SignedContingentInput deparceled = SignedContingentInput.CREATOR.createFromParcel(parcel);
        assertEquals(sci, deparceled);
        assertArrayEquals(sci.toByteArray(), deparceled.toByteArray());

        assertEquals(11, sci.getRing().length);
        client.shutdown();

    }

    @Test
    public void testCancelSignedContingentInput() throws Exception {

        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        final MobileCoinClient otherClient = MobileCoinClientBuilder.newBuilder().build();

        final Amount requiredAmount = new Amount(new BigInteger("10000000"), eUSD);
        final SignedContingentInput sci = client.createSignedContingentInput(
                Amount.ofMOB(new BigInteger("10000000000000")),
                requiredAmount
        );

        final Amount cancelationFee = client.estimateTotalFee(Amount.ofMOB(new BigInteger("10000000000000")));

        assertEquals(SignedContingentInput.CancelationResult.FAILED_UNOWNED_TX_OUT, otherClient.cancelSignedContingentInput(sci, cancelationFee));
        assertEquals(SignedContingentInput.CancelationResult.SUCCESS, client.cancelSignedContingentInput(sci, cancelationFee));

        client.shutdown();
        otherClient.shutdown();

    }

    @Test
    public void testBuildTransactionWithPresignedInput() throws Exception {

        final MobileCoinClient builderClient = MobileCoinClientBuilder.newBuilder().build();
        final MobileCoinClient consumerClient = MobileCoinClientBuilder.newBuilder().build();

        final Amount requiredAmount = new Amount(new BigInteger("10000000"), eUSD);
        final SignedContingentInput sci = builderClient.createSignedContingentInput(
                Amount.ofMOB(new BigInteger("10000000000000")),
                requiredAmount
        );

        assertTrue(sci.isValid());

        final Amount fee = consumerClient.getOrFetchMinimumTxFee(TokenId.MOB);

        consumerClient.prepareTransaction(sci, fee);

    }

}
