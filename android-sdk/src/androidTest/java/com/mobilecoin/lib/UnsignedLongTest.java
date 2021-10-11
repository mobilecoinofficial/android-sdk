// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.ImmutableSet;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class UnsignedLongTest {
    private static final ImmutableSet<Long> TEST_LONGS;
    private static final ImmutableSet<BigInteger> TEST_BIG_INTEGERS;

    static {
        ImmutableSet.Builder<Long> testLongsBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<BigInteger> testBigIntegersBuilder = ImmutableSet.builder();
        for (long i = -3; i <= 3; i++) {
            testLongsBuilder
                    .add(i)
                    .add(Long.MAX_VALUE + i)
                    .add(Long.MIN_VALUE + i)
                    .add(Integer.MIN_VALUE + i)
                    .add(Integer.MAX_VALUE + i);
            BigInteger bigI = BigInteger.valueOf(i);
            testBigIntegersBuilder
                    .add(bigI)
                    .add(BigInteger.valueOf(Long.MAX_VALUE).add(bigI))
                    .add(BigInteger.valueOf(Long.MIN_VALUE).add(bigI))
                    .add(BigInteger.valueOf(Integer.MAX_VALUE).add(bigI))
                    .add(BigInteger.valueOf(Integer.MIN_VALUE).add(bigI))
                    .add(BigInteger.ONE.shiftLeft(63).add(bigI))
                    .add(BigInteger.ONE.shiftLeft(64).add(bigI));
        }
        TEST_LONGS = testLongsBuilder.build();
        TEST_BIG_INTEGERS = testBigIntegersBuilder.build();
    }

    @Test
    public void test_big_integers() {
        for (long value : TEST_LONGS) {
            BigInteger expected =
                    (value >= 0)
                            ? BigInteger.valueOf(value)
                            : BigInteger.valueOf(value).add(BigInteger.ZERO.setBit(64));
            assertEquals(
                    UnsignedLong.fromLongBits(value).toString(),
                    expected,
                    UnsignedLong.fromLongBits(value).toBigInteger());
        }
    }

    @Test
    public void test_big_integer_values() {
        BigInteger min = BigInteger.ZERO;
        BigInteger max = UnsignedLong.MAX_VALUE.toBigInteger();
        for (BigInteger big : TEST_BIG_INTEGERS) {
            boolean expectSuccess = big.compareTo(min) >= 0 && big.compareTo(max) <= 0;
            try {
                assertEquals(big, UnsignedLong.valueOf(big).toBigInteger());
                Assert.assertTrue(expectSuccess);
            } catch (IllegalArgumentException e) {
                Assert.assertFalse(expectSuccess);
            }
        }
    }

    @Test
    public void test_float_value() {
        for (long value : TEST_LONGS) {
            UnsignedLong unsignedValue = UnsignedLong.fromLongBits(value);
            assertEquals(unsignedValue.toBigInteger().floatValue(),
                    unsignedValue.floatValue(), 0F);
        }
    }

    @Test
    public void test_double_value() {
        for (long value : TEST_LONGS) {
            UnsignedLong unsignedValue = UnsignedLong.fromLongBits(value);
            assertEquals(unsignedValue.toBigInteger().doubleValue(),
                    unsignedValue.doubleValue(), 0D);
        }
    }

    @Test
    public void test_add() {
        for (long a : TEST_LONGS) {
            for (long b : TEST_LONGS) {
                UnsignedLong unsignedA = UnsignedLong.fromLongBits(a);
                UnsignedLong unsignedB = UnsignedLong.fromLongBits(b);
                long expected = unsignedA.toBigInteger().add(unsignedB.toBigInteger()).longValue();
                UnsignedLong unsignedSum = unsignedA.add(unsignedB);
                assertEquals(expected, unsignedSum.longValue());
            }
        }
    }

    @Test
    public void test_sub() {
        for (long a : TEST_LONGS) {
            for (long b : TEST_LONGS) {
                UnsignedLong unsignedA = UnsignedLong.fromLongBits(a);
                UnsignedLong unsignedB = UnsignedLong.fromLongBits(b);
                long expected =
                        unsignedA.toBigInteger().subtract(unsignedB.toBigInteger()).longValue();
                UnsignedLong unsignedSub = unsignedA.sub(unsignedB);
                assertEquals(expected, unsignedSub.longValue());
            }
        }
    }

    @Test
    public void test_mul() {
        for (long a : TEST_LONGS) {
            for (long b : TEST_LONGS) {
                UnsignedLong unsignedA = UnsignedLong.fromLongBits(a);
                UnsignedLong unsignedB = UnsignedLong.fromLongBits(b);
                long expected =
                        unsignedA.toBigInteger().multiply(unsignedB.toBigInteger()).longValue();
                UnsignedLong unsignedMul = unsignedA.mul(unsignedB);
                assertEquals(expected, unsignedMul.longValue());
            }
        }
    }

    @Test
    public void test_divided_by() {
        for (long a : TEST_LONGS) {
            for (long b : TEST_LONGS) {
                if (b != 0) {
                    UnsignedLong unsignedA = UnsignedLong.fromLongBits(a);
                    UnsignedLong unsignedB = UnsignedLong.fromLongBits(b);
                    long expected =
                            unsignedA.toBigInteger().divide(unsignedB.toBigInteger()).longValue();
                    UnsignedLong unsignedDiv = unsignedA.divideBy(unsignedB);
                    assertEquals(expected, unsignedDiv.longValue());
                }
            }
        }
    }

    @Test
    public void test_divide_by_zero() {
        for (long a : TEST_LONGS) {
            try {
                UnsignedLong.fromLongBits(a).divideBy(UnsignedLong.ZERO);
                Assert.fail("Expected ArithmeticException");
            } catch (ArithmeticException expected) {
            }
        }
    }

    @Test
    public void test_remainder() {
        for (long a : TEST_LONGS) {
            for (long b : TEST_LONGS) {
                if (b != 0) {
                    UnsignedLong unsignedA = UnsignedLong.fromLongBits(a);
                    UnsignedLong unsignedB = UnsignedLong.fromLongBits(b);
                    long expected =
                            unsignedA.toBigInteger().remainder(unsignedB.toBigInteger()).longValue();
                    UnsignedLong unsignedRem = unsignedA.remainder(unsignedB);
                    assertEquals(expected, unsignedRem.longValue());
                }
            }
        }
    }

    @Test
    public void test_remainder_zero() {
        for (long a : TEST_LONGS) {
            try {
                UnsignedLong.fromLongBits(a).remainder(UnsignedLong.ZERO);
                Assert.fail("Expected ArithmeticException");
            } catch (ArithmeticException expected) {
            }
        }
    }

    @Test
    public void test_compare() {
        for (long a : TEST_LONGS) {
            for (long b : TEST_LONGS) {
                UnsignedLong unsignedA = UnsignedLong.fromLongBits(a);
                UnsignedLong unsignedB = UnsignedLong.fromLongBits(b);
                assertEquals(
                        unsignedA.toBigInteger().compareTo(unsignedB.toBigInteger()),
                        unsignedA.compareTo(unsignedB));
            }
        }
    }

    @Test
    public void test_equals() {
        for (long a : TEST_LONGS) {
            BigInteger big = (a >= 0)
                    ? BigInteger.valueOf(a)
                    : BigInteger.valueOf(a).add(BigInteger.ZERO.setBit(64));

            assertEquals(UnsignedLong.fromLongBits(a), UnsignedLong.fromBigInteger(big));
        }
    }

    @Test
    public void test_to_string() {
        for (long value : TEST_LONGS) {
            UnsignedLong unsignedValue = UnsignedLong.fromLongBits(value);
            assertEquals(unsignedValue.toBigInteger().toString(),
                    unsignedValue.toString());
        }
    }

    @Test
    public void test_parcelable() {
        for(long value : TEST_LONGS) {
            UnsignedLong uutInput = UnsignedLong.fromLongBits(value);
            Parcel parcel = Parcel.obtain();
            uutInput.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            UnsignedLong uutOutput = UnsignedLong.CREATOR.createFromParcel(parcel);
            assertEquals(uutInput, uutOutput);
        }
    }

}
