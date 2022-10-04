// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.api.Printable;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import java.util.Objects;

public final class PaymentRequest {
    private final static String TAG = PaymentRequest.class.getName();
    private final PublicAddress publicAddress;
    private final UnsignedLong value;
    private final String memo;
    private final TokenId tokenId;

    public PaymentRequest(
            @NonNull PublicAddress publicAddress,
            @NonNull UnsignedLong value,
            @NonNull String memo,
            @NonNull TokenId tokenId
    ) {
        this.publicAddress = publicAddress;
        this.value = value;
        this.memo = memo;
        this.tokenId = tokenId;
    }

    @NonNull
    static PaymentRequest fromProtoBufObject(@NonNull Printable.PaymentRequest protoBuf)
            throws SerializationException {
        Logger.i(TAG, "Deserializing PaymentRequest from protobuf");
        PublicAddress publicAddress = PublicAddress.fromProtoBufObject(protoBuf.getPublicAddress());
        TokenId tokenId = TokenId.from(
                UnsignedLong.fromLongBits(protoBuf.getTokenId())
        );
        return new PaymentRequest(
                publicAddress,
                UnsignedLong.fromLongBits(protoBuf.getValue()),
                protoBuf.getMemo(),
                tokenId
        );
    }

    @NonNull
    public PublicAddress getPublicAddress() {
        Logger.i(TAG, "Getting public address", null, publicAddress);
        return publicAddress;
    }

    @NonNull
    public UnsignedLong getValue() {
        Logger.i(TAG, "Getting public value", null, value);
        return value;
    }

    @NonNull
    public String getMemo() {
        Logger.i(TAG, "Getting memo", null, memo);
        return memo;
    }

    @NonNull
    public TokenId getTokenId() {
        return tokenId;
    }

    @NonNull
    Printable.PaymentRequest toProtoBufObject() {
        Logger.i(TAG, "Serializing to protobuf");
        return Printable.PaymentRequest.newBuilder()
                .setMemo(getMemo())
                .setPublicAddress(getPublicAddress().toProtoBufObject())
                .setValue(getValue().longValue())
                .setTokenId(tokenId.getId().longValue())
                .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicAddress, value, memo, tokenId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentRequest that = (PaymentRequest) o;
        return publicAddress.equals(that.publicAddress) &&
                value.equals(that.value) &&
                tokenId.equals(that.tokenId) &&
                memo.equals(that.memo);
    }
}
