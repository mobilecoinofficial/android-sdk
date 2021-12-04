// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.mobilecoin.api.Printable;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import java.util.Arrays;
import java.util.Objects;

public class TransferPayload {
    private final static String TAG = TransferPayload.class.getName();
    private final byte[] rootEntropy;
    private final byte[] bip39Entropy;
    private final RistrettoPublic publicKey;
    private final String memo;

    private TransferPayload(
            @Nullable byte[] rootEntropy,
            @Nullable byte[] bip39Entropy,
            @NonNull RistrettoPublic publicKey,
            @NonNull String memo
    ) {
        this.rootEntropy = rootEntropy;
        this.bip39Entropy = bip39Entropy;
        this.publicKey = publicKey;
        this.memo = memo;
    }
    public static TransferPayload fromRootEntropy(
            @NonNull byte[] rootEntropy,
            @NonNull RistrettoPublic publicKey,
            @NonNull String memo
    ) {
        return new TransferPayload(rootEntropy, null, publicKey, memo);
    }

    public static TransferPayload fromBip39Entropy(
            @NonNull byte[] bip39Entropy,
            @NonNull RistrettoPublic publicKey,
            @NonNull String memo
    ) {
        return new TransferPayload(null, bip39Entropy, publicKey, memo);
    }

    @NonNull
    static TransferPayload fromProtoBufObject(@NonNull Printable.TransferPayload protoBuf)
            throws SerializationException {
        Logger.i(TAG, "Initializing from protobuf");
        ByteString rootEntropyString = protoBuf.getRootEntropy();
        ByteString bip39EntropyString = protoBuf.getBip39Entropy();
        return new TransferPayload(
                rootEntropyString != null ? rootEntropyString.toByteArray() : null,
                bip39EntropyString != null ? bip39EntropyString.toByteArray() : null,
                RistrettoPublic.fromBytes(protoBuf.getTxOutPublicKey().getData().toByteArray()),
                protoBuf.getMemo()
        );
    }

    @Nullable
    public byte[] getRootEntropy() {
        return rootEntropy;
    }

    @Nullable
    public byte[] getBip39Entropy() {
        return bip39Entropy;
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
        Printable.TransferPayload.Builder payloadBuilder = Printable.TransferPayload.newBuilder();
        payloadBuilder
                .setMemo(getMemo())
                .setTxOutPublicKey(getPublicKey().toProtoBufObject());
        if (rootEntropy != null) {
           payloadBuilder.setRootEntropy(ByteString.copyFrom(rootEntropy));
        }
        if (bip39Entropy != null) {
            payloadBuilder.setBip39Entropy(ByteString.copyFrom(bip39Entropy));
        }
        return payloadBuilder.build();
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
