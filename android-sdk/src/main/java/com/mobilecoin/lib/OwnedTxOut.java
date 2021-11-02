// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.AmountDecoderException;
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
    private static final long serialVersionUID = 3L;

    //  The global index of this TxOut in the entire block chain.
    private final UnsignedLong txOutGlobalIndex;

    // The block index at which this TxOut appeared.
    private final UnsignedLong receivedBlockIndex;

    private final Date receivedBlockTimestamp;
    private Date spentBlockTimestamp;
    private UnsignedLong spentBlockIndex;

    private final BigInteger value;
    private final RistrettoPublic txOutPublicKey;
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

            MobileCoinAPI.TxOut txOutProto = MobileCoinAPI.TxOut.newBuilder()
                    .setAmount(amount.toProtoBufObject())
                    .setPublicKey(txOutPublicKeyProto)
                    .setTargetKey(txOutTargetKeyProto)
                    .build();
            // Calculated fields
            TxOut nativeTxOut = TxOut.fromProtoBufObject(txOutProto);
            keyImage = nativeTxOut.computeKeyImage(accountKey);
        } catch (SerializationException | AmountDecoderException | TransactionBuilderException e) {
            IllegalArgumentException illegalArgumentException =
                    new IllegalArgumentException("Unable to decode the TxOutRecord", e);
            Util.logException(TAG, illegalArgumentException);
            throw illegalArgumentException;
        }
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

    /**
     * Creates an OwnedTxOut from the provided parcel
     * @param parcel The parcel that contains na OwnedTxOut
     */
    private OwnedTxOut(Parcel parcel) throws SerializationException {
        txOutGlobalIndex = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        receivedBlockIndex = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        receivedBlockTimestamp = (Date)parcel.readSerializable();
        spentBlockTimestamp = (Date)parcel.readSerializable();
        spentBlockIndex = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        value = (BigInteger)parcel.readSerializable();
        txOutPublicKey = RistrettoPublic.fromBytes(parcel.createByteArray());
        keyImage = parcel.createByteArray();
        keyImageHash = parcel.readInt();
    }

    /**
     * Writes this object to the provided parcel
     * @param parcel The parcel to write the object to
     * @param flags The flags describing the contents of this object
     */
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(txOutGlobalIndex, flags);
        parcel.writeParcelable(receivedBlockIndex, flags);
        parcel.writeSerializable(receivedBlockTimestamp);
        parcel.writeSerializable(spentBlockTimestamp);
        parcel.writeParcelable(spentBlockIndex, flags);
        parcel.writeSerializable(value);
        parcel.writeByteArray(txOutPublicKey.getKeyBytes());
        parcel.writeByteArray(keyImage);
        parcel.writeInt(keyImageHash);
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
        public OwnedTxOut createFromParcel(Parcel parcel) {
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
