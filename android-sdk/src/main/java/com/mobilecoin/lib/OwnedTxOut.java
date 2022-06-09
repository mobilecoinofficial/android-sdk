// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.api.MobileCoinAPI.EncryptedMemo;
import com.mobilecoin.lib.exceptions.AmountDecoderException;
import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;
import com.mobilecoin.lib.log.Logger;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import fog_view.View;

/**
 * A transaction output that belongs to a {@link AccountKey}
 */
public class OwnedTxOut implements Parcelable {
    private final static String TAG = OwnedTxOut.class.getName();

    // Bump serial version and read/write code if fields change
    private static final long serialVersionUID = 4L;

    //  The global index of this TxOut in the entire block chain.
    private final UnsignedLong txOutGlobalIndex;

    // The block index at which this TxOut appeared.
    private final UnsignedLong receivedBlockIndex;

    private final Date receivedBlockTimestamp;
    private Date spentBlockTimestamp;
    private UnsignedLong spentBlockIndex;

    private final TxOutMemo cachedTxOutMemo;

    private final Amount amount;
    private final RistrettoPublic txOutPublicKey;
    private final RistrettoPublic txOutTargetKey;
    private final byte[] keyImage;
    private int keyImageHash;

    OwnedTxOut(
            @NonNull View.TxOutRecord txOutRecord,
            @NonNull AccountKey accountKey
    ) {
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
            txOutPublicKey = RistrettoPublic.fromProtoBufObject(txOutPublicKeyProto);
            MobileCoinAPI.CompressedRistretto txOutTargetKeyProto =
                    MobileCoinAPI.CompressedRistretto.newBuilder()
                            .setData(txOutRecord.getTxOutTargetKeyData())
                            .build();
            txOutTargetKey = RistrettoPublic.fromProtoBufObject(txOutTargetKeyProto);
            RistrettoPublic txOutSharedSecret = Util.getSharedSecret(accountKey.getViewKey(), txOutPublicKey);
            long maskedValue = txOutRecord.getTxOutAmountMaskedValue();
            byte maskedTokenId[] = txOutRecord.getTxOutAmountMaskedTokenId().toByteArray();
            MaskedAmount maskedAmount = new MaskedAmount(txOutSharedSecret, maskedValue, maskedTokenId);
            amount = maskedAmount.unmaskAmount(
                    accountKey.getViewKey(),
                    txOutPublicKey
            );

            MobileCoinAPI.TxOut.Builder txOutProtoBuilder = MobileCoinAPI.TxOut.newBuilder()
                    .setMaskedAmount(maskedAmount.toProtoBufObject())
                    .setPublicKey(txOutPublicKeyProto)
                    .setTargetKey(txOutTargetKeyProto);
            if (!txOutRecord.getTxOutEMemoData().isEmpty()) {
                EncryptedMemo encryptedMemo = EncryptedMemo.newBuilder()
                    .setData(txOutRecord.getTxOutEMemoData()).build();
                txOutProtoBuilder.setEMemo(encryptedMemo);
            }

            // Calculated fields
            TxOut nativeTxOut = TxOut.fromProtoBufObject(txOutProtoBuilder.build());
            byte decryptedMemoPayload[] = nativeTxOut.decryptMemoPayload(accountKey);
            keyImage = nativeTxOut.computeKeyImage(accountKey);
            cachedTxOutMemo = TxOutMemoParser
                    .parseTxOutMemo(decryptedMemoPayload, accountKey, nativeTxOut);
        } catch (SerializationException | AmountDecoderException | TransactionBuilderException | InvalidTxOutMemoException e) {
            IllegalArgumentException illegalArgumentException =
                    new IllegalArgumentException("Unable to decode the TxOutRecord", e);
            Util.logException(TAG, illegalArgumentException);
            throw illegalArgumentException;
        }
    }

    /** Retrieves the {@link TxOutMemo} for the given TxOut. */
    @NonNull
    public TxOutMemo getTxOutMemo() {
        return cachedTxOutMemo;
    }

    /**
     * @return The value of this TxOut
     * @deprecated Deprecated as of 1.2.0. Please use {@link OwnedTxOut#getAmount()}.
     * @see Amount
     * @see TokenId
     */
    @Deprecated
    @NonNull
    public BigInteger getValue() {
        return amount.getValue();
    }

    /**
     * @return The token ID of this TxOut
     * @deprecated Deprecated as of 1.2.0. Please use {@link OwnedTxOut#getAmount()}.
     * @see Amount
     * @see TokenId
     */
    @Deprecated
    @NonNull
    public TokenId getTokenId() {
        return amount.getTokenId();
    }

    /**
     * @return The amount of this TxOut
     */
    @NonNull Amount getAmount() {
        return amount;
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

    @NonNull
    public RistrettoPublic getTargetKey() {
        return txOutTargetKey;
    }

    @NonNull
    public RistrettoPublic getSharedSecret(AccountKey accountKey) throws TransactionBuilderException {
        return Util.getSharedSecret(accountKey.getViewKey(), txOutPublicKey);
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
        return Objects.equals(this.txOutGlobalIndex, that.txOutGlobalIndex) &&
               Objects.equals(this.receivedBlockIndex, that.receivedBlockIndex) &&
               Objects.equals(this.receivedBlockTimestamp, that.receivedBlockTimestamp) &&
               Objects.equals(this.spentBlockTimestamp, that.spentBlockTimestamp) &&
               Objects.equals(this.spentBlockIndex, that.spentBlockIndex) &&
               Objects.equals(this.amount, that.amount) &&
               Objects.equals(this.txOutPublicKey, that.txOutPublicKey) &&
               Objects.equals(this.txOutTargetKey, that.txOutTargetKey) &&
               Arrays.equals(this.keyImage, that.keyImage) &&
               this.keyImageHash == that.keyImageHash &&
               Objects.equals(this.cachedTxOutMemo, that.cachedTxOutMemo);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(txOutGlobalIndex, receivedBlockIndex,
                receivedBlockTimestamp, spentBlockTimestamp, spentBlockIndex, amount, txOutPublicKey,
                txOutTargetKey, Arrays.hashCode(keyImage), keyImageHash, cachedTxOutMemo);
        return result;
    }

    /**
     * Creates an OwnedTxOut from the provided parcel
     * @param parcel The parcel that contains an OwnedTxOut
     */
    private OwnedTxOut(@NonNull Parcel parcel) throws SerializationException {
        txOutGlobalIndex = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        receivedBlockIndex = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        receivedBlockTimestamp = (Date)parcel.readSerializable();
        spentBlockTimestamp = (Date)parcel.readSerializable();
        spentBlockIndex = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        amount = parcel.readParcelable(Amount.class.getClassLoader());
        txOutPublicKey = RistrettoPublic.fromBytes(parcel.createByteArray());
        txOutTargetKey = RistrettoPublic.fromBytes(parcel.createByteArray());
        keyImage = parcel.createByteArray();
        keyImageHash = parcel.readInt();
        cachedTxOutMemo = parcel.readParcelable(TxOutMemo.class.getClassLoader());
    }

    /**
     * Writes this object to the provided parcel
     * @param parcel The parcel to write the object to
     * @param flags The flags describing the contents of this object
     */
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeParcelable(txOutGlobalIndex, flags);
        parcel.writeParcelable(receivedBlockIndex, flags);
        parcel.writeSerializable(receivedBlockTimestamp);
        parcel.writeSerializable(spentBlockTimestamp);
        parcel.writeParcelable(spentBlockIndex, flags);
        parcel.writeParcelable(amount, flags);
        parcel.writeByteArray(txOutPublicKey.getKeyBytes());
        parcel.writeByteArray(txOutTargetKey.getKeyBytes());
        parcel.writeByteArray(keyImage);
        parcel.writeInt(keyImageHash);
        parcel.writeParcelable(cachedTxOutMemo, flags);
    }

    /**
     * @return The flags needed to write and read this object to or from a parcel
     */
    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OwnedTxOut> CREATOR = new Creator<OwnedTxOut>() {
        /**
         * Create OwnedTxOut from the provided Parcel
         * @param parcel The parcel containing an OwnedTxOut
         * @return The OwnedTxOut contained in the provided Parcel
         */
        @Override
        public OwnedTxOut createFromParcel(@NonNull Parcel parcel) {
            try {
                return new OwnedTxOut(parcel);
            } catch(SerializationException e) {
                Logger.e(OwnedTxOut.class.getSimpleName(), "Deserialization of OwnedTxOut failed.", e);
                return null;
            }
        }

        /**
         * Used by Creator to deserialize an array of OwnedTxOut
         * @param length
         * @return
         */
        @Override
        public OwnedTxOut[] newArray(int length) {
            return new OwnedTxOut[length];
        }
    };

}
