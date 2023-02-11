package com.mobilecoin.lib;

import static com.mobilecoin.lib.UtilTest.waitForTransactionStatus;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.http.Requester.HttpRequester;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;

@RunWith(AndroidJUnit4.class)
public class MobileCoinClientWithHttpRequesterTest {

    // Tests RestConsensusClientService, RestFogMerkleProofService, RestFogReportService,
    // and RestFogUntrustedService.
    @Test
    public void test_mobile_coin_client_with_http_requester() throws Exception {
        TestFogConfig config = Environment.getTestFogConfig();
        TransportProtocol httpTransportProtocol = TransportProtocol.forHTTP(new HttpRequester(config.getUsername(), config.getPassword()));

        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder()
                .setTransportProtocol(httpTransportProtocol)
                .build();

        final Balance balance = mobileCoinClient.getBalance(TokenId.MOB);
        assertNotNull(balance);
        assertTrue(balance.getValue().compareTo(BigInteger.ZERO) > 0);

        AccountKey recipient = TestKeysManager.getNextAccountKey();
        try {
            Amount amount = new Amount(BigInteger.TEN, TokenId.MOB);
            Amount minimumFee = mobileCoinClient.estimateTotalFee(amount);
            PendingTransaction pending = mobileCoinClient.prepareTransaction(
                    recipient.getPublicAddress(),
                    amount,
                    minimumFee,
                    TxOutMemoBuilder.createDefaultRTHMemoBuilder()
            );
            mobileCoinClient.submitTransaction(pending.getTransaction());
            Transaction.Status status = waitForTransactionStatus(
                    mobileCoinClient,
                    pending.getTransaction()
            );
            Assert.assertSame(
                    "Valid transaction must be accepted",
                    status,
                    Transaction.Status.ACCEPTED
            );
        } finally {
            mobileCoinClient.shutdown();
        }
    }
}
