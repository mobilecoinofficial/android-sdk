// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.api.MobileCoinAPI.EncryptedMemo;
import com.mobilecoin.lib.exceptions.AmountDecoderException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.util.Hex;
import fog_view.View;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A transaction output that belongs to a {@link AccountKey}
 */
public class OwnedTxOut implements Serializable {
    private final static String TAG = OwnedTxOut.class.getName();

    // Bump serial version and read/write code if fields change
    private static final long serialVersionUID = 3L;

    //  The global index of this TxOut in the entire block chain.
    private final UnsignedLong txOutGlobalIndex;
    private final byte[] decryptedMemoPayload;

    // The block index at which this TxOut appeared.
    private final UnsignedLong receivedBlockIndex;
    private final AccountKey accountKey;
    private final TxOut nativeTxOut;

    private final Date receivedBlockTimestamp;
    private Date spentBlockTimestamp;
    private UnsignedLong spentBlockIndex;

    private TxOutMemo cachedTxOutMemo;

    private final BigInteger value;
    private final RistrettoPublic txOutPublicKey;
    private final byte[] keyImage;
    private int keyImageHash;

    OwnedTxOut(
            @NonNull View.TxOutRecord txOutRecord,
            @NonNull AccountKey accountKey
    ) {
        try {
            this.accountKey = accountKey;
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
            long maskedValue = txOutRecord.getTxOutAmountMaskedValue();
            RistrettoPublic txOutSharedSecret =
                Util.getSharedSecret(accountKey.getViewKey(), txOutPublicKey);
            Amount amount = new Amount(txOutSharedSecret, maskedValue);
            value = amount.unmaskValue(
                    accountKey.getViewKey(),
                    txOutPublicKey
            );

            MobileCoinAPI.TxOut.Builder txOutProtoBuilder = MobileCoinAPI.TxOut.newBuilder()
                    .setAmount(amount.toProtoBufObject())
                    .setPublicKey(txOutPublicKeyProto)
                    .setTargetKey(txOutTargetKeyProto);
            if (!txOutRecord.getTxOutEMemoData().isEmpty()) {
                EncryptedMemo encryptedMemo = EncryptedMemo.newBuilder()
                    .setData(txOutRecord.getTxOutEMemoData()).build();
                txOutProtoBuilder.setEMemo(encryptedMemo);
            }
            // Calculated fields
            nativeTxOut = TxOut.fromProtoBufObject(txOutProtoBuilder.build());
            decryptedMemoPayload = nativeTxOut.decryptMemoPayload(accountKey);
            keyImage = nativeTxOut.computeKeyImage(accountKey);
        } catch (SerializationException | AmountDecoderException | TransactionBuilderException e) {
            IllegalArgumentException illegalArgumentException =
                    new IllegalArgumentException("Unable to decode the TxOutRecord", e);
            Util.logException(TAG, illegalArgumentException);
            throw illegalArgumentException;
        }
    }

    /** Retrieves the {@link TxOutMemo} for the given TxOut. */
    public TxOutMemo getTxOutMemo() {
        if (cachedTxOutMemo == null) {
            cachedTxOutMemo = TxOutMemoParser
                .parseTxOutMemo(decryptedMemoPayload, accountKey, nativeTxOut);
        }

        return cachedTxOutMemo;
    }

    /**
     * Returns the decoded value of the TxOut
     */
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
        return (spentBlockIndex != null) && (spentBlockIndex.compareTo(atIndex) <= 0);
    }

    synchronized void setSpent(
            @NonNull UnsignedLong spentBlockIndex,
            @Nullable Date spentBlockTimestamp
    ) {
        Logger.i(TAG, "Setting spent status", null,
                "spentBlockIndex:", spentBlockIndex,
                "spentBlockTimeStamp:", spentBlockTimestamp);
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
        return Objects.equals(txOutGlobalIndex, that.txOutGlobalIndex) &&
               Objects.equals(receivedBlockIndex, that.receivedBlockIndex) &&
               Objects.equals(spentBlockIndex, that.spentBlockIndex) &&
               Objects.equals(value, that.value) &&
               Objects.equals(txOutPublicKey, that.txOutPublicKey) &&
               Arrays.equals(keyImage, that.keyImage);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(txOutGlobalIndex, receivedBlockIndex,
                spentBlockIndex, value, txOutPublicKey);
        result = 31 * result + Arrays.hashCode(keyImage);
        return result;
    }
}
