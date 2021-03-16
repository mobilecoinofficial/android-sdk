// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import org.junit.Assert;
import org.junit.Test;

public class ClientKexRngTest {

    private final byte[] rng1 = {
            (byte)0x84, (byte)0xF2, (byte)0xF5, (byte)0xE3,
            (byte)0x24, (byte)0xC9, (byte)0x3, (byte)0x1E,
            (byte)0x1F, (byte)0xB9, (byte)0x2E, (byte)0x60,
            (byte)0x38, (byte)0xE7, (byte)0xA2, (byte)0x24
    };

    private final byte[] rng2 = {
            (byte)0x72, (byte)0x36, (byte)0x12, (byte)0x70,
            (byte)0x74, (byte)0xFE, (byte)0x2F, (byte)0x89,
            (byte)0xB2, (byte)0x41, (byte)0x3D, (byte)0xC9,
            (byte)0x1E, (byte)0xB4, (byte)0xBB, (byte)0x26
    };

    @Test
    public void test_rng_outputs() {
        try {
            byte[] keyBytes = {
                    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1
            };
            RistrettoPrivate sec = RistrettoPrivate.fromBytes(keyBytes);
            byte[] nonce = {
                    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
                    2, 2, 2, 2, 2
            };
            ClientKexRng rng = new ClientKexRng(sec,
                                                nonce,
                                                0
            );
            byte[] out = rng.getOutput();
            Assert.assertArrayEquals(
                    out,
                    rng1
            );
            rng.advance();
            out = rng.getOutput();
            Assert.assertArrayEquals(
                    out,
                    rng2
            );
            rng.advance();
        } catch (Exception e) {
            Assert.fail("RNG outputs are not equal! e: " + e.getLocalizedMessage());
        }
    }
}
