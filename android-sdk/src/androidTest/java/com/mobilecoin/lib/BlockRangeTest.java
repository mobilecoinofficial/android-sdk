package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BlockRangeTest {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRange() {
        BlockRange blockRange = new BlockRange(1, 0);
    }

    @Test
    public void rangeSizeTest() {
        UnsignedLong rangeStart = UnsignedLong.valueOf(1L);
        UnsignedLong rangeEnd = UnsignedLong.valueOf(10L);
        BlockRange blockRange = new BlockRange(rangeStart, rangeEnd);
        assertEquals(rangeEnd.sub(rangeStart), blockRange.size());
    }

}
