package com.mobilecoin.lib;

import static com.mobilecoin.lib.UtilTest.waitForTransactionStatus;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.http.Requester.HttpRequester;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;

@RunWith(AndroidJUnit4.class)
public class MobileCoinClientWithHttpRequesterTest {

    // Tests RestFogBlockService, RestFogKeyImageService, and RestFogViewService
    @Test
    public void getBalance_afterSetTransportProtocolWithHTTP_retrievesBalance() throws Exception {
        TestFogConfig config = Environment.getTestFogConfig();
        TransportProtocol httpTransportProtocol = TransportProtocol.forHTTP(new HttpRequester(config.getUsername(), config.getPassword()));

        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder()
                .setTransportProtocol(httpTransportProtocol)
                .build();

        Balance balance = mobileCoinClient.getBalance();
        Assert.assertNotNull(balance);
    }

    // Tests RestBlockchainService.
    @Test
    public void getOrFetchMinimumTxFee_afterSetTransportProtocolWithHTTP_retrievesTransferableAmount() throws Exception {
        TestFogConfig config = Environment.getTestFogConfig();
        TransportProtocol httpTransportProtocol = TransportProtocol.forHTTP(new HttpRequester(config.getUsername(), config.getPassword()));

        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder()
                .setTransportProtocol(httpTransportProtocol)
                .build();

        BigInteger minimumTxFee = mobileCoinClient.getOrFetchMinimumTxFee();

        Assert.assertNotNull(minimumTxFee);
    }

    // Tests RestConsensusClientService, RestFogMerkleProofService, RestFogReportService,
    // and RestFogUntrustedService.
    @Test
    public void submitTransaction_afterSetTransportProtocolWithHTTP_submitsTransaction() throws Exception {
        TestFogConfig config = Environment.getTestFogConfig();
        TransportProtocol httpTransportProtocol = TransportProtocol.forHTTP(new HttpRequester(config.getUsername(), config.getPassword()));

        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder()
                .setTransportProtocol(httpTransportProtocol)
                .build();

        AccountKey recipient = TestKeysManager.getNextAccountKey();
        try {
            BigInteger amount = BigInteger.TEN;
            BigInteger minimumFee = mobileCoinClient.estimateTotalFee(
                    amount
            );
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
