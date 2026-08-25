// Copyright (c) 2020-2026 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.protobuf.ByteString;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.network.services.FogKeyImageService;
import com.mobilecoin.lib.network.services.ServiceAPIManager;
import com.mobilecoin.lib.network.services.transport.Transport;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.HashSet;

import attest.Attest;
import fog_ledger.Ledger;

/**
 * Covers the per-txo start_block watermark reaching the wire in each key-image query.
 */
@RunWith(RobolectricTestRunner.class)
public class AttestedLedgerClientWatermarkTest {

    @Test
    public void checkUtxoKeyImages_scopesEachQueryToThatTxOutsWatermark() throws Exception {
        // Real watermark accessors, so an accessor that stopped storing the value fails here.
        byte[] watermarkedKeyImage = {1};
        OwnedTxOut watermarked = mock(OwnedTxOut.class, CALLS_REAL_METHODS);
        doReturn(KeyImage.fromBytes(watermarkedKeyImage)).when(watermarked).getKeyImage();
        watermarked.setKnownToBeUnspentBlockCount(UnsignedLong.fromLongBits(50));

        byte[] neverCheckedKeyImage = {2};
        OwnedTxOut neverChecked = mock(OwnedTxOut.class, CALLS_REAL_METHODS);
        doReturn(KeyImage.fromBytes(neverCheckedKeyImage)).when(neverChecked).getKeyImage();

        FogKeyImageService keyImageService = mock(FogKeyImageService.class);
        when(keyImageService.checkKeyImages(any())).thenReturn(Attest.Message.getDefaultInstance());
        ServiceAPIManager apiManager = mock(ServiceAPIManager.class);
        when(apiManager.getFogKeyImageService(any())).thenReturn(keyImageService);

        AttestedLedgerClient client = mock(AttestedLedgerClient.class, CALLS_REAL_METHODS);
        ArgumentCaptor<Ledger.CheckKeyImagesRequest> sentRequest =
                ArgumentCaptor.forClass(Ledger.CheckKeyImagesRequest.class);
        doReturn(apiManager).when(client).getAPIManager();
        doReturn(mock(Transport.class)).when(client).getNetworkTransport();
        doReturn(Attest.Message.getDefaultInstance())
                .when(client).encryptMessage(sentRequest.capture());
        doReturn(Attest.Message.newBuilder()
                .setData(Ledger.CheckKeyImagesResponse.getDefaultInstance().toByteString())
                .build()).when(client).decryptMessage(any());

        client.checkUtxoKeyImages(new HashSet<>(Arrays.asList(watermarked, neverChecked)));

        Ledger.CheckKeyImagesRequest request = sentRequest.getValue();
        assertEquals(2, request.getQueriesCount());
        assertEquals(50L, startBlockFor(request, watermarkedKeyImage));
        assertEquals(0L, startBlockFor(request, neverCheckedKeyImage));
    }

    private static long startBlockFor(Ledger.CheckKeyImagesRequest request, byte[] keyImage) {
        MobileCoinAPI.KeyImage queried = MobileCoinAPI.KeyImage.newBuilder()
                .setData(ByteString.copyFrom(keyImage)).build();
        for (Ledger.KeyImageQuery query : request.getQueriesList()) {
            if (query.getKeyImage().equals(queried)) {
                return query.getStartBlock();
            }
        }
        throw new AssertionError("no query carried the given key image");
    }
}
