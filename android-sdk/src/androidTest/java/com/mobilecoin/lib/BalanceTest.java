// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class BalanceTest {
    private final static UnsignedLong TEST_BLOCK_INDEX = UnsignedLong.valueOf(100);
    private final static BigInteger TEST_AMOUNT = BigInteger.ONE;

    @Test
    public void balance_block_index_test() {
        Balance balance = new Balance(
                TEST_AMOUNT,
                TEST_BLOCK_INDEX
        );
        Assert.assertEquals(
                balance.getBlockIndex(),
                TEST_BLOCK_INDEX
        );
        Assert.assertEquals(
                balance.getAmountPicoMob(),
                TEST_AMOUNT
        );
    }

    static final BigInteger U64_MAX = UnsignedLong.MAX_VALUE.toBigInteger();
    static final BigInteger LARGE_AMOUNT = U64_MAX.add(BigInteger.TEN).add(BigInteger.ONE);
    static final BigInteger LARGE_BALANCE = LARGE_AMOUNT.multiply(BigInteger.valueOf(2L));

    @Test
    public void testLargeBalance() throws Exception {
        MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        client = new MobileCoinClient(
                client.getAccountKey(),
                createMockTxOutStore(),
                client.clientConfig,
                client.cacheStorage,
                client.fogReportsManager,
                client.fogBlockClient,
                client.untrustedClient,
                client.viewClient,
                client.ledgerClient,
                client.consensusClient,
                client.blockchainClient
        );
        assertEquals(LARGE_BALANCE, client.getBalance(TokenId.MOB).getValue());
        final BigInteger fee = client.estimateTotalFee(Amount.ofMOB(LARGE_AMOUNT)).getValue();
        assertEquals(LARGE_BALANCE.subtract(fee), client.getTransferableAmount(TokenId.MOB).getValue());
    }

    private static TxOutStore createMockTxOutStore() throws Exception {
        final TxOutStore txOutStore = mock(TxOutStore.class);
        final Set<OwnedTxOut> otxos = createMockOwnedTxOuts();
        when(txOutStore.getCurrentBlockIndex()).thenReturn(UnsignedLong.MAX_VALUE);
        when(txOutStore.getSyncedTxOuts()).thenReturn(otxos);
        when(txOutStore.getUnspentTxOuts()).thenReturn(otxos);
        return txOutStore;
    }

    private static Set<OwnedTxOut> createMockOwnedTxOuts() throws Exception {
        final Set<OwnedTxOut> txos = new HashSet<>();
        txos.add(createMockOwnedTxOut(Amount.ofMOB(U64_MAX)));
        txos.add(createMockOwnedTxOut(Amount.ofMOB(U64_MAX)));
        txos.add(createMockOwnedTxOut(Amount.ofMOB((BigInteger.TEN))));
        txos.add(createMockOwnedTxOut(Amount.ofMOB((BigInteger.TEN))));
        txos.add(createMockOwnedTxOut(Amount.ofMOB((BigInteger.ONE))));
        txos.add(createMockOwnedTxOut(Amount.ofMOB((BigInteger.ONE))));
        return txos;
    }

    private static OwnedTxOut createMockOwnedTxOut(final Amount amount) throws Exception {
        final OwnedTxOut otxo = mock(OwnedTxOut.class);
        when(otxo.getAmount()).thenReturn(amount);
        when(otxo.getReceivedBlockIndex()).thenReturn(UnsignedLong.ZERO);
        when(otxo.isSpent(any())).thenReturn(false);
        when(otxo.copy()).thenReturn(otxo);
        return otxo;
    }

}
