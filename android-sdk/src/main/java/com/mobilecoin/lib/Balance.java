// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import java.math.BigInteger;

/**
 * The {@code Balance} class represents the account balance
 */
public class Balance {
    private final static String TAG = Balance.class.getName();
    private final BigInteger amountPicoMob;
    private final UnsignedLong atBlock;

    Balance(
            @NonNull BigInteger amountPicoMob,
            @NonNull UnsignedLong atBlock
    ) {
        this.amountPicoMob = amountPicoMob;
        this.atBlock = atBlock;
    }

    /**
     * Returns the balance amount in pico MOBs
     */
    @NonNull
    public BigInteger getAmountPicoMob() {
        return amountPicoMob;
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
        return amountPicoMob.equals(balance.amountPicoMob);
    }

    @Override
    public int hashCode() {
        return 31 * amountPicoMob.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return amountPicoMob.toString();
    }
}
