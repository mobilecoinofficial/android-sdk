// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class Transaction extends Native {
    private final static String TAG = Transaction.class.getName();
    private final MobileCoinAPI.Tx protoBufTx;
    private byte[] serializedBytes;

    private Transaction(long existingRustObj) throws SerializationException {
        rustObj = existingRustObj;
        try {
            protoBufTx = MobileCoinAPI.Tx.parseFrom(toByteArray());
        } catch (InvalidProtocolBufferException ex) {
            throw new SerializationException(ex.getLocalizedMessage(), ex);
        }
    }

    private Transaction(@NonNull MobileCoinAPI.Tx tx) {
        protoBufTx = tx;
    }

    @NonNull
    static Transaction fromJNI(long rustObj) throws SerializationException {
        return new Transaction(rustObj);
    }

    /**
     * Construct an {@link Transaction} object from the serialized bytes.
     *
     * @param serializedBytes a binary representation of the {@link Transaction} object (see {@link
     *                        Transaction#toByteArray()})
     * @throws IllegalArgumentException if serialized bytes parameter is invalid
     */
    @NonNull
    public static Transaction fromBytes(@NonNull byte[] serializedBytes)
            throws SerializationException {
        Logger.i(TAG, "Deserializing transaction from bytes");
        try {
            MobileCoinAPI.Tx tx = MobileCoinAPI.Tx.parseFrom(serializedBytes);
            return Transaction.fromProtoBufObject(tx);
        } catch (InvalidProtocolBufferException ex) {
            throw new SerializationException(ex.getLocalizedMessage());
        }
    }

    @NonNull
    static Transaction fromProtoBufObject(@NonNull MobileCoinAPI.Tx tx) {
        Logger.i(TAG, "Deserializing transaction from protobuf");
        return new Transaction(tx);
    }

    @NonNull
    public synchronized BigInteger getFee() {
        BigInteger fee = BigInteger.valueOf(protoBufTx.getPrefix().getFee());
        Logger.i(TAG, "Getting fee", null, fee);
        return fee;
    }

    /**
     * Output public keys are needed to identify output TxOuts created by this transaction
     */
    @NonNull
    public Set<RistrettoPublic> getOutputPublicKeys() {
        Logger.i(TAG, "Getting output public keys");
        List<MobileCoinAPI.TxOut> outputs = protoBufTx.getPrefix().getOutputsList();
        return outputs.stream()
                .map(txOut -> {
                    try {
                        return RistrettoPublic.fromProtoBufObject(txOut.getPublicKey());
                    } catch (SerializationException e) {
                        Logger.e(TAG, "BUG: ");
                        throw new IllegalStateException("BUG: type conversion should not trigger " +
                                "serialization exception", e);
                    }
                })
                .collect(Collectors.toSet());
    }

    /**
     * Returns a binary representation of the {@link Transaction} instance
     */
    @NonNull
    public byte[] toByteArray() throws SerializationException {
        Logger.i(TAG, "Serializing to byte array");
        try {
            if (serializedBytes == null) {
                serializedBytes = encode();
            }
            return serializedBytes;
        } catch (Exception ex) {
            throw new SerializationException(ex.getLocalizedMessage(), ex);
        }
    }

    @NonNull
    synchronized MobileCoinAPI.Tx toProtoBufObject() {
        Logger.i(TAG, "Serializing to protobuf");
        return protoBufTx;
    }

    /**
     * Key images for the transaction input TxOuts
     */
    @NonNull
    public synchronized Set<KeyImage> getKeyImages() {
        Logger.i(TAG, "Getting key images");
        List<MobileCoinAPI.RingMLSAG> signatures =
                protoBufTx.getSignature().getRingSignaturesList();
        return signatures.stream()
                .map(sig -> KeyImage.fromBytes(sig.getKeyImage().getData().toByteArray()))
                .collect(Collectors.toSet());
    }

    public long getTombstoneBlockIndex() {
        long tombstoneBlockIndex = toProtoBufObject().getPrefix().getTombstoneBlock();
        Logger.i(TAG, "Getting tombstone block index", null, tombstoneBlockIndex);
        return tombstoneBlockIndex;
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    private native void finalize_jni();

    @NonNull
    private native byte[] encode();

    public enum Status {
        UNKNOWN, ACCEPTED, FAILED;

        private UnsignedLong blockIndex;

        synchronized Status atBlock(UnsignedLong receivedAt) {
            blockIndex = receivedAt;
            return this;
        }

        @NonNull
        public synchronized UnsignedLong getBlockIndex() {
            return blockIndex;
        }
    }

}
