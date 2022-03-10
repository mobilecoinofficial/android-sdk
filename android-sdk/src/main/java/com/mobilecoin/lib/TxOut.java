// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;


import androidx.annotation.NonNull;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.AmountDecoderException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;
import java.util.Objects;

final class TxOut extends Native {
    private final static String TAG = TxOut.class.getName();
    private final MobileCoinAPI.TxOut protoBufTxOut;
    private final RistrettoPublic pubKey;
    private final RistrettoPublic targetKey;

    private TxOut(@NonNull byte[] serializedBytes) throws SerializationException {
        try {
            init_from_protobuf_bytes(serializedBytes);
            protoBufTxOut = MobileCoinAPI.TxOut.parseFrom(serializedBytes);
            pubKey = RistrettoPublic.fromProtoBufObject(protoBufTxOut.getPublicKey());
            targetKey = RistrettoPublic.fromProtoBufObject(protoBufTxOut.getTargetKey());
        } catch (Exception ex) {
            SerializationException serializationException =
                    new SerializationException(ex.getLocalizedMessage(), ex);
            Util.logException(TAG, serializationException);
            throw serializationException;
        }
    }

    private TxOut(long existingRustObj) throws SerializationException {
        rustObj = existingRustObj;
        try {
            protoBufTxOut = MobileCoinAPI.TxOut.parseFrom(toByteArray());
            pubKey = RistrettoPublic.fromProtoBufObject(protoBufTxOut.getPublicKey());
            targetKey = RistrettoPublic.fromProtoBufObject(protoBufTxOut.getTargetKey());
        } catch (InvalidProtocolBufferException ex) {
            SerializationException serializationException =
                    new SerializationException(ex.getLocalizedMessage());
            Util.logException(TAG, serializationException);
            throw serializationException;
        }
    }

    private TxOut(@NonNull MobileCoinAPI.TxOut tx) throws SerializationException {
        try {
            protoBufTxOut = tx;
            pubKey = RistrettoPublic.fromProtoBufObject(protoBufTxOut.getPublicKey());
            targetKey = RistrettoPublic.fromProtoBufObject(protoBufTxOut.getTargetKey());
            init_from_protobuf_bytes(tx.toByteString().toByteArray());
        } catch (Exception ex) {
            SerializationException serializationException =
                    new SerializationException(ex.getLocalizedMessage());
            Util.logException(TAG, serializationException);
            throw serializationException;
        }
    }

    @NonNull
    static TxOut fromJNI(long rustObj) throws SerializationException {
        return new TxOut(rustObj);
    }

    /**
     * Construct an {@link Transaction} object from the serialized bytes.
     *
     * @param serializedBytes a binary representation of the {@link Transaction} object (see {@link
     *                        Transaction#toByteArray()})
     * @throws IllegalArgumentException if serialized bytes parameter is invalid
     */
    @NonNull
    public static TxOut fromBytes(@NonNull byte[] serializedBytes) throws SerializationException {
        return new TxOut(serializedBytes);
    }

    @NonNull
    static TxOut fromProtoBufObject(@NonNull MobileCoinAPI.TxOut txOut) throws SerializationException {
        return new TxOut(txOut);
    }

    @NonNull
    public Amount getAmount() {
        try {
            return Amount.fromProtoBufObject(protoBufTxOut.getAmount());
        } catch (AmountDecoderException exception) {
            // the amount is validated during the object construction
            IllegalStateException illegalStateException = new IllegalStateException(exception);
            Logger.wtf(TAG, "BUG: unreachable code", illegalStateException);
            throw illegalStateException;
        }
    }

    /**
     * Returns a binary representation of the {@link Transaction} instance
     */
    @NonNull
    public byte[] toByteArray() throws SerializationException {
        try {
            return encode();
        } catch (Exception ex) {
            throw new SerializationException(ex.getLocalizedMessage());
        }
    }

    @NonNull
    MobileCoinAPI.TxOut toProtoBufObject() {
        return protoBufTxOut;
    }

    @NonNull
    byte[] decryptMemoPayload(@NonNull AccountKey accountKey) {
        if (!protoBufTxOut.hasEMemo()) {
            return new byte[0];
        }
        return decrypt_memo_payload(accountKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TxOut txOut = (TxOut) o;
        return protoBufTxOut.equals(txOut.protoBufTxOut) &&
                pubKey.equals(txOut.pubKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protoBufTxOut, pubKey);
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    @NonNull
    byte[] computeKeyImage(AccountKey accountKey) {
        return compute_key_image(accountKey);
    }

    @NonNull
    RistrettoPublic getPubKey() {
        return pubKey;
    }

    @NonNull
    RistrettoPublic getTargetKey() {
        return targetKey;
    }

    private native void init_from_protobuf_bytes(@NonNull byte[] data);

    private native void finalize_jni();

    @NonNull
    private native byte[] compute_key_image(@NonNull AccountKey account_key);

    @NonNull
    private native byte[] encode();

    @NonNull
    private native byte[] decrypt_memo_payload(@NonNull AccountKey accountKey);
}
