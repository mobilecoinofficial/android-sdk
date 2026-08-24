// Copyright (c) 2020-2026 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.protobuf.ByteString;
import com.mobilecoin.api.MobileCoinAPI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import fog_ledger.Ledger;

/**
 * Covers the per-txo start_block watermark set from key-image results.
 */
@RunWith(RobolectricTestRunner.class)
public class TxOutStoreWatermarkTest {

    @Test
    public void updateTxOutsSpentState_watermarksOnlyTxOutsConfirmedUnspent() throws Exception {
        TxOutStore store = new TxOutStore(null);

        byte[] spentKeyImageBytes = {1};
        OwnedTxOut spentTxOut = mock(OwnedTxOut.class);
        when(spentTxOut.getKeyImageHashCode()).thenReturn(Arrays.hashCode(spentKeyImageBytes));
        when(spentTxOut.getKeyImage()).thenReturn(KeyImage.fromBytes(spentKeyImageBytes));
        when(spentTxOut.getSpentBlockIndex()).thenReturn(UnsignedLong.ONE);

        byte[] unspentKeyImageBytes = {2};
        OwnedTxOut unspentTxOut = mock(OwnedTxOut.class);
        when(unspentTxOut.getKeyImage()).thenReturn(KeyImage.fromBytes(unspentKeyImageBytes));

        // getUtxoByKeyImage resolves a Spent result against the store's own synced set, not the
        // queried set, so the spent txo needs to be reachable that way too.
        Field recoveredTxOutsField = TxOutStore.class.getDeclaredField("recoveredTxOuts");
        recoveredTxOutsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentLinkedQueue<OwnedTxOut> recoveredTxOuts =
                (ConcurrentLinkedQueue<OwnedTxOut>) recoveredTxOutsField.get(store);
        recoveredTxOuts.add(spentTxOut);

        Ledger.CheckKeyImagesResponse response = Ledger.CheckKeyImagesResponse.newBuilder()
                .setNumBlocks(50)
                .setGlobalTxoCount(100)
                .addResults(Ledger.KeyImageResult.newBuilder()
                        .setKeyImage(MobileCoinAPI.KeyImage.newBuilder()
                                .setData(ByteString.copyFrom(spentKeyImageBytes)).build())
                        .setKeyImageResultCode(Ledger.KeyImageResultCode.Spent_VALUE)
                        .setSpentAt(10)
                        .setTimestamp(UnsignedLong.MAX_VALUE.longValue())
                        .build())
                .build();

        Set<OwnedTxOut> queried = new HashSet<>(Arrays.asList(spentTxOut, unspentTxOut));
        store.updateTxOutsSpentState(queried, response);

        verify(spentTxOut, never()).setKnownToBeUnspentBlockCount(any());
        verify(unspentTxOut).setKnownToBeUnspentBlockCount(UnsignedLong.fromLongBits(50));
    }

    @Test
    public void updateTxOutsSpentState_watermarksAnExplicitNotSpentResult() throws Exception {
        TxOutStore store = new TxOutStore(null);

        byte[] notSpentKeyImageBytes = {3};
        OwnedTxOut notSpentTxOut = mock(OwnedTxOut.class);
        when(notSpentTxOut.getKeyImage()).thenReturn(KeyImage.fromBytes(notSpentKeyImageBytes));

        Ledger.CheckKeyImagesResponse response = Ledger.CheckKeyImagesResponse.newBuilder()
                .setNumBlocks(75)
                .setGlobalTxoCount(200)
                .addResults(Ledger.KeyImageResult.newBuilder()
                        .setKeyImage(MobileCoinAPI.KeyImage.newBuilder()
                                .setData(ByteString.copyFrom(notSpentKeyImageBytes)).build())
                        .setKeyImageResultCode(Ledger.KeyImageResultCode.NotSpent_VALUE)
                        .build())
                .build();

        store.updateTxOutsSpentState(new HashSet<>(Arrays.asList(notSpentTxOut)), response);

        verify(notSpentTxOut).setKnownToBeUnspentBlockCount(UnsignedLong.fromLongBits(75));
    }
}
