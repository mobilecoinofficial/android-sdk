// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.AmountDecoderException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import fog_view.View;

public class OwnedTxOut implements Serializable {
    private final static String TAG = OwnedTxOut.class.getName();

    // Bump serial version and read/write code if fields change
    private static final long serialVersionUID = 3L;

    //  The global index of this TxOut in the entire block chain.
    private final UnsignedLong txOutGlobalIndex;

    // The block index at which this TxOut appeared.
    private final UnsignedLong receivedBlockIndex;

    private final Date receivedBlockTimestamp;
    private Date spentBlockTimestamp;
    private UnsignedLong spentBlockIndex;
    private boolean isSpent;

    private final BigInteger value;
    private final RistrettoPublic txOutPublicKey;
    private final byte[] keyImage;
    private int keyImageHash;

    OwnedTxOut(
            @NonNull View.TxOutRecord txOutRecord,
            @NonNull AccountKey accountKey
    ) {
        Logger.i(TAG, "Initializing OwnedTxOut");
        try {
            txOutGlobalIndex = UnsignedLong.fromLongBits(txOutRecord.getTxOutGlobalIndex());
            long longTimestampSeconds = txOutRecord.getTimestamp();
            // when the timestamp is missing U64::MAX is returned
            UnsignedLong timestampSeconds = UnsignedLong.fromLongBits(longTimestampSeconds);
            if (!timestampSeconds.equals(UnsignedLong.MAX_VALUE)) {
                long longTimestampMillis = TimeUnit.SECONDS.toMillis(timestampSeconds.longValue());
                receivedBlockTimestamp = new Date(longTimestampMillis);
            } else {
                receivedBlockTimestamp = null;
            }
            receivedBlockIndex = UnsignedLong.fromLongBits(txOutRecord.getBlockIndex());
            MobileCoinAPI.CompressedRistretto txOutPublicKeyProto =
                    MobileCoinAPI.CompressedRistretto.newBuilder()
                            .setData(txOutRecord.getTxOutPublicKeyData())
                            .build();
            MobileCoinAPI.CompressedRistretto txOutTargetKeyProto =
                    MobileCoinAPI.CompressedRistretto.newBuilder()
                            .setData(txOutRecord.getTxOutTargetKeyData())
                            .build();
            txOutPublicKey = RistrettoPublic.fromProtoBufObject(txOutPublicKeyProto);
            byte[] amountCommitment = txOutRecord.getTxOutAmountCommitmentData().toByteArray();
            long maskedValue = txOutRecord.getTxOutAmountMaskedValue();
            Amount amount = new Amount(amountCommitment, maskedValue);
            value = amount.unmaskValue(
                    accountKey.getViewKey(),
                    txOutPublicKey
            );

            MobileCoinAPI.TxOut txOutProto = MobileCoinAPI.TxOut.newBuilder()
                    .setAmount(amount.toProtoBufObject())
                    .setPublicKey(txOutPublicKeyProto)
                    .setTargetKey(txOutTargetKeyProto)
                    .build();
            // Calculated fields
            TxOut nativeTxOut = TxOut.fromProtoBufObject(txOutProto);
            keyImage = nativeTxOut.computeKeyImage(accountKey);
        } catch (SerializationException | AmountDecoderException e) {
            IllegalArgumentException illegalArgumentException =
                    new IllegalArgumentException("Unable to decode the TxOutRecord", e);
            Util.logException(TAG, illegalArgumentException);
            throw illegalArgumentException;
        }
    }

    @NonNull
    public BigInteger getValue() {
        return value;
    }

    @NonNull
    public KeyImage getKeyImage() {
        return KeyImage.fromBytes(keyImage);
    }

    @NonNull
    public synchronized UnsignedLong getReceivedBlockIndex() {
        return receivedBlockIndex;
    }

    @Nullable
    public Date getReceivedBlockTimestamp() {
        return receivedBlockTimestamp;
    }

    @Nullable
    public synchronized UnsignedLong getSpentBlockIndex() {
        return spentBlockIndex;
    }

    @Nullable
    public synchronized Date getSpentBlockTimestamp() {
        return spentBlockTimestamp;
    }

    @NonNull
    public RistrettoPublic getPublicKey() {
        return txOutPublicKey;
    }

    public synchronized boolean isSpent(@NonNull UnsignedLong atIndex) {
        boolean isSpentCheck = isSpent && (spentBlockIndex.compareTo(atIndex) <= 0);
        Logger.i(TAG, "Checking if spent", null, isSpentCheck);
        return isSpentCheck;
    }

    synchronized void setSpent(
            @NonNull UnsignedLong spentBlockIndex,
            @Nullable Date spentBlockTimestamp
    ) {
        Logger.i(TAG, "Setting spent");
        this.isSpent = true;
        this.spentBlockIndex = spentBlockIndex;
        this.spentBlockTimestamp = spentBlockTimestamp;
    }

    @NonNull
    UnsignedLong getTxOutGlobalIndex() {
        return txOutGlobalIndex;
    }

    int getKeyImageHashCode() {
        if (keyImageHash == 0) {
            keyImageHash = Arrays.hashCode(keyImage);
        }
        return keyImageHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OwnedTxOut that = (OwnedTxOut) o;
        return isSpent == that.isSpent &&
                txOutGlobalIndex.equals(that.txOutGlobalIndex) &&
                receivedBlockIndex.equals(that.receivedBlockIndex) &&
                spentBlockIndex.equals(that.spentBlockIndex) &&
                value.equals(that.value) &&
                txOutPublicKey.equals(that.txOutPublicKey) &&
                Arrays.equals(keyImage, that.keyImage);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(txOutGlobalIndex, receivedBlockIndex,
                spentBlockIndex, isSpent, value,
                txOutPublicKey);
        result = 31 * result + Arrays.hashCode(keyImage);
        return result;
    }
}
