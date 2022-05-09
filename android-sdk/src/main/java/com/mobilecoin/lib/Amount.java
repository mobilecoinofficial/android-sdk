package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.api.MobileCoinAPI;

import java.math.BigInteger;

public class Amount extends Native {

    private final BigInteger value;
    private final MobileCoinAPI.KnownTokenId tokenId;

    Amount(long value, long unmaskedTokenId) {
        this.value = BigInteger.valueOf(value);
        this.tokenId = MobileCoinAPI.KnownTokenId.forNumber((int)unmaskedTokenId);
    }

    @NonNull
    public BigInteger getValue() {
        return this.value;
    }

    @NonNull
    public MobileCoinAPI.KnownTokenId getTokenId() {
        return this.tokenId;
    }

}
