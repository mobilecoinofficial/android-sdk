// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.util.Objects;

/**
 * The {@code Balance} class represents the account balance
 */
final public class Balance {
    private final static String TAG = Balance.class.getName();
    private final BigInteger value;
    private final UnsignedLong atBlock;

    public Balance(
            @NonNull BigInteger value,
            @NonNull UnsignedLong atBlock
    ) {
        this.value = value;
        this.atBlock = atBlock;
    }

    /**
     * Returns the balance amount in pico MOBs
     */
    @NonNull
    @Deprecated
    public BigInteger getAmountPicoMob() {
        return value;
    }

    /**
     * Returns the value of this balance
     */
    @NonNull
    public BigInteger getValue() {
        return this.value;
    }

    /**
     * Returns the block index at which this balance was current
     */
    @NonNull
    public UnsignedLong getBlockIndex() {
        return atBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Balance balance = (Balance) o;
        return value.equals(balance.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.value,
                this.atBlock
        );
    }

    @NonNull
    @Override
    public String toString() {
        return value.toString();
    }
}
