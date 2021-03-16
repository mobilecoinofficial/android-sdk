// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import org.junit.Assert;
import org.junit.Test;

public class TransactionTest {
    private final static UnsignedLong TEST_BLOCK_INDEX = UnsignedLong.valueOf(1000);

    @Test
    public void tx_status_options_test() {
        for (Transaction.Status status : Transaction.Status.values()) {
            status.atBlock(TEST_BLOCK_INDEX);
            Assert.assertEquals(
                    status.getBlockIndex(),
                    TEST_BLOCK_INDEX
            );
        }
    }
    @Test
    public void comparison_test() {
        Transaction.Status status = Transaction.Status.ACCEPTED.atBlock(TEST_BLOCK_INDEX);
        Assert.assertEquals(
                status,
                Transaction.Status.ACCEPTED
        );
    }
}
