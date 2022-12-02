package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.AmountDecoderException;

abstract class MaskedAmount extends Native {

    /**
     * Construct and return a new MaskedAmount protocol buffer object
     */
    @NonNull
    abstract MobileCoinAPI.MaskedAmount toProtoBufObject();

    /**
     * MaskedAmount's commitment
     *
     * @return A Pedersen commitment {@code v*G + s*H}
     */
    @NonNull
    abstract byte[] getCommitment();

    /**
     * MaskedAmount's masked value
     *
     * @return {@code masked_value = value XOR_8 Blake2B(value_mask || shared_secret)}
     */
    abstract long getMaskedValue();

    /**
     * Unmasks the MaskedAmount
     *
     * @param txPubKey transaction public key
     * @return unmasked amount of picoMob represented as a BigInteger
     */
    @NonNull
    abstract Amount unmaskAmount(
            @NonNull RistrettoPrivate viewKey,
            @NonNull RistrettoPublic txPubKey
    ) throws AmountDecoderException;

}
