package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import java.math.BigInteger;

public class Amount {

    private final BigInteger value;
    private final UnsignedLong tokenId;

    Amount(BigInteger value, UnsignedLong tokenId) {
        this.value = value;
        this.tokenId = tokenId;
    }

    @NonNull
    public BigInteger getValue() {
        return this.value;
    }

    @NonNull
    public UnsignedLong getTokenId() {
        return this.tokenId;
    }

}
