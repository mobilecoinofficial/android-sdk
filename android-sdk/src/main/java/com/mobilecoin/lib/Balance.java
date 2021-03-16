// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.log.Logger;

import java.math.BigInteger;

/**
 * Represents account balance in picoMob for a specific block height
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

    @NonNull
    public BigInteger getAmountPicoMob() {
        Logger.i(TAG, "Getting balance amount");
        return amountPicoMob;
    }

    @NonNull
    public UnsignedLong getBlockIndex() {
        Logger.i(TAG, "Getting block index");
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
