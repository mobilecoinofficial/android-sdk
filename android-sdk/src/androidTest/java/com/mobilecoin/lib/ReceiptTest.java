// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import org.junit.Assert;
import org.junit.Test;

public class ReceiptTest {
    private final static UnsignedLong TEST_BLOCK_INDEX = UnsignedLong.valueOf(1000);

    @Test
    public void receipt_status_options_test() {
        for (Receipt.Status status : Receipt.Status.values()) {
            status.atBlock(TEST_BLOCK_INDEX);
            Assert.assertEquals(
                    status.getBlockIndex(),
                    TEST_BLOCK_INDEX
            );
        }
    }

    @Test
    public void comparison_test() {
        Receipt.Status status = Receipt.Status.RECEIVED.atBlock(TEST_BLOCK_INDEX);
        Assert.assertEquals(
                status,
                Receipt.Status.RECEIVED
        );
    }
}
