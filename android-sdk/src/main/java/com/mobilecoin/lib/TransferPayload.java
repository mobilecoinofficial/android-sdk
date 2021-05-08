// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.mobilecoin.api.Printable;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import java.util.Arrays;
import java.util.Objects;

public class TransferPayload {
    private final static String TAG = TransferPayload.class.getName();
    private final byte[] rootEntropy;
    private final RistrettoPublic publicKey;
    private final String memo;

    public TransferPayload(
            @NonNull byte[] rootEntropy,
            @NonNull RistrettoPublic publicKey,
            @NonNull String memo
    ) {
        this.rootEntropy = rootEntropy;
        this.publicKey = publicKey;
        this.memo = memo;
    }

    @NonNull
    static TransferPayload fromProtoBufObject(@NonNull Printable.TransferPayload protoBuf)
            throws SerializationException {
        Logger.i(TAG, "Initializing from protobuf");
        return new TransferPayload(
                protoBuf.getBip39Entropy().toByteArray(),
                RistrettoPublic.fromBytes(protoBuf.getTxOutPublicKey().getData().toByteArray()),
                protoBuf.getMemo()
        );
    }

    @NonNull
    public byte[] getRootEntropy() {
        return rootEntropy;
    }

    @NonNull
    public RistrettoPublic getPublicKey() {
        Logger.i(TAG, "Getting public key", null, publicKey);
        return publicKey;
    }

    @NonNull
    public String getMemo() {
        Logger.i(TAG, "Getting memo", null, memo);
        return memo;
    }

    @NonNull
    Printable.TransferPayload toProtoBufObject() {
        Logger.i(TAG, "Serializing to protobuf");
        return Printable.TransferPayload.newBuilder().setMemo(getMemo())
                .setTxOutPublicKey(getPublicKey().toProtoBufObject())
                .setBip39Entropy(ByteString.copyFrom(rootEntropy)).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferPayload that = (TransferPayload) o;
        return Arrays.equals(rootEntropy, that.rootEntropy) &&
                publicKey.equals(that.publicKey) &&
                memo.equals(that.memo);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(publicKey, memo);
        result = 31 * result + Arrays.hashCode(rootEntropy);
        return result;
    }
}
