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

    public PaymentRequest(
            @NonNull PublicAddress publicAddress,
            @NonNull UnsignedLong value,
            @NonNull String memo
    ) {
        this.publicAddress = publicAddress;
        this.value = value;
        this.memo = memo;
    }

    @NonNull
    static PaymentRequest fromProtoBufObject(@NonNull Printable.PaymentRequest protoBuf)
            throws SerializationException {
        Logger.i(TAG, "Deserializing PaymentRequest from protobof");
        PublicAddress publicAddress = PublicAddress.fromProtoBufObject(protoBuf.getPublicAddress());
        return new PaymentRequest(
                publicAddress,
                UnsignedLong.fromLongBits(protoBuf.getValue()),
                protoBuf.getMemo()
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
    Printable.PaymentRequest toProtoBufObject() {
        Logger.i(TAG, "Serializing to protobuf");
        return Printable.PaymentRequest.newBuilder().setMemo(getMemo())
                .setPublicAddress(getPublicAddress().toProtoBufObject()).setValue(getValue().longValue())
                .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicAddress, value, memo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentRequest that = (PaymentRequest) o;
        return publicAddress.equals(that.publicAddress) &&
                value.equals(that.value) &&
                memo.equals(that.memo);
    }
}
