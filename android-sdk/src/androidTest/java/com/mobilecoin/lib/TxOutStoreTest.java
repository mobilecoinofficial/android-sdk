// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.protobuf.ByteString;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FeeRejectedException;
import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.FragmentedAccountException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidReceiptException;
import com.mobilecoin.lib.exceptions.InvalidTransactionException;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.services.ServiceAPIManager;
import com.mobilecoin.lib.network.uri.FogUri;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fog_common.FogCommon;
import fog_view.View;
import kex_rng.KexRng;

public class TxOutStoreTest {
    private final TestFogConfig fogConfig = Environment.getTestFogConfig();

    @Test
    public void test_serialize_roundtrip()
            throws SerializationException, InvalidFogResponse, NetworkException,
            AttestationException, InvalidUriException {
        AccountKey accountKey = TestKeysManager.getNextAccountKey();
        MobileCoinClient mobileCoinClient = Environment.makeFreshMobileCoinClient(accountKey);
        TxOutStore store = new TxOutStore(accountKey);
        store.refresh(
                mobileCoinClient.viewClient,
                mobileCoinClient.ledgerClient,
                mobileCoinClient.fogBlockClient
        );
        mobileCoinClient.shutdown();
        Set<OwnedTxOut> utxos = store.getSyncedTxOuts();
        int storeSize = utxos.size();
        if (storeSize <= 0) {
            Assert.fail("UTXO store must contain non-zero TXOs");
        }

        BigInteger balance = BigInteger.ZERO;
        for (OwnedTxOut utxo : utxos) {
            balance = balance.add(utxo.getValue());
        }

        byte[] serialized = store.toByteArray();
        store = TxOutStore.fromBytes(
                serialized,
                accountKey
        );
        Set<OwnedTxOut> restoredUtxos = store.getSyncedTxOuts();
        int restoredStoreSize = restoredUtxos.size();
        Assert.assertEquals("Serialized and Deserialized stores sizes must be the same",
                storeSize,
                restoredStoreSize
        );

        BigInteger restoredBalance = BigInteger.ZERO;
        for (OwnedTxOut utxo : restoredUtxos) {
            restoredBalance = restoredBalance.add(utxo.getValue());
        }
        Assert.assertEquals("Balance must remain the same after serialization round trip",
                balance,
                restoredBalance
        );
    }

    @Test
    public void fetch_fog_misses_test()
            throws NetworkException, AttestationException, FragmentedAccountException,
            InsufficientFundsException, InvalidFogResponse, TransactionBuilderException,
            FeeRejectedException, InvalidTransactionException, InterruptedException,
            FogReportException, InvalidReceiptException, InvalidUriException {

        MobileCoinClient senderClient = Environment.makeFreshMobileCoinClient();
        MobileCoinClient recipientClient = Environment.makeFreshMobileCoinClient();

        // send a random amount
        BigInteger amount = BigInteger.valueOf(Math.abs(new SecureRandom().nextInt() % 100) + 1);
        BigInteger minimumFee = senderClient.estimateTotalFee(
                amount
        );
        PendingTransaction pending = senderClient.prepareTransaction(
                recipientClient.getAccountKey().getPublicAddress(),
                amount,
                minimumFee
        );
        senderClient.submitTransaction(pending.getTransaction());

        Transaction.Status status;
        do {
            Thread.sleep(1000);
            status = senderClient.getTransactionStatus(pending.getTransaction());
            assertTrue(status.getBlockIndex().compareTo(UnsignedLong.ZERO) > 0);
            // transaction status will change to FAILED if the current block index becomes
            // higher than transaction maximum heights
        } while (status == Transaction.Status.UNKNOWN);


        Receipt.Status receiptStatus;
        UnsignedLong txoBlock;
        do {
            receiptStatus = recipientClient.getReceiptStatus(pending.getReceipt());
            txoBlock = receiptStatus.getBlockIndex();
            Thread.sleep(1000);
        } while (receiptStatus == Receipt.Status.UNKNOWN);

        FogBlockClient blockClient = new FogBlockClient(
                new FogUri(fogConfig.getFogUri()),
                fogConfig.getClientConfig().fogLedger
        );

        blockClient.setAuthorization(
                fogConfig.getUsername(),
                fogConfig.getPassword()
        );

        TxOutStore store = new TxOutStore(recipientClient.getAccountKey());

        // add two small ranges to check
        Set<BlockRange> fakeFogMisses = new HashSet<>(Arrays.asList(new BlockRange(txoBlock,
                        txoBlock.add(UnsignedLong.ONE)),
                new BlockRange(status.getBlockIndex(),
                        status.getBlockIndex().add(UnsignedLong.TEN))));
        Set<OwnedTxOut> records = store.fetchFogMisses(
                fakeFogMisses,
                blockClient

        );
        blockClient.shutdown();

        // search for the specific amount sent earlier
        boolean found = false;
        for (OwnedTxOut txOut : records) {
            found = txOut.getValue().equals(amount);
            if (found) break;
        }
        if (!found) {
            Assert.fail("Unable to retrieve account TxOuts from the ledger");
        }
    }

    @Test
    public void testEmptyResponse() throws Exception {
        AttestedViewClient viewClient = mock(AttestedViewClient.class);
        View.QueryResponse.Builder responseBuilder = View.QueryResponse.newBuilder();
        when(viewClient.request(any(), eq(Long.valueOf(0L)), eq(Long.valueOf(0L)))).thenReturn(responseBuilder.build());

        TxOutStore uut = new TxOutStore(null);
        Set<BlockRange> results = uut.updateRNGsAndTxOuts(viewClient,
                new DefaultFogQueryScalingStrategy(), new DefaultFogSeedProvider(),
                new DefaultVersionedCryptoBox());
        assertTrue(results.isEmpty());
    }

    @Test
    public void testUpdateRNGs() throws Exception {

        //build AccountKey
        AccountKey accountKey = mock(AccountKey.class);
        //

        AttestedViewClient viewClient = mock(AttestedViewClient.class);

        //build Kex RNG pub key
        KexRng.KexRngPubkey.Builder kexRngPubkey = KexRng.KexRngPubkey.newBuilder();
        kexRngPubkey.setVersion(0);
        kexRngPubkey.setPubkey(ByteString.copyFrom(new byte[32]));
        //
        //build RNG record
        View.RngRecord.Builder rngRecord = View.RngRecord.newBuilder();
        rngRecord.setStartBlock(0L);
        rngRecord.setIngestInvocationId(0L);
        rngRecord.setPubkey(kexRngPubkey.build());//add Kex RNG pub key;
        //
        //build TxOutStoreResult
        View.TxOutSearchResult.Builder searchResult = View.TxOutSearchResult.newBuilder();
        searchResult.setResultCode(View.TxOutSearchResultCode.NotFound_VALUE);
        searchResult.setCiphertext(ByteString.copyFrom(new byte[32]));
        searchResult.setSearchKey(ByteString.copyFrom(new byte[32]));
        //
        //build query responses
        View.QueryResponse.Builder responseBuilder = View.QueryResponse.newBuilder();
        responseBuilder.addRngs(rngRecord.build());//add RNG record
        View.QueryResponse response1 = responseBuilder.build();
        responseBuilder.addTxOutSearchResults(searchResult.build());
        View.QueryResponse response2 = responseBuilder.build();
        //
        //build FogSeed
        FogSeed fogSeed = mock(FogSeed.class);
        when(fogSeed.isObsolete()).thenReturn(false);
        when(fogSeed.getNextN(anyLong())).thenReturn(new byte[1][32]);
        when(fogSeed.getOutput()).thenReturn(new byte[32]);
        //
        //build FogSeedProvider
        FogSeedProvider seedProvider = mock(FogSeedProvider.class);
        when(seedProvider.fogSeedFor(any(), any())).thenReturn(fogSeed);
        //
        //build VersionedCryptoBox
        VersionedCryptoBox cryptoBox = mock(VersionedCryptoBox.class);

        //chain different response scenarios from view client
        when(viewClient.request(any(), eq(Long.valueOf(0L)), eq(Long.valueOf(0L))))
                .thenReturn(response1)//seed is null
                .thenReturn(response2)//use new fog seed created in the first iteration of updateRNGsAndTxOuts
                .thenReturn(responseBuilder.setTxOutSearchResults(0, searchResult.setResultCode(
                        View.TxOutSearchResultCode.BadSearchKey_VALUE).build()).build())//Exception should be thrown in this case
                .thenReturn(responseBuilder.setTxOutSearchResults(0, searchResult.setResultCode(
                        View.TxOutSearchResultCode.InternalError_VALUE).build()).build());//Exception should be thrown in this case

        TxOutStore uut = new TxOutStore(accountKey);
        Set<BlockRange> results = uut.updateRNGsAndTxOuts(viewClient,
                new DefaultFogQueryScalingStrategy(), seedProvider, cryptoBox);
        assertTrue(results.isEmpty());

        boolean exceptionThrown = false;
        try {
            uut.updateRNGsAndTxOuts(viewClient,
                    new DefaultFogQueryScalingStrategy(), seedProvider, cryptoBox);
        } catch(InvalidFogResponse expected) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        exceptionThrown = false;
        try {
            uut.updateRNGsAndTxOuts(viewClient,
                    new DefaultFogQueryScalingStrategy(), seedProvider, cryptoBox);
        } catch(InvalidFogResponse expected) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

}
