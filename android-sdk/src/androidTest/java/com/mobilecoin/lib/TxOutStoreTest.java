// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Parcel;

import com.google.protobuf.ByteString;
import com.mobilecoin.lib.exceptions.BadBip39EntropyException;
import com.mobilecoin.lib.exceptions.FogSyncException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.network.uri.FogUri;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import consensus_common.ConsensusCommon;
import fog_common.FogCommon;
import fog_ledger.Ledger;
import fog_view.View;
import kex_rng.KexRng;

public class TxOutStoreTest {
    private final TestFogConfig fogConfig = Environment.getTestFogConfig();

    @Test
    public void test_serialize_roundtrip() throws Exception {
        AccountKey accountKey = TestKeysManager.getNextAccountKey();
        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder()
            .setAccountKey(accountKey).build();
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

        Map<TokenId, Balance> balances = new HashMap<TokenId, Balance>();
        for(OwnedTxOut otxo : utxos) {
            //TODO: on API level 24, we can use getOrDefault to simplify the logic here
            Balance balance = balances.get(otxo.getAmount().getTokenId());
            if(null == balance) {
                balance = new Balance(BigInteger.ZERO, store.getCurrentBlockIndex());
            }
            else {
                balance = new Balance(
                        balance.getValue().add(otxo.getAmount().getValue()),
                        store.getCurrentBlockIndex()
                );
            }
            balances.put(otxo.getAmount().getTokenId(), balance);
        }

        byte[] serialized = store.toByteArray();
        store = TxOutStore.fromBytes(serialized);
        Set<OwnedTxOut> restoredUtxos = store.getSyncedTxOuts();
        int restoredStoreSize = restoredUtxos.size();
        Assert.assertEquals("Serialized and Deserialized stores sizes must be the same",
                storeSize,
                restoredStoreSize
        );

        Map<TokenId, Balance> restoredBalances = new HashMap<TokenId, Balance>();
        for(OwnedTxOut otxo : restoredUtxos) {
            //TODO: on API level 24, we can use getOrDefault to simplify the logic here
            Balance balance = restoredBalances.get(otxo.getAmount().getTokenId());
            if(null == balance) {
                balance = new Balance(BigInteger.ZERO, store.getCurrentBlockIndex());
            }
            else {
                balance = new Balance(
                        balance.getValue().add(otxo.getAmount().getValue()),
                        store.getCurrentBlockIndex()
                );
            }
            restoredBalances.put(otxo.getAmount().getTokenId(), balance);
        }
        Assert.assertEquals("Balance must remain the same after serialization round trip",
                balances,
                restoredBalances
        );
    }

    @Test
    public void fetch_fog_misses_test() throws Exception {

        final MobileCoinClient senderClient = MobileCoinClientBuilder.newBuilder().build();
        final MobileCoinClient recipientClient = MobileCoinClientBuilder.newBuilder().build();

        // send a random amount
        final Amount amount = Amount.ofMOB(BigInteger.valueOf(Math.abs(new SecureRandom().nextInt() % 100) + 1));
        final Amount minimumFee = senderClient.estimateTotalFee(
                amount
        );
        final PendingTransaction pending = senderClient.prepareTransaction(
                recipientClient.getAccountKey().getPublicAddress(),
                amount,
                minimumFee,
                TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(senderClient.getAccountKey())
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

        final FogUri fogUri = new FogUri(fogConfig.getFogUri());
        final FogBlockClient blockClient = new FogBlockClient(
                RandomLoadBalancer.create(fogUri),
                fogConfig.getClientConfig().fogLedger,
                fogConfig.getTransportProtocol()
        );

        blockClient.setAuthorization(
                fogConfig.getUsername(),
                fogConfig.getPassword()
        );

        final TxOutStore store = new TxOutStore(recipientClient.getAccountKey());

        // add two small ranges to check
        final Set<BlockRange> fakeFogMisses = new HashSet<>(Arrays.asList(new BlockRange(txoBlock,
                        txoBlock.add(UnsignedLong.ONE)),
                new BlockRange(status.getBlockIndex(),
                        status.getBlockIndex().add(UnsignedLong.TEN))));
        final Set<OwnedTxOut> records = store.fetchFogMisses(
                fakeFogMisses,
                blockClient

        );
        blockClient.shutdown();

        // search for the specific amount sent earlier
        boolean found = false;
        for (OwnedTxOut txOut : records) {
            found = txOut.getAmount().equals(amount);
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
    public void testUpdateRNGsAndTxOuts() throws Exception {

        //build AccountKey
        AccountKey accountKey = mock(AccountKey.class);

        //build Kex RNG pub key
        KexRng.KexRngPubkey.Builder kexRngPubkey = KexRng.KexRngPubkey.newBuilder();
        kexRngPubkey.setVersion(0);
        kexRngPubkey.setPubkey(ByteString.copyFrom(new byte[32]));

        //build RNG record
        View.RngRecord.Builder rngRecord = View.RngRecord.newBuilder();
        rngRecord.setStartBlock(0L);
        rngRecord.setIngestInvocationId(0L);
        rngRecord.setPubkey(kexRngPubkey.build());//add Kex RNG pub key;

        //build TxOutStoreResult
        View.TxOutSearchResult.Builder searchResult = View.TxOutSearchResult.newBuilder();
        searchResult.setResultCode(View.TxOutSearchResultCode.NotFound_VALUE);
        searchResult.setCiphertext(ByteString.copyFrom(new byte[32]));
        searchResult.setSearchKey(ByteString.copyFrom(new byte[32]));

        //build query responses
        View.QueryResponse.Builder responseBuilder = View.QueryResponse.newBuilder();
        responseBuilder.addRngs(rngRecord.build());//add RNG record
        View.QueryResponse response1 = responseBuilder.build();
        responseBuilder.addTxOutSearchResults(searchResult.build());
        View.QueryResponse response2 = responseBuilder.build();

        //build FogSeed
        FogSeed fogSeed = mock(FogSeed.class);
        when(fogSeed.isObsolete()).thenReturn(false);
        when(fogSeed.getNextN(anyLong())).thenAnswer(invocation ->
                new byte[((Number)invocation.getArgument(0)).intValue()][32]);
        when(fogSeed.getOutput()).thenReturn(new byte[32]);

        //build FogSeedProvider
        FogSeedProvider seedProvider = mock(FogSeedProvider.class);
        when(seedProvider.fogSeedFor(any(), any())).thenReturn(fogSeed);

        //build VersionedCryptoBox
        VersionedCryptoBox cryptoBox = mock(VersionedCryptoBox.class);
        when(cryptoBox.versionedCryptoBoxDecrypt(any(), any())).thenReturn(SAMPLE_TXOUT_BYTES);

        //chain different response scenarios from view client
        AttestedViewClient viewClient = mock(AttestedViewClient.class);
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

        // Test all cases where we expect that an exception be thrown
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
        exceptionThrown = false;
        try {
            byte[] mismatchedSearchKey = new byte[32];
            mismatchedSearchKey[0] = 1;
            when(fogSeed.getOutput()).thenReturn(mismatchedSearchKey).thenReturn(new byte[32]);
            uut.updateRNGsAndTxOuts(viewClient,
                    new DefaultFogQueryScalingStrategy(), seedProvider, cryptoBox);
        } catch (InvalidFogResponse expected) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        //build block range
        FogCommon.BlockRange.Builder blockRange1 = FogCommon.BlockRange.newBuilder()
                .setStartBlock(1L).setEndBlock(2L);
        FogCommon.BlockRange.Builder blockRange2 = FogCommon.BlockRange.newBuilder()
                .setStartBlock(100L).setEndBlock(142L);
        FogCommon.BlockRange.Builder blockRange3 = FogCommon.BlockRange.newBuilder()
                .setStartBlock(4L).setEndBlock(10L);
        //Test missing block ranges returned
        when(viewClient.request(any(), eq(Long.valueOf(0L)), eq(Long.valueOf(0L))))
                .thenReturn(responseBuilder.setTxOutSearchResults(0,
                        searchResult.setResultCode(View.TxOutSearchResultCode.Found_VALUE).build())
                        .addMissedBlockRanges(blockRange1)
                        .addMissedBlockRanges(blockRange2).build())
                .thenReturn(responseBuilder.setTxOutSearchResults(0,
                        searchResult.setResultCode(View.TxOutSearchResultCode.NotFound_VALUE).build())
                        .addMissedBlockRanges(blockRange3).build());
        results = uut.updateRNGsAndTxOuts(viewClient,
                new DefaultFogQueryScalingStrategy(), seedProvider, cryptoBox);

        assertEquals(results.size(), responseBuilder.getMissedBlockRangesCount());

        Set<BlockRange> expectedRanges = new HashSet<BlockRange>();
        expectedRanges.add(new BlockRange(blockRange1.build()));
        expectedRanges.add(new BlockRange(blockRange2.build()));
        expectedRanges.add(new BlockRange(blockRange3.build()));

        assertTrue(Objects.equals(results, expectedRanges));

    }

    @Test
    public void testFogSyncDetection() throws Exception {

        boolean exceptionThrown = false;

        final long testValueFog1 = 61L;
        final long testValueConsensus1 = testValueFog1;
        fogSyncTest_attemptRefresh(testValueFog1, testValueFog1, testValueConsensus1);// same index should succeed

        final long testValueFog2 = 321L;
        final long testValueConsensus2 = testValueFog2 - 2L;
        fogSyncTest_attemptRefresh(testValueFog2, testValueFog2, testValueConsensus2);// Fog ahead of Consensus should succeed (occurs when cached Consensus block info is used)

        final long testValueFog3 = 5234523462456L;
        final long testValueConsensus3 = testValueFog3 + TxOutStore.FOG_SYNC_THRESHOLD.longValue() - 2L;
        fogSyncTest_attemptRefresh(testValueFog3, testValueFog3, testValueConsensus3);// Fog behind but within threshold should succeed

        final long testValueFog4 = 60L;
        final long testValueConsensus4 = testValueFog4 + TxOutStore.FOG_SYNC_THRESHOLD.longValue() - 1L;
        try {
            fogSyncTest_attemptRefresh(testValueFog4, testValueFog4, testValueConsensus4);// Fog behind at threshold, should fail
        } catch(FogSyncException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        exceptionThrown = false;

        final long testValueFog5 = 410L;
        final long testValueConsensus5 = testValueFog5 + TxOutStore.FOG_SYNC_THRESHOLD.longValue();
        try {
            fogSyncTest_attemptRefresh(testValueFog5, testValueFog5, testValueConsensus5);// Fog behind over threshold, should fail
        } catch (FogSyncException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        exceptionThrown = false;

        final long testValueView6 = 234234235675L;
        final long testValueLedger6 = testValueView6 - TxOutStore.FOG_SYNC_THRESHOLD.longValue() + 2L;
        final long testValueConsensus6 = testValueView6;
        fogSyncTest_attemptRefresh(testValueView6, testValueLedger6, testValueConsensus6);// below threshold, should pass

        final long testValueView7 = 99L;
        final long testValueLedger7 = testValueView7 + TxOutStore.FOG_SYNC_THRESHOLD.longValue() + 1L;
        final long testValueConsensus7 = testValueView7 + TxOutStore.FOG_SYNC_THRESHOLD.longValue() / 2;
        try {
            fogSyncTest_attemptRefresh(testValueView7, testValueLedger7, testValueConsensus7);// at threshold, should fail
        } catch(FogSyncException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        exceptionThrown = false;

        final long testValueView8 = 170L;
        final long testValueLedger8 = testValueView8 - TxOutStore.FOG_SYNC_THRESHOLD.longValue() - 1L;
        final long testValueConsensus8 = testValueView8 - TxOutStore.FOG_SYNC_THRESHOLD.longValue() / 2;
        try {
            fogSyncTest_attemptRefresh(testValueView8, testValueLedger8, testValueConsensus8);// at threshold, should fail
        } catch(FogSyncException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        exceptionThrown = false;

        final long testValueView9 = 99L;
        final long testValueLedger9 = testValueView9 + TxOutStore.FOG_SYNC_THRESHOLD.longValue();
        final long testValueConsensus9 = testValueView9 + TxOutStore.FOG_SYNC_THRESHOLD.longValue() / 2;
        try {
            fogSyncTest_attemptRefresh(testValueView9, testValueLedger9, testValueConsensus9);// above threshold, should fail
        } catch(FogSyncException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        exceptionThrown = false;

    }

    private void fogSyncTest_attemptRefresh(long fogViewBlocks, long fogLedgerBlocks, long consensusBlocks) throws Exception {

        AccountKey accountKey = mock(AccountKey.class);

        AttestedViewClient viewClient = mock(AttestedViewClient.class);
        View.QueryResponse.Builder viewResponseBuilder = View.QueryResponse.newBuilder();
        viewResponseBuilder.setHighestProcessedBlockCount(fogViewBlocks);
        when(viewClient.request(any(), eq(Long.valueOf(0L)), eq(Long.valueOf(0L)))).thenReturn(viewResponseBuilder.build());

        AttestedLedgerClient ledgerClient = mock(AttestedLedgerClient.class);
        Ledger.CheckKeyImagesResponse.Builder ledgerResponseBuilder = Ledger.CheckKeyImagesResponse.newBuilder();
        ledgerResponseBuilder.setNumBlocks(fogLedgerBlocks);
        ledgerResponseBuilder.setGlobalTxoCount(18930623637638213L);
        when(ledgerClient.checkUtxoKeyImages(any())).thenReturn(ledgerResponseBuilder.build());

        FogBlockClient blockClient = mock(FogBlockClient.class);
        when(blockClient.scanForTxOutsInBlockRange(any(), any())).thenReturn(new ArrayList<OwnedTxOut>());

        BlockchainClient blockchainClient = mock(BlockchainClient.class);
        ConsensusCommon.LastBlockInfoResponse.Builder blockchainClientResponseBuilder = ConsensusCommon.LastBlockInfoResponse.newBuilder();
        blockchainClientResponseBuilder.setIndex(consensusBlocks);
        when(blockchainClient.getOrFetchLastBlockInfo()).thenReturn(blockchainClientResponseBuilder.build());

        TxOutStore txOutStore = new TxOutStore(accountKey);
        txOutStore.setConsensusBlockIndex(UnsignedLong.fromLongBits(consensusBlocks));
        txOutStore.refresh(
                viewClient,
                ledgerClient,
                blockClient
        );

    }

    @Test
    public void testGetCurrentBlockIndex() {

        AccountKey accountKey = mock(AccountKey.class);
        TxOutStore txOutStore = new TxOutStore(accountKey);

        txOutStore.setLedgerBlockIndex(UnsignedLong.ZERO);
        txOutStore.setViewBlockIndex(UnsignedLong.ZERO);
        assertEquals(txOutStore.getCurrentBlockIndex(), UnsignedLong.ZERO);

        txOutStore.setLedgerBlockIndex(UnsignedLong.TEN);
        assertEquals(txOutStore.getCurrentBlockIndex(), UnsignedLong.ZERO);

        txOutStore.setViewBlockIndex(UnsignedLong.TEN);
        assertEquals(txOutStore.getCurrentBlockIndex(), UnsignedLong.TEN);

        txOutStore.setViewBlockIndex(UnsignedLong.TEN.add(UnsignedLong.TEN));
        assertEquals(txOutStore.getCurrentBlockIndex(), UnsignedLong.TEN);

    }

    @Test
    public void testParcelable() throws BadBip39EntropyException {
        AccountTest.AccountTestData accountData = AccountTest.loadAccountTestData().get(0);
        AccountKey accountWithoutFog = AccountKeyDeriver.deriveAccountKeyFromMnemonic(
                accountData.mnemonic, accountData.accountIndex);
        TxOutStore parcelInput = new TxOutStore(accountWithoutFog);
        Parcel parcel = Parcel.obtain();
        parcelInput.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        TxOutStore parcelOutput = TxOutStore.CREATOR.createFromParcel(parcel);
        parcelOutput.setAccountKey(accountWithoutFog);
        assertEquals(parcelInput, parcelOutput);
    }

    private static final byte[] SAMPLE_TXOUT_BYTES = new byte[] {17, -93, 2, -81, 7, -62,
            104, -128, -95, 26, 32, -94, -11, 86, 42, 90, -43, 32, 5, 21, 72, -110, -74, 68, -108, 87, 37,
            57, -50, 90, 45, -3, -43, 96, -3, 21, -40, 27, -88, -34, -60, 124, 31, 34, 32, 116, -23, -29,
            103, 10, -31, -32, 21, -95, 68, -10, 95, 125, 65, -124, -106, -80, -93, -52, -121, -89, -5,
            40, 95, 111, -109, 22, 40, 72, 44, -47, 80, 41, 26, -113, 1, 0, 0, 0, 0, 0, 49, 28, 0, 0,
            0, 0, 0, 0, 0, 57, 89, 102, 24, 97, 0, 0, 0, 0, 69, 68, 109, -49, -85};

}
