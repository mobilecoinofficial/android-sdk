// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.SerializationException;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * <pre>
 * A RistrettoPrivate key
 *
 * MobileCoin accounts consist of two RistrettoPrivate keys: view & spend
 * </pre>
 */
public class RistrettoPrivate extends Native {
    private final byte[] keyBytes;

    private RistrettoPrivate(long existingRustObj) {
        rustObj = existingRustObj;
        keyBytes = getKeyBytes();
    }

    private RistrettoPrivate(
            @NonNull byte[] bytes,
            @NonNull PayloadType type
    ) throws SerializationException {
        try {
            if (type == PayloadType.KEY_BYTES) {
                init_jni(bytes);
            } else if (type == PayloadType.SEED_BYTES) {
                init_jni_seed(bytes);
            }
            keyBytes = getKeyBytes();
        } catch (Exception ex) {
            throw new SerializationException(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Create a RistrettoPrivate key instance from the key bytes
     *
     * @param bytes a key buffer. Must conform to a valid key format.
     * @return initialized {@link RistrettoPrivate} instance
     */
    @NonNull
    public static RistrettoPrivate fromBytes(@NonNull byte[] bytes) throws SerializationException {
        return new RistrettoPrivate(
                bytes,
                PayloadType.KEY_BYTES
        );
    }

    /**
     * Generates a new {@link RistrettoPrivate} using explicit random seed
     *
     * @param seed 32 bytes long array to seed the random engine
     * @return initialized {@link RistrettoPrivate} instance
     */
    @NonNull
    public static RistrettoPrivate generateNewKey(@NonNull byte[] seed)
            throws SerializationException {
        return new RistrettoPrivate(
                seed,
                PayloadType.SEED_BYTES
        );
    }

    /**
     * Generates a new {@link RistrettoPrivate} using implicitly generated random seed. Internal
     * seed is generated using {@link SecureRandom}
     *
     * @return initialized {@link RistrettoPrivate} instance
     */
    @NonNull
    public static RistrettoPrivate generateNewKey() {
        try {
            byte[] seed = new byte[32];
            (new SecureRandom()).nextBytes(seed);
            return generateNewKey(seed);
        } catch (SerializationException ex) {
            throw new IllegalStateException("BUG: unable to create new key from a valid seed", ex);
        }
    }

    /**
     * Constructs a private key from an existing rust object. This is used by the JNI
     * implementation.
     */
    @NonNull
    static RistrettoPrivate fromJNI(long rustObj) {
        return new RistrettoPrivate(rustObj);
    }

    @NonNull
    static RistrettoPrivate fromProtoBufObject(
            @NonNull MobileCoinAPI.RistrettoPrivate ristrettoPrivate
    ) throws SerializationException {
        byte[] keyBytes = ristrettoPrivate.getData().toByteArray();
        return new RistrettoPrivate(
                keyBytes,
                PayloadType.KEY_BYTES
        );
    }

    /**
     * Returns buffer of the underlying native private key bytes
     *
     * @return key bytes buffer as an array of bytes
     */
    @NonNull
    public byte[] getKeyBytes() {
        try {
            return get_bytes();
        } catch (Exception exception) {
            throw new IllegalStateException("BUG: Unable to call FFI getKeyBytes", exception);
        }

    }

    /**
     * Generates and returns {@link RistrettoPublic} corresponding to the underlying private key
     *
     * @return instance of a {@link RistrettoPublic}
     */
    @NonNull
    public RistrettoPublic getPublicKey() {
        long rustObj;
        try {
            rustObj = get_public();
        } catch (Exception exception) {
            throw new IllegalStateException("BUG: Unable to call FFI getKeyBytes");
        }
        return RistrettoPublic.fromJNI(rustObj);
    }

    @NonNull
    MobileCoinAPI.RistrettoPrivate toProtoBufObject() {
        MobileCoinAPI.RistrettoPrivate.Builder builder =
                MobileCoinAPI.RistrettoPrivate.newBuilder();
        builder.setData(ByteString.copyFrom(getKeyBytes()));
        return builder.build();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RistrettoPrivate that = (RistrettoPrivate) o;
        return Arrays.equals(this.keyBytes, that.keyBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getKeyBytes());
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    // JNI calls
    private native void init_jni(@NonNull byte[] rawKeyBytes);

    private native void init_jni_seed(@NonNull byte[] seedBytes);

    private native void finalize_jni();

    @NonNull
    private native byte[] get_bytes();

    @NonNull
    private native long get_public();

    private enum PayloadType {
        KEY_BYTES, SEED_BYTES
    }
}
