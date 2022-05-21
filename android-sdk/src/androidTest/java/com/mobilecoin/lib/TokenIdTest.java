package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.log.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class TokenIdTest {

    @Test
    public void testGetBalance() throws Exception {
        MobileCoinClient client = MobileCoinClientBuilder.newBuilder()
                .setAccountKey(createAccountKeyFromMnemonic(ACCOUNT_WITH_MOBUSD_1))
                .build();
        Balance mobBalance = client.getBalance(KnownTokenId.MOB.getId());
        Balance mobUsdBalance = client.getBalance(UnsignedLong.ONE);//TODO: Update with KnownTokenId
        assertTrue("0 MOB balance",
                mobBalance.getValue().compareTo(BigInteger.ZERO) > 0);
        assertTrue("0 MOB USD balance",
                mobUsdBalance.getValue().compareTo(BigInteger.ZERO) > 0);
        client.shutdown();
    }

    @Test
    public void testGetBalances() throws Exception {
        MobileCoinClient client1 = MobileCoinClientBuilder.newBuilder()
                .setAccountKey(createAccountKeyFromMnemonic(ACCOUNT_WITH_MOBUSD_1))
                .build();
        MobileCoinClient client2 = MobileCoinClientBuilder.newBuilder()
                .setAccountKey(createAccountKeyFromMnemonic(ACCOUNT_WITH_MOBUSD_2))
                .build();
        Map<UnsignedLong, Balance> client1Balances = client1.getBalances();
        for(Map.Entry<UnsignedLong, Balance> entry : client1Balances.entrySet()) {
            assertEquals(entry.getValue().getValue(), client1.getBalance(entry.getKey()).getValue());
        }
        Map<UnsignedLong, Balance> client2Balances = client2.getBalances();
        for(Map.Entry<UnsignedLong, Balance> entry : client2Balances.entrySet()) {
            assertEquals(entry.getValue().getValue(), client2.getBalance(entry.getKey()).getValue());
        }
        client1.shutdown();
        client2.shutdown();
    }

    @Test
    public void testMobUsdTransfer() throws Exception {
        MobileCoinClient senderClient = MobileCoinClientBuilder.newBuilder()
                .setAccountKey(createAccountKeyFromMnemonic(ACCOUNT_WITH_MOBUSD_1))
                .build();
        MobileCoinClient recipientClient = MobileCoinClientBuilder.newBuilder()
                .setAccountKey(createAccountKeyFromMnemonic(ACCOUNT_WITH_MOBUSD_2))
                .build();

        //TODO: update with KnownTokenId
        Balance senderMobBalanceBefore = senderClient.getBalance(KnownTokenId.MOB.getId());
        Balance senderMobUsdBalanceBefore = senderClient.getBalance(UnsignedLong.ONE);
        Balance recipientMobBalanceBefore = recipientClient.getBalance(KnownTokenId.MOB.getId());
        Balance recipientMobUsdBalanceBefore = recipientClient.getBalance(UnsignedLong.ONE);

        Amount amountToSend = new Amount(BigInteger.TEN, UnsignedLong.ONE);//TODO: update with KnownTokenId
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

        //TODO: update with KnownTokenId
        Balance senderMobBalanceAfter = senderClient.getBalance(KnownTokenId.MOB.getId());
        Balance senderMobUsdBalanceAfter = senderClient.getBalance(UnsignedLong.ONE);
        Balance recipientMobBalanceAfter = recipientClient.getBalance(KnownTokenId.MOB.getId());
        Balance recipientMobUsdBalanceAfter = recipientClient.getBalance(UnsignedLong.ONE);

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

    private static AccountKey createAccountKeyFromMnemonic(String mnemonic) throws Exception {
        return AccountKey.fromMnemonicPhrase(
                mnemonic,
                0,
                Environment.getTestFogConfig().getFogUri(),
                Environment.getTestFogConfig().getFogReportId(),
                Environment.getTestFogConfig().getFogAuthoritySpki()
        );
    }

    public static final String ACCOUNT_WITH_MOBUSD_1 = "action sphere soft mercy month frown learn renew bottom pattern attend level chat neglect miracle cause decorate convince hand bread live execute grass palace";
    public static final String ACCOUNT_WITH_MOBUSD_2 = "typical shine grocery luggage lizard latin food warrior achieve leave season furnace seminar else verify toy result style captain cotton spare survey fame panther";

}
