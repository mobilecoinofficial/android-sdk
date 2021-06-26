// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.AmountDecoderException;
import com.mobilecoin.lib.log.Logger;

import java.math.BigInteger;

/**
 * Encapsulates the abstraction of a native Amount with a JNI link to control the native
 * counterpart.
 */
final class Amount extends Native {
    private final static String TAG = Amount.class.getName();
    private final MobileCoinAPI.Amount protoBufAmount;

    /**
     * Constructs native Amount object from the commitment and masked data
     *
     * @param commitment  A Pedersen commitment {@code v*G + s*H}
     * @param maskedValue {@code masked_value = value XOR_8 Blake2B(value_mask || shared_secret)}
     */
    Amount(@NonNull byte[] commitment, long maskedValue) throws AmountDecoderException {
        protoBufAmount = MobileCoinAPI.Amount.newBuilder()
                .setCommitment(MobileCoinAPI.CompressedRistretto.newBuilder()
                        .setData(ByteString.copyFrom(commitment)).build())
                .setMaskedValue(maskedValue).build();
        try {
            init_jni(
                    commitment,
                    maskedValue
            );
        } catch (Exception exception) {
            AmountDecoderException amountDecoderException = new AmountDecoderException("Unable to" +
                    " initialize amount object", exception);
            Util.logException(TAG, amountDecoderException);
            throw amountDecoderException;
        }
    }

    /**
     * Constructs native Amount object from the txOutSharedSecret and masked value.
     *
     * @param txOutSharedSecret  A {@link RistrettoPrivate} representing the shared secret.
     * @param maskedValue {@code masked_value = value XOR_8 Blake2B(value_mask || shared_secret)}
     */
    Amount(@NonNull RistrettoPublic txOutSharedSecret, long maskedValue) throws AmountDecoderException {
        try {
            init_jni_with_secret(
                txOutSharedSecret,
                maskedValue
            );
            byte[] amountBytes = get_bytes();
            protoBufAmount = MobileCoinAPI.Amount.parseFrom(amountBytes);
        } catch (Exception exception) {
            AmountDecoderException amountDecoderException = new AmountDecoderException("Unable to" +
                " initialize amount object", exception);
            Util.logException(TAG, amountDecoderException);
            throw amountDecoderException;
        }
    }

    /**
     * Constructs native Amount object from the protocol buffer
     */
    Amount(@NonNull MobileCoinAPI.Amount amount) throws AmountDecoderException {
        this(amount.getCommitment().getData().toByteArray(), amount.getMaskedValue());
    }

    /**
     * Constructs native Amount object from the protocol buffer
     */
    static Amount fromProtoBufObject(@NonNull MobileCoinAPI.Amount protoBuf)
            throws AmountDecoderException {
        Logger.i(TAG, "Deserializing amount from protobuf object");
        return new Amount(protoBuf);
    }

    /**
     * Construct and return a new Amount protocol buffer object
     */
    @NonNull
    MobileCoinAPI.Amount toProtoBufObject() {
        return protoBufAmount;
    }

    /**
     * Amount's commitment
     *
     * @return A Pedersen commitment {@code v*G + s*H}
     */
    @NonNull
    byte[] getCommitment() {
        return protoBufAmount.getCommitment().getData().toByteArray();
    }

    /**
     * Amount's masked value
     *
     * @return {@code masked_value = value XOR_8 Blake2B(value_mask || shared_secret)}
     */
    long getMaskedValue() {
        return protoBufAmount.getMaskedValue();
    }

    /**
     * Unmasks the value of the Amount
     *
     * @param txPubKey transaction public key
     * @return unmasked amount of picoMob represented as a BigInteger
     */
    @NonNull
    BigInteger unmaskValue(
            @NonNull RistrettoPrivate viewKey,
            @NonNull RistrettoPublic txPubKey
    ) throws AmountDecoderException {
        Logger.i(TAG, "Unmasking amount");
        try {
            return unmask_value(
                    viewKey,
                    txPubKey
            );
        } catch (Exception exception) {
            AmountDecoderException amountDecoderException = new AmountDecoderException("Unable to" +
                    " unmask the amount", exception);
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
    private native BigInteger unmask_value(
            @NonNull RistrettoPrivate view_key,
            @NonNull RistrettoPublic pub_key
    );

    private native void init_jni(
            @NonNull byte[] commitment,
            long maskedValue
    );

    private native void init_jni_with_secret(
        @NonNull RistrettoPublic txOutSharedSecret,
        long maskedValue
    );

    private native byte[] get_bytes();

    private native void finalize_jni();
}
