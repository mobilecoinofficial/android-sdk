package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.InvalidTransactionException;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class TokenIdTest {

/*
 ********************************************************************
 * Unit Tests
 ********************************************************************
 */

    //Not using TokenId.MOB because test should be valid regardless of its ID
    @Test
    public void testGetUnspentTxOuts() throws Exception {
        TxOutStore txOutStore = mock(TxOutStore.class);
        doNothing().when(txOutStore).refresh(any(), any(), any());
        Set<OwnedTxOut> unspentTxOuts = new HashSet<OwnedTxOut>();

        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.TEN, TokenId.from(UnsignedLong.ZERO))));
        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.ONE, TokenId.from(UnsignedLong.ZERO))));
        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.ZERO, TokenId.from(UnsignedLong.ZERO))));
        unspentTxOuts.add(createMockTxOut(new Amount(new BigInteger("230"), TokenId.from(UnsignedLong.ZERO))));

        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.TEN, TokenId.from(UnsignedLong.ONE))));
        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.ONE, TokenId.from(UnsignedLong.ONE))));

        unspentTxOuts.add(createMockTxOut(new Amount(new BigInteger("4325"), TokenId.from(UnsignedLong.TEN))));

        when(txOutStore.getUnspentTxOuts()).thenReturn(unspentTxOuts);

        MobileCoinClient client = new MobileCoinClient(
                null,
                txOutStore,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(client.getAllUnspentTxOuts().size(), 7);
        assertEquals(client.getUnspentTxOuts(TokenId.from(UnsignedLong.ZERO)).size(), 4);
        assertEquals(client.getUnspentTxOuts(TokenId.from(UnsignedLong.ONE)).size(), 2);
        assertEquals(client.getUnspentTxOuts(TokenId.from(UnsignedLong.TEN)).size(), 1);
        assertEquals(client.getUnspentTxOuts(TokenId.from(UnsignedLong.fromLongBits(37))).size(), 0);

        Amount tokenIdZeroTotal = client.getUnspentTxOuts(TokenId.from(UnsignedLong.ZERO)).stream()
                .map(OwnedTxOut::getAmount)
                .reduce(Amount::add).get();
        assertEquals(new Amount(new BigInteger("241"), TokenId.from(UnsignedLong.ZERO)), tokenIdZeroTotal);

        Amount tokenIdOneTotal = client.getUnspentTxOuts(TokenId.from(UnsignedLong.ONE)).stream()
                .map(OwnedTxOut::getAmount)
                .reduce(Amount::add).get();
        assertEquals(new Amount(new BigInteger("11"), TokenId.from(UnsignedLong.ONE)), tokenIdOneTotal);

        Amount tokenIdTenTotal = client.getUnspentTxOuts(TokenId.from(UnsignedLong.TEN)).stream()
                .map(OwnedTxOut::getAmount)
                .reduce(Amount::add).get();
        assertEquals(new Amount(new BigInteger("4325"), TokenId.from(UnsignedLong.TEN)), tokenIdTenTotal);

        // Filtering by token ID 11 should return no OwnedTxOuts
        assertFalse(client.getUnspentTxOuts(TokenId.from(UnsignedLong.TEN.add(UnsignedLong.ONE))).stream()
                .map(OwnedTxOut::getAmount)
                .reduce(Amount::add).isPresent());

    }

    //Not using TokenId.MOB because test should be valid regardless of its ID
    @Test
    public void getBalancesUnitTest() throws Exception {
        TxOutStore txOutStore = mock(TxOutStore.class);
        when(txOutStore.getCurrentBlockIndex()).thenReturn(UnsignedLong.TEN);
        doNothing().when(txOutStore).refresh(any(), any(), any());
        Set<OwnedTxOut> unspentTxOuts = new HashSet<OwnedTxOut>();

        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.TEN, TokenId.from(UnsignedLong.ZERO))));
        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.ONE, TokenId.from(UnsignedLong.ZERO))));
        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.ZERO, TokenId.from(UnsignedLong.ZERO))));
        unspentTxOuts.add(createMockTxOut(new Amount(new BigInteger("230"), TokenId.from(UnsignedLong.ZERO))));
        unspentTxOuts.add(createMockTxOut(new Amount(new BigInteger("4325512"), TokenId.from(UnsignedLong.ZERO))));
        unspentTxOuts.add(createMockTxOut(new Amount(new BigInteger("69238"), TokenId.from(UnsignedLong.ZERO))));

        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.TEN, TokenId.from(UnsignedLong.ONE))));
        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.ONE, TokenId.from(UnsignedLong.ONE))));
        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.TEN, TokenId.from(UnsignedLong.ONE))));
        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.TEN, TokenId.from(UnsignedLong.ONE))));
        unspentTxOuts.add(createMockTxOut(new Amount(new BigInteger("53423"), TokenId.from(UnsignedLong.ONE))));

        unspentTxOuts.add(createMockTxOut(new Amount(new BigInteger("6534"), TokenId.from(UnsignedLong.TEN))));
        unspentTxOuts.add(createMockTxOut(new Amount(BigInteger.TEN, TokenId.from(UnsignedLong.TEN))));
        unspentTxOuts.add(createMockTxOut(new Amount(new BigInteger("10975"), TokenId.from(UnsignedLong.TEN))));
        unspentTxOuts.add(createMockTxOut(new Amount(new BigInteger("9572983"), TokenId.from(UnsignedLong.TEN))));

        when(txOutStore.getUnspentTxOuts()).thenReturn(unspentTxOuts);

        MobileCoinClient client = new MobileCoinClient(
                null,
                txOutStore,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        Map<TokenId, Balance> client1Balances = client.getBalances();
        for(Map.Entry<TokenId, Balance> entry : client1Balances.entrySet()) {
            assertEquals(entry.getValue().getValue(), client.getBalance(entry.getKey()).getValue());
        }

        Amount tokenIdZeroTotal = client.getUnspentTxOuts(TokenId.from(UnsignedLong.ZERO)).stream()
                .map(OwnedTxOut::getAmount)
                .reduce(Amount::add).get();
        assertEquals(new Amount(new BigInteger("4394991"), TokenId.from(UnsignedLong.ZERO)), tokenIdZeroTotal);

        Amount tokenIdOneTotal = client.getUnspentTxOuts(TokenId.from(UnsignedLong.ONE)).stream()
                .map(OwnedTxOut::getAmount)
                .reduce(Amount::add).get();
        assertEquals(new Amount(new BigInteger("53454"), TokenId.from(UnsignedLong.ONE)), tokenIdOneTotal);

        Amount tokenIdTenTotal = client.getUnspentTxOuts(TokenId.from(UnsignedLong.TEN)).stream()
                .map(OwnedTxOut::getAmount)
                .reduce(Amount::add).get();
        assertEquals(new Amount(new BigInteger("9590502"), TokenId.from(UnsignedLong.TEN)), tokenIdTenTotal);

        // Filtering byt token ID 11 should return no OwnedTxOuts
        assertFalse(client.getUnspentTxOuts(TokenId.from(UnsignedLong.TEN.add(UnsignedLong.ONE))).stream()
                .map(OwnedTxOut::getAmount)
                .reduce(Amount::add).isPresent());

    }

    //Not using TokenId.MOB because test should be valid regardless of its ID
    @Test
    public void testMixedTransactionFails() throws Exception {
        MobileCoinClient client = new MobileCoinClient(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        try {
            client.prepareTransaction(
                    null,
                    new Amount(BigInteger.TEN, TokenId.from(UnsignedLong.ZERO)),
                    new Amount(BigInteger.ONE, TokenId.from(UnsignedLong.ONE)),
                    null
            );
            fail("Mixed transactions should fail.");
        } catch(IllegalArgumentException e) {}
        try {
            new AccountSnapshot(null, null, null)
                    .prepareTransaction(null,
                            new Amount(BigInteger.TEN, TokenId.from(UnsignedLong.ZERO)),
                            new Amount(BigInteger.ONE, TokenId.from(UnsignedLong.ONE)),
                            null
                    );
            fail("Mixed transactions should fail.");
        } catch(IllegalArgumentException e) {}
    }

/*
 ********************************************************************
 * Integration Tests
 ********************************************************************
 */

    @Test
    public void testAccountHasBalance() throws Exception {
        if(Environment.CURRENT_TEST_ENV != Environment.TestEnvironment.MOBILE_DEV) return;// TODO: remove check when token IDs on other nets
        MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        Balance mobBalance = client.getBalance(TokenId.MOB);
        Balance mobUsdBalance = client.getBalance(eUSD);
        assertTrue("0 MOB balance",
                mobBalance.getValue().compareTo(BigInteger.ZERO) > 0);
        assertTrue("0 MOB USD balance",
                mobUsdBalance.getValue().compareTo(BigInteger.ZERO) > 0);
        client.shutdown();
    }

    @Test
    public void testGetBalances() throws Exception {
        if(Environment.CURRENT_TEST_ENV != Environment.TestEnvironment.MOBILE_DEV) return;// TODO: remove check when token IDs on other nets
        MobileCoinClient client1 = MobileCoinClientBuilder.newBuilder().build();
        MobileCoinClient client2 = MobileCoinClientBuilder.newBuilder().build();
        Map<TokenId, Balance> client1Balances = client1.getBalances();
        for(Map.Entry<TokenId, Balance> entry : client1Balances.entrySet()) {
            assertEquals(entry.getValue().getValue(), client1.getBalance(entry.getKey()).getValue());
        }
        Map<TokenId, Balance> client2Balances = client2.getBalances();
        for(Map.Entry<TokenId, Balance> entry : client2Balances.entrySet()) {
            assertEquals(entry.getValue().getValue(), client2.getBalance(entry.getKey()).getValue());
        }
        client1.shutdown();
        client2.shutdown();
    }

    @Test
    public void testMobUsdTransfer() throws Exception {
        if(Environment.CURRENT_TEST_ENV != Environment.TestEnvironment.MOBILE_DEV) return;// TODO: remove check when token IDs on other nets
        MobileCoinClient senderClient = MobileCoinClientBuilder.newBuilder().build();
        MobileCoinClient recipientClient = MobileCoinClientBuilder.newBuilder().build();

        Balance senderMobBalanceBefore = senderClient.getBalance(TokenId.MOB);
        Balance senderMobUsdBalanceBefore = senderClient.getBalance(eUSD);
        Balance recipientMobBalanceBefore = recipientClient.getBalance(TokenId.MOB);
        Balance recipientMobUsdBalanceBefore = recipientClient.getBalance(eUSD);

        Amount amountToSend = new Amount(BigInteger.TEN, eUSD);
        Amount fee = senderClient.estimateTotalFee(amountToSend);
        PendingTransaction pendingTransaction =
            senderClient.prepareTransaction(
                recipientClient.getAccountKey().getPublicAddress(),
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(senderClient.getAccountKey())
            );
        senderClient.submitTransaction(pendingTransaction.getTransaction());
        UtilTest.waitForTransactionStatus(senderClient, pendingTransaction.getTransaction());

        Balance senderMobBalanceAfter = senderClient.getBalance(TokenId.MOB);
        Balance senderMobUsdBalanceAfter = senderClient.getBalance(eUSD);
        Balance recipientMobBalanceAfter = recipientClient.getBalance(TokenId.MOB);
        Balance recipientMobUsdBalanceAfter = recipientClient.getBalance(eUSD);

        //Check that MOB USD balances updated and MOB balances didn't
        assertEquals(senderMobBalanceBefore.getValue(), senderMobBalanceAfter.getValue());
        assertNotEquals(senderMobUsdBalanceBefore.getValue(), senderMobUsdBalanceAfter.getValue());
        assertEquals(recipientMobBalanceBefore.getValue(), recipientMobBalanceAfter.getValue());
        assertNotEquals(recipientMobUsdBalanceBefore.getValue(), recipientMobUsdBalanceAfter.getValue());

        //Check that MOB USD balances updated properly
        assertEquals(
                senderMobUsdBalanceBefore.getValue(),
                senderMobUsdBalanceAfter.getValue().add(amountToSend.getValue()).add(fee.getValue())
        );
        assertEquals(
                recipientMobUsdBalanceAfter.getValue(),
                recipientMobUsdBalanceBefore.getValue().add(amountToSend.getValue())
        );

        senderClient.shutdown();
        recipientClient.shutdown();
    }

    @Test
    public void testMobUSDWithWrongFee() throws Exception {
        if(Environment.CURRENT_TEST_ENV != Environment.TestEnvironment.MOBILE_DEV) return;// TODO: remove check when token IDs on other nets
        MobileCoinClient senderClient = MobileCoinClientBuilder.newBuilder().build();
        PublicAddress recipientAddress = TestKeysManager.getNextAccountKey().getPublicAddress();
        Amount amountToSend = new Amount(new BigInteger("43252"), eUSD);
        Amount fee = senderClient
                .estimateTotalFee(amountToSend);
        fee = fee.divide(new Amount(BigInteger.TEN, eUSD));
        PendingTransaction pendingTransaction = senderClient.prepareTransaction(
                recipientAddress,
                amountToSend,
                fee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(senderClient.getAccountKey())
        );
        try {
            senderClient.submitTransaction(pendingTransaction.getTransaction());
            fail("Sending MOB USD transaction with invalid fee should fail.");
        } catch(InvalidTransactionException e) {}
        finally {
            senderClient.shutdown();
        }
    }

    @Test
    public void testMobUsdSnapshotTransfer() throws Exception {
        if(Environment.CURRENT_TEST_ENV != Environment.TestEnvironment.MOBILE_DEV) return;// TODO: remove check when token IDs on other nets
        MobileCoinClient senderClient = MobileCoinClientBuilder.newBuilder().build();
        MobileCoinClient recipientClient = MobileCoinClientBuilder.newBuilder().build();

        AccountSnapshot snapshot = senderClient.getAccountSnapshot();

        Balance senderMobBalanceBefore = snapshot.getBalance(TokenId.MOB);
        Balance senderMobUsdBalanceBefore = snapshot.getBalance(eUSD);
        Balance recipientMobBalanceBefore = recipientClient.getBalance(TokenId.MOB);
        Balance recipientMobUsdBalanceBefore = recipientClient.getBalance(eUSD);

        Amount amountToSend = new Amount(BigInteger.TEN, eUSD);
        Amount fee = snapshot.estimateTotalFee(amountToSend, senderClient.getOrFetchMinimumTxFee(eUSD));
        PendingTransaction pendingTransaction =
                snapshot.prepareTransaction(
                        recipientClient.getAccountKey().getPublicAddress(),
                        amountToSend,
                        fee,
                        TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(senderClient.getAccountKey())
                );
        senderClient.submitTransaction(pendingTransaction.getTransaction());
        UtilTest.waitForTransactionStatus(senderClient, pendingTransaction.getTransaction());

        Balance senderMobBalanceAfter = senderClient.getBalance(TokenId.MOB);
        Balance senderMobUsdBalanceAfter = senderClient.getBalance(eUSD);
        Balance recipientMobBalanceAfter = recipientClient.getBalance(TokenId.MOB);
        Balance recipientMobUsdBalanceAfter = recipientClient.getBalance(eUSD);

        //Check that MOB USD balances updated and MOB balances didn't
        assertEquals(senderMobBalanceBefore.getValue(), senderMobBalanceAfter.getValue());
        assertNotEquals(senderMobUsdBalanceBefore.getValue(), senderMobUsdBalanceAfter.getValue());
        assertEquals(recipientMobBalanceBefore.getValue(), recipientMobBalanceAfter.getValue());
        assertNotEquals(recipientMobUsdBalanceBefore.getValue(), recipientMobUsdBalanceAfter.getValue());

        //Check that MOB USD balances updated properly
        assertEquals(
                senderMobUsdBalanceBefore.getValue(),
                senderMobUsdBalanceAfter.getValue().add(amountToSend.getValue()).add(fee.getValue())
        );
        assertEquals(
                recipientMobUsdBalanceAfter.getValue(),
                recipientMobUsdBalanceBefore.getValue().add(amountToSend.getValue())
        );

        senderClient.shutdown();
        recipientClient.shutdown();
    }

    @Test
    public void testClientGetFeeHasCorrectTokenId() throws Exception {
        if(Environment.CURRENT_TEST_ENV != Environment.TestEnvironment.MOBILE_DEV) return;// TODO: remove check when token IDs on other nets
        MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        Amount amountToSendMOB = new Amount(BigInteger.TEN, TokenId.MOB);
        Amount feeMOB = client.estimateTotalFee(amountToSendMOB);
        assertEquals(amountToSendMOB.getTokenId(), feeMOB.getTokenId());
        Amount amountToSendMOBUSD = new Amount(BigInteger.TEN, eUSD);
        Amount feeMOBUSD = client.estimateTotalFee(amountToSendMOBUSD);
        assertEquals(amountToSendMOBUSD.getTokenId(), feeMOBUSD.getTokenId());
        client.shutdown();
    }

    @Test
    public void testSnapshotGetFeeHasCorrectTokenId() throws Exception {
        if(Environment.CURRENT_TEST_ENV != Environment.TestEnvironment.MOBILE_DEV) return;// TODO: remove check when token IDs on other nets
        MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        AccountSnapshot snapshot = client.getAccountSnapshot();
        Amount amountToSendMOB = new Amount(BigInteger.TEN, TokenId.MOB);
        Amount feeMOB = snapshot.estimateTotalFee(amountToSendMOB, client.getOrFetchMinimumTxFee(TokenId.MOB));
        assertEquals(amountToSendMOB.getTokenId(), feeMOB.getTokenId());
        Amount amountToSendMOBUSD = new Amount(BigInteger.TEN, eUSD);
        Amount feeMOBUSD = snapshot.estimateTotalFee(amountToSendMOBUSD, client.getOrFetchMinimumTxFee(eUSD));
        assertEquals(amountToSendMOBUSD.getTokenId(), feeMOBUSD.getTokenId());
        client.shutdown();
    }

    private static OwnedTxOut createMockTxOut(@NonNull Amount amount) {
        OwnedTxOut mock = mock(OwnedTxOut.class);
        when(mock.getAmount()).thenReturn(amount);
        return mock;
    }

    private static final TokenId eUSD = TokenId.from(UnsignedLong.fromLongBits(8192L));

}
