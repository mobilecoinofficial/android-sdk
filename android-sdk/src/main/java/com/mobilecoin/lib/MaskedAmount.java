// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.AmountDecoderException;
import com.mobilecoin.lib.log.Logger;

/**
 * Encapsulates the abstraction of a native MaskedAmount with a JNI link to control the native
 * counterpart.
 */
final class MaskedAmount extends Native {
    private final static String TAG = MaskedAmount.class.getName();
    private final MobileCoinAPI.MaskedAmount protoBufMaskedAmount;

    /**
     * Constructs native MaskedAmount object from the commitment and masked data
     *
     * @param commitment  A Pedersen commitment {@code v*G + s*H}
     * @param maskedValue {@code masked_value = value XOR_8 Blake2B(value_mask || shared_secret)}
     */
    MaskedAmount(@NonNull byte[] commitment, long maskedValue, @NonNull byte maskedTokenId[]) throws AmountDecoderException {
        protoBufMaskedAmount = MobileCoinAPI.MaskedAmount.newBuilder()
                .setCommitment(MobileCoinAPI.CompressedRistretto.newBuilder()
                        .setData(ByteString.copyFrom(commitment)).build())
                .setMaskedValue(maskedValue)
                .setMaskedTokenId(ByteString.copyFrom(maskedTokenId)).build();
        try {
            init_jni(
                    commitment,
                    maskedValue,
                    maskedTokenId
            );
        } catch (Exception exception) {
            AmountDecoderException amountDecoderException = new AmountDecoderException("Unable to" +
                    " initialize MaskedAmount object", exception);
            Util.logException(TAG, amountDecoderException);
            throw amountDecoderException;
        }
    }

    /**
     * Constructs native MaskedAmount object from the txOutSharedSecret and masked value.
     *
     * @param txOutSharedSecret  A {@link RistrettoPublic} representing the shared secret.
     * @param maskedValue {@code masked_value = value XOR_8 Blake2B(value_mask || shared_secret)}
     */
    MaskedAmount(@NonNull RistrettoPublic txOutSharedSecret,
                 long maskedValue,
                 @NonNull byte maskedTokenId[]
    ) throws AmountDecoderException {
        try {
            init_jni_with_secret(
                txOutSharedSecret,
                maskedValue,
                maskedTokenId
            );
            byte[] amountBytes = get_bytes();
            protoBufMaskedAmount = MobileCoinAPI.MaskedAmount.parseFrom(amountBytes);
        } catch (Exception exception) {
            AmountDecoderException amountDecoderException = new AmountDecoderException("Unable to" +
                " initialize MaskedAmount object", exception);
            Util.logException(TAG, amountDecoderException);
            throw amountDecoderException;
        }
    }

    /**
     * Constructs native MaskedAmount object from the protocol buffer
     */
    MaskedAmount(@NonNull MobileCoinAPI.MaskedAmount amount) throws AmountDecoderException {
        this(
                amount.getCommitment().getData().toByteArray(),
                amount.getMaskedValue(),
                amount.getMaskedTokenId().toByteArray()
        );
    }

    /**
     * Constructs native MaskedAmount object from the protocol buffer
     */
    static MaskedAmount fromProtoBufObject(@NonNull MobileCoinAPI.MaskedAmount protoBuf)
            throws AmountDecoderException {
        Logger.i(TAG, "Deserializing amount from protobuf object");
        return new MaskedAmount(protoBuf);
    }

    /**
     * Construct and return a new MaskedAmount protocol buffer object
     */
    @NonNull
    MobileCoinAPI.MaskedAmount toProtoBufObject() {
        return protoBufMaskedAmount;
    }

    /**
     * MaskedAmount's commitment
     *
     * @return A Pedersen commitment {@code v*G + s*H}
     */
    @NonNull
    byte[] getCommitment() {
        return protoBufMaskedAmount.getCommitment().getData().toByteArray();
    }

    /**
     * MaskedAmount's masked value
     *
     * @return {@code masked_value = value XOR_8 Blake2B(value_mask || shared_secret)}
     */
    long getMaskedValue() {
        return protoBufMaskedAmount.getMaskedValue();
    }

    /**
     * Unmasks the MaskedAmount
     *
     * @param txPubKey transaction public key
     * @return unmasked amount of picoMob represented as a BigInteger
     */
    @NonNull
    Amount unmaskAmount(
        @NonNull RistrettoPrivate viewKey,
        @NonNull RistrettoPublic txPubKey
    ) throws AmountDecoderException {
        Logger.i(TAG, "Unmasking amount");
        try {
            return unmask_amount(
                    viewKey,
                    txPubKey
            );
        } catch (Exception exception) {
            AmountDecoderException amountDecoderException = new AmountDecoderException("Unable to" +
                    " unmask the MaskedAmount", exception);
            Util.logException(TAG, amountDecoderException);
            throw amountDecoderException;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        Logger.i(TAG, "Finalizing object");
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    /* Native methods */

    @NonNull
    private native Amount unmask_amount(
            @NonNull RistrettoPrivate view_key,
            @NonNull RistrettoPublic pub_key
    );

    private native void init_jni(
            @NonNull byte[] commitment,
            long maskedValue,
            @NonNull byte maskedTokenId[]
    );

    private native void init_jni_with_secret(
        @NonNull RistrettoPublic txOutSharedSecret,
        long maskedValue,
        @NonNull byte maskedTokenId[]
    );

    private native byte[] get_bytes();

    private native void finalize_jni();
}
