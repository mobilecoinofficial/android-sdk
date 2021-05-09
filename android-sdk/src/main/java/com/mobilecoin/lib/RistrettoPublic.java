// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.SerializationException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * <pre>
 * A RistrettoPublic key
 *
 * MobileCoin public addresses consist of two RistrettoPublic keys: view & spend
 * </pre>
 */
public final class RistrettoPublic extends Native implements Serializable {
    public static final int PUBLIC_KEY_SIZE = 32;
    private static final long serialVersionUID = 1L;
    private MobileCoinAPI.CompressedRistretto compressedRistretto;

    private RistrettoPublic(long existingRustObj) {
        rustObj = existingRustObj;
        try {
            byte[] keyBytes = get_bytes();
            MobileCoinAPI.CompressedRistretto.Builder builder =
                    MobileCoinAPI.CompressedRistretto.newBuilder();
            builder.setData(ByteString.copyFrom(keyBytes));
            compressedRistretto = builder.build();
        } catch (Exception ex) {
            throw new IllegalStateException("BUG: unable to get key bytes from the native code",
                    ex);
        }
    }

    private RistrettoPublic(@NonNull MobileCoinAPI.CompressedRistretto compressedRistretto)
            throws SerializationException {
        this.compressedRistretto = compressedRistretto;
        try {
            init_jni(this.compressedRistretto.getData().toByteArray());
        } catch (Exception ex) {
            throw new SerializationException(ex.getLocalizedMessage());
        }
    }

    @NonNull
    static RistrettoPublic fromProtoBufObject(@NonNull MobileCoinAPI.CompressedRistretto compressedRistretto)
            throws SerializationException {
        return new RistrettoPublic(compressedRistretto);
    }

    /**
     * Create RistrettoPublic key instance from the key bytes.
     *
     * @param bytes a key bytes buffer. Must conform to a valid key format.
     * @return initialized {@link RistrettoPublic} instance
     */
    @NonNull
    public static RistrettoPublic fromBytes(@NonNull byte[] bytes) throws SerializationException {
        MobileCoinAPI.CompressedRistretto compressedRistretto =
                MobileCoinAPI.CompressedRistretto.newBuilder().setData(ByteString.copyFrom(bytes))
                        .build();
        return RistrettoPublic.fromProtoBufObject(compressedRistretto);
    }

    /**
     * Constructs a public key from an existing rust object. This is used by the JNI
     * implementation.
     */
    @NonNull
    static RistrettoPublic fromJNI(long rustObj) {
        return new RistrettoPublic(rustObj);
    }

    @NonNull
    MobileCoinAPI.CompressedRistretto toProtoBufObject() {
        return compressedRistretto;
    }

    /**
     * Returns the underlying private key bytes
     *
     * @return key bytes as an array of bytes
     */
    @NonNull
    public byte[] getKeyBytes() {
        return compressedRistretto.getData().toByteArray();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(compressedRistretto.getData().toByteArray());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RistrettoPublic that = (RistrettoPublic) o;
        return Arrays.equals(compressedRistretto.getData().toByteArray(),
                that.compressedRistretto.getData().toByteArray());
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.write(compressedRistretto.getData().toByteArray());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        byte[] keyBytes = new byte[PUBLIC_KEY_SIZE];
        int bytesRead = in.read(keyBytes);
        if (bytesRead != PUBLIC_KEY_SIZE) {
            throw new IOException("Invalid public key size in the serialized data");
        }
        try {
            compressedRistretto = MobileCoinAPI.CompressedRistretto.newBuilder()
                    .setData(ByteString.copyFrom(keyBytes)).build();
        } catch (Exception e) {
            throw new IOException("Unable to create a public key from the serialized data", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    // JNI calls
    private native void init_jni(@NonNull byte[] raw_key_bytes);

    private native void finalize_jni();

    @NonNull
    private native byte[] get_bytes();
}
