// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

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

}
