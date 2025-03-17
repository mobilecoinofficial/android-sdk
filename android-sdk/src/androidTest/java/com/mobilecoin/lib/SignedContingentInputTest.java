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

        if(Environment.CURRENT_TEST_ENV != Environment.TestEnvironment.MOBILE_DEV) return;

        final MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();

        final SignedContingentInput sci = client.createSignedContingentInput(
                Amount.ofMOB(new BigInteger("10000000000000")),
                new Amount(new BigInteger("10000000"),TokenId.from(UnsignedLong.ONE))
        );

        final Amount[] requiredAmounts = sci.getRequiredOutputAmounts();
        final Amount pseudoOutputAmount = sci.getPseudoOutputAmount();

        assertTrue(sci.isValid(true));
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

        if(Environment.CURRENT_TEST_ENV != Environment.TestEnvironment.MOBILE_DEV) return;

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

        if(Environment.CURRENT_TEST_ENV != Environment.TestEnvironment.MOBILE_DEV) return;

        final MobileCoinClient builderClient = MobileCoinClientBuilder.newBuilder().build();
        final MobileCoinClient consumerClient = MobileCoinClientBuilder.newBuilder().build();

        final Map<TokenId, Balance> builderBalancesBefore = builderClient.getBalances();
        final Map<TokenId, Balance> consumerBalancesBefore = consumerClient.getBalances();

        final Amount requiredAmount = new Amount(new BigInteger("100"), eUSD);
        final Amount fee = consumerClient.getOrFetchMinimumTxFee(TokenId.MOB);
        final Amount rewardAmount = fee.multiply(Amount.ofMOB(BigInteger.TEN));

        final SignedContingentInput sci = builderClient.createSignedContingentInput(
                rewardAmount,
                requiredAmount
        );

        assertTrue(sci.isValid(true));

        Transaction transaction = consumerClient.prepareTransaction(sci, fee);
        consumerClient.submitTransaction(transaction);

        Thread.sleep(10000L);// TODO: Create working wait for transaction status for SCI transactions

        // Check that builder client received required amount
        assertEquals(builderBalancesBefore.get(eUSD).getValue().add(requiredAmount.getValue()), builderClient.getBalance(eUSD).getValue());
        // Check that builder client spent the reward amount
        assertEquals(builderBalancesBefore.get(TokenId.MOB).getValue().subtract(rewardAmount.getValue()), builderClient.getBalance(TokenId.MOB).getValue());

        // Check the consumer client received reward amount minus fees
        assertEquals(consumerBalancesBefore.get(TokenId.MOB).getValue().add(rewardAmount.getValue().subtract(fee.getValue())), consumerClient.getBalance(TokenId.MOB).getValue());
        // Check that the consumer client spent the required amount
        assertEquals(consumerBalancesBefore.get(eUSD).getValue().subtract(requiredAmount.getValue()), consumerClient.getBalance(eUSD).getValue());

        builderClient.shutdown();
        consumerClient.shutdown();

    }

}
