// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.AmountDecoderException;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FogSyncException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import java.math.BigInteger;

/**
 * This receipt is created by {@link MobileCoinTransactionClient#prepareTransaction(PublicAddress, BigInteger, BigInteger, TxOutMemoBuilder)} Then shared with the recipient, so they can check the transaction status. Only the
 * recipient can use this receipt since it requires a recipient's key for decoding.
 */
public final class Receipt {
    private static final String TAG = Receipt.class.getName();
    static final int CONFIRMATION_NUMBER_LENGTH = 32;
    private final MobileCoinAPI.Receipt receiptBuf;
    private final RistrettoPublic publicKey;

    private Receipt(@NonNull byte[] serializedBytes) throws SerializationException {
        try {
            receiptBuf = MobileCoinAPI.Receipt.parseFrom(serializedBytes);
            byte[] keyBytes = receiptBuf.getPublicKey().getData().toByteArray();
            publicKey = RistrettoPublic.fromBytes(keyBytes);
        } catch (InvalidProtocolBufferException ex) {
            throw new SerializationException(ex.getLocalizedMessage());
        }
    }

    private Receipt(@NonNull MobileCoinAPI.Receipt receipt) throws SerializationException {
        this(receipt.toByteArray());
    }

    Receipt(
            @NonNull RistrettoPublic txOutPublicKey,
            @NonNull byte[] confirmationHash,
            @NonNull MaskedAmount maskedAmount,
            @NonNull UnsignedLong tombstoneBlockIndex
    ) {
        MobileCoinAPI.Receipt.Builder receiptBuilder = MobileCoinAPI.Receipt.newBuilder();
        receiptBuilder.setTombstoneBlock(tombstoneBlockIndex.longValue());
        receiptBuilder.setMaskedAmount(maskedAmount.toProtoBufObject());
        receiptBuilder.setConfirmation(MobileCoinAPI.TxOutConfirmationNumber.newBuilder()
                .setHash(ByteString.copyFrom(confirmationHash))
                .build());
        receiptBuilder.setPublicKey(txOutPublicKey.toProtoBufObject());
        receiptBuf = receiptBuilder.build();
        publicKey = txOutPublicKey;
    }

    /**
     * Construct a {@link Receipt} object from the serialized bytes.
     *
     * @param serializedBytes a binary representation of the {@link Receipt} object (see {@link
     *                        Receipt#toByteArray()})
     * @throws SerializationException if serialized bytes parameter is invalid
     */
    @NonNull
    public static Receipt fromBytes(@NonNull byte[] serializedBytes) throws SerializationException {
        return new Receipt(serializedBytes);
    }

    @NonNull
    static Receipt fromProtoBufObject(@NonNull MobileCoinAPI.Receipt receipt) throws SerializationException {
        return new Receipt(receipt);
    }

    /**
     * Verify the receipt's confirmation hash and the encoded amount
     */
    public boolean isValid(@NonNull AccountKey accountKey) {
        byte[] confirmationHash = receiptBuf.getConfirmation().getHash().toByteArray();
        byte[] txPubKeyBytes = receiptBuf.getPublicKey().getData().toByteArray();
        try {
            // validate the amount
            // if the amount is invalid the AmountDecoderException is thrown
            getAmount(accountKey);

            // validate the confirmation hash
            RistrettoPublic pubkey = RistrettoPublic.fromBytes(txPubKeyBytes);
            return isConfirmationHashValid(accountKey,
                    confirmationHash,
                    pubkey);
        } catch (SerializationException exception) {
            Logger.w(TAG, "Unable to deserialize tx pubkey", exception);
        } catch (AmountDecoderException exception) {
            Logger.w(TAG, "Receipt's amount is invalid", exception);
        }
        return false;
    }

    public boolean isConfirmationHashValid(@NonNull AccountKey accountKey,
                                           @NonNull byte[] confirmationHash,
                                           @NonNull RistrettoPublic publicKey) {
        try {
            return is_confirmation_valid(confirmationHash,
                    publicKey,
                    accountKey.getViewKey()
            );
        } catch (Exception exception) {
            Logger.w(TAG, "Unable to validate confirmation hash", exception);
            return false;
        }
    }

    /**
     * @return {@link BigInteger} decoded receipt amount
     */
    @NonNull
    public BigInteger getAmount(@NonNull AccountKey accountKey) throws AmountDecoderException {
        if (!receiptBuf.hasMaskedAmount()) {
            throw new AmountDecoderException("Receipt does not contain an encoded amount");
        }
        MobileCoinAPI.MaskedAmount protoMaskedAmount = receiptBuf.getMaskedAmount();
        byte[] commitment = protoMaskedAmount.getCommitment().getData().toByteArray();
        long maskedValue = protoMaskedAmount.getMaskedValue();
        MaskedAmount maskedAmount = new MaskedAmount(
                commitment,
                maskedValue
        );
        return maskedAmount.unmaskAmount(
                accountKey.getViewKey(),
                getPublicKey()
        );
    }

    /**
     * Returns a binary representation of this object
     */
    @NonNull
    public byte[] toByteArray() {
        return receiptBuf.toByteArray();
    }

    @NonNull
    MobileCoinAPI.Receipt toProtoBufObject() {
        return receiptBuf;
    }

    @NonNull
    public RistrettoPublic getPublicKey() {
        return publicKey;
    }

    @NonNull
    public UnsignedLong getTombstoneBlockIndex() {
        return UnsignedLong.fromLongBits(receiptBuf.getTombstoneBlock());
    }

    /**
     * Fetch the TxOut for this Receipt
     *
     * @param mobileCoinClient must correspond to the account the receipt is intended for
     * @return {@link OwnedTxOut} or null if the TxOut was not found
     */
    @Nullable
    public OwnedTxOut fetchOwnedTxOut(@NonNull MobileCoinClient mobileCoinClient) throws NetworkException,
            InvalidFogResponse, AttestationException, FogSyncException {
        return mobileCoinClient.getAccountActivity().getAllTxOuts().stream()
                .filter(txOut -> txOut.getPublicKey().equals(getPublicKey()))
                .findFirst()
                .orElse(null);
    }

    private native boolean is_confirmation_valid(
            @NonNull byte[] confirmationHash,
            @NonNull RistrettoPublic txPubKey,
            @NonNull RistrettoPrivate viewPrivateKey
    );

    public enum Status {
        UNKNOWN, RECEIVED, FAILED;

        private UnsignedLong blockIndex;

        synchronized Status atBlock(UnsignedLong receivedAt) {
            blockIndex = receivedAt;
            return this;
        }

        public synchronized UnsignedLong getBlockIndex() {
            return blockIndex;
        }
    }
}
