package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.network.TransportProtocol;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MobileCoinViewClientTest {

    @Test
    public void testMobileCoinViewClientFetchTxOuts() throws Exception {

        final TestFogConfig fogConfig = TestFogConfig.getFogConfig(Environment.CURRENT_TEST_ENV);

        final MobileCoinClient fullClient = MobileCoinClientBuilder.newBuilder().build();
        final MobileCoinViewClient viewClient = new MobileCoinViewClient(
                new ViewAccountKey(fullClient.getAccountKey()),
                fogConfig.getFogUri(),
                fogConfig.getConsensusUri(),
                TransportProtocol.forGRPC()
        );

        fullClient.getTxOutStore().refresh(fullClient.viewClient, fullClient.ledgerClient, fullClient.fogBlockClient);
        final Balance fullClientBalance = new Balance(fullClient.getTxOutStore().getSyncedTxOuts().stream()
                .map(OwnedTxOut::getAmount)
                .reduce(Amount::add)
                .filter(a -> a.getTokenId().equals(TokenId.MOB)).get().getValue(), fullClient.getTxOutStore().getCurrentBlockIndex());
        final Balance viewClientBalance = viewClient.getBalance(TokenId.MOB);

        assertEquals(fullClientBalance, viewClientBalance);

    }

}
