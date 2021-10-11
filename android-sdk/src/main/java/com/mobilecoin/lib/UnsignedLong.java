// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.util.Objects;

public final class UnsignedLong extends Number implements Comparable<UnsignedLong>, Parcelable {

    private static final long UNSIGNED_MASK = 0x7fffffffffffffffL;
    private static final long MIN_LONG_VALUE = 0x8000000000000000L;
    private static final long MAX_LONG_VALUE = 0x7fffffffffffffffL;

    public static final UnsignedLong ZERO = new UnsignedLong(0);
    public static final UnsignedLong ONE = new UnsignedLong(1);
    public static final UnsignedLong TEN = new UnsignedLong(10);
    public static final UnsignedLong MAX_VALUE = new UnsignedLong(-1L);

    private final long value;

    private UnsignedLong(long value) {
        this.value = value;
    }

    /**
     * Returns an {@code UnsignedLong} corresponding to a given bit representation. The argument is
     * interpreted as an unsigned 64-bit value. Specifically, the sign bit of {@code bits} is
     * interpreted as a normal bit, and all other bits are treated as usual.
     */
    @NonNull
    public static UnsignedLong fromLongBits(long bits) {
        return new UnsignedLong(bits);
    }

    /**
     * Returns an {@code UnsignedLong} corresponding to a given BigInteger's value.
     * The argument is interpreted as an unsigned 64-bit value. Specifically, the sign bit of
     * {@code bits} is interpreted as a normal bit, and all other bits are treated as usual.
     */
    @NonNull
    public static UnsignedLong fromBigInteger(@NonNull BigInteger value) {
        return new UnsignedLong(value.longValue());
    }

    /**
     * @param value a non-negative long value to represent
     * @return {@link UnsignedLong} representing the argument value
     * @throws IllegalArgumentException if the value is negative
     */
    @NonNull
    public static UnsignedLong valueOf(long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("The value is outside of the long range");
        }
        return fromLongBits(value);
    }

    /**
     * @param value a non-negative BigInteger value to represent
     * @return {@link UnsignedLong} representing the argument value
     * @throws IllegalArgumentException if the value is negative or does not fit into unsigned long
     */
    @NonNull
    public static UnsignedLong valueOf(@NonNull BigInteger value) {
        if (value.signum() < 0 || value.bitLength() > Long.SIZE) {
            throw new IllegalArgumentException("The value is outside of the unsigned long range");
        }
        return fromBigInteger(value);
    }

    /**
     * Compares two {@code long} values numerically treating the values
     * as unsigned.
     */
    @Override
    public int compareTo(UnsignedLong o) {
        return Long.compare(value + MIN_LONG_VALUE, o.value + MIN_LONG_VALUE);
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        float fValue = (float) (value & UNSIGNED_MASK);
        if (value < 0) {
            fValue += 0x1.0p63f;
        }
        return fValue;
    }

    @Override
    public double doubleValue() {
        double dValue = (double) (value & UNSIGNED_MASK);
        if (value < 0) {
            dValue += 0x1.0p63;
        }
        return dValue;
    }

    /**
     * Return a BigInteger equal to the unsigned value of the
     * argument.
     */
    @NonNull
    public BigInteger toBigInteger() {
        if (value >= 0L) return BigInteger.valueOf(value);
        else {
            BigInteger bigInt = BigInteger.valueOf(value & UNSIGNED_MASK);
            bigInt = bigInt.setBit(Long.SIZE - 1);
            return bigInt;
        }
    }

    @NonNull
    @Override
    public String toString() {
        if (value > 0) {
            return Long.toString(value);
        } else {
            return toBigInteger().toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnsignedLong that = (UnsignedLong) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Returns the result of adding this and {@code addend}. If the result would have more than 64
     * bits, returns the low 64 bits of the result.
     */
    @NonNull
    public UnsignedLong add(@NonNull UnsignedLong addend) {
        return fromLongBits(this.value + addend.value);
    }

    /**
     * Returns the result of subtracting this and {@code subtrahend}.
     */
    @NonNull
    public UnsignedLong sub(@NonNull UnsignedLong subtrahend) {
        return fromLongBits(this.value - subtrahend.value);
    }

    /**
     * Returns the result of multiplying this and {@code multiplier}. If the result would have more
     * than 64 bits, returns the low 64 bits of the result.
     */
    @NonNull
    public UnsignedLong mul(@NonNull UnsignedLong multiplier) {
        return fromLongBits(value * multiplier.value);
    }

    /**
     * Returns value / divisor, where the dividend and divisor are treated as unsigned 64-bit longs.
     *
     * @param divisor the divisor (denominator)
     * @throws ArithmeticException if divisor is 0
     */
    public long divideBy(long divisor) {
        long dividend = value;
        if (divisor < 0) { // i.e., divisor >= 2^63:
            if (compareTo(UnsignedLong.fromLongBits(divisor)) < 0) {
                return 0; // dividend < divisor
            } else {
                return 1; // dividend >= divisor
            }
        }

        // signed division if dividend < 2^63
        if (dividend >= 0) {
            return dividend / divisor;
        }

        // use BigInteger for simplicity
        // use UnsignedLong as an intermediate to not lose the sign
        // todo: see "Hacker's Delight" for optimal divide and remainder algos
        BigInteger bigDividend = UnsignedLong.fromLongBits(dividend).toBigInteger();
        BigInteger bigDivisor = UnsignedLong.fromLongBits(divisor).toBigInteger();
        return bigDividend.divide(bigDivisor).longValue();
    }

    /**
     * Returns value / divisor, where the dividend and divisor are treated as unsigned 64-bit longs.
     *
     * @param divisor the divisor (denominator)
     * @throws ArithmeticException if divisor is 0
     */
    @NonNull
    public UnsignedLong divideBy(@NonNull UnsignedLong divisor) {
        return UnsignedLong.fromLongBits(divideBy(divisor.value));
    }

    /**
     * Returns value % divisor, where the dividend and divisor are treated as unsigned 64-bit longs.
     *
     * @param divisor the divisor (denominator)
     * @throws ArithmeticException if divisor is 0
     */
    public long remainder(long divisor) {
        if (divisor < 0) { // i.e., divisor >= 2^63:
            if (compareTo(UnsignedLong.fromLongBits(divisor)) < 0) {
                return value; // dividend < divisor
            } else {
                return value - divisor; // dividend >= divisor
            }
        }

        // use signed modulus if dividend < 2^63
        if (value >= 0) {
            return value % divisor;
        }
        // use BigInteger for simplicity
        // use UnsignedLong as an intermediate to not lose the sign
        // todo: see "Hacker's Delight" for optimal divide and remainder algos
        BigInteger bigDivisor = UnsignedLong.fromLongBits(divisor).toBigInteger();
        BigInteger bigResult = toBigInteger().remainder(bigDivisor);
        return bigResult.longValue();
    }

    /**
     * Returns value % divisor, where the dividend and divisor are treated as unsigned 64-bit
     * longs.
     *
     * @param divisor the divisor (denominator)
     * @throws ArithmeticException if divisor is 0
     */
    @NonNull
    public UnsignedLong remainder(@NonNull UnsignedLong divisor) {
        return UnsignedLong.fromLongBits(remainder(divisor.value));
    }

    public static final Creator<UnsignedLong> CREATOR = new Creator<UnsignedLong>() {
        public UnsignedLong createFromParcel(Parcel parcel) {
            return new UnsignedLong(parcel);
        }

        @Override
        public UnsignedLong[] newArray(int length) {
            return new UnsignedLong[length];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(value);
    }

    private UnsignedLong(Parcel parcel) {
        value = parcel.readLong();
    }

}
