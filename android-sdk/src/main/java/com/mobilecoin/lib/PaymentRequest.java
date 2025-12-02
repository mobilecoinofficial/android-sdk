// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    private final UnsignedLong paymentId;

    public PaymentRequest(
            @NonNull PublicAddress publicAddress,
            @NonNull UnsignedLong value,
            @NonNull String memo,
            @NonNull TokenId tokenId,
            @Nullable UnsignedLong paymentId
    ) {
        this.publicAddress = publicAddress;
        this.value = value;
        this.memo = memo;
        this.tokenId = tokenId;
        this.paymentId = paymentId;
    }

    @NonNull
    static PaymentRequest fromProtoBufObject(@NonNull Printable.PaymentRequest protoBuf)
            throws SerializationException {
        Logger.i(TAG, "Deserializing PaymentRequest from protobuf");
        PublicAddress publicAddress = PublicAddress.fromProtoBufObject(protoBuf.getPublicAddress());
        TokenId tokenId = TokenId.from(
                UnsignedLong.fromLongBits(protoBuf.getTokenId())
        );
        long rawPaymentId = protoBuf.getPaymentId();
        UnsignedLong paymentId = (rawPaymentId != 0) ?
                UnsignedLong.fromLongBits(rawPaymentId) : null;
        return new PaymentRequest(
                publicAddress,
                UnsignedLong.fromLongBits(protoBuf.getValue()),
                protoBuf.getMemo(),
                tokenId,
                paymentId
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

    @Nullable
    public UnsignedLong getPaymentId() {
        Logger.i(TAG, "Getting payment id", null, paymentId);
        return paymentId;
    }

    @NonNull
    Printable.PaymentRequest toProtoBufObject() {
        Logger.i(TAG, "Serializing to protobuf");
        Printable.PaymentRequest.Builder paymentRequestBuilder = Printable.PaymentRequest.newBuilder();

        paymentRequestBuilder
                .setMemo(getMemo())
                .setPublicAddress(getPublicAddress().toProtoBufObject())
                .setValue(getValue().longValue())
                .setTokenId(tokenId.getId().longValue());

        if (paymentId != null) {
            paymentRequestBuilder.setPaymentId(paymentId.longValue());
        }

        return paymentRequestBuilder.build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicAddress, value, memo, tokenId, paymentId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentRequest that = (PaymentRequest) o;
        return publicAddress.equals(that.publicAddress) &&
                value.equals(that.value) &&
                tokenId.equals(that.tokenId) &&
                memo.equals(that.memo) &&
                Objects.equals(paymentId, that.paymentId);
    }
}
