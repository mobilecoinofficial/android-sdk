package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;

import java.util.Objects;

// TODO: doc
public class DestinationWithPaymentIntentMemo extends TxOutMemo {

    private static final String TAG = DestinationWithPaymentIntentMemo.class.getSimpleName();

    private final DestinationWithPaymentIntentMemoData destinationWithPaymentIntentMemoData;

    /**
     * Creates a {@link DestinationWithPaymentIntentMemo} from decrypted memo data that hasn't been validated yet.
     *
     * @param accountKey
     * @param txOut
     * @param memoData   - The {@value TxOutMemo#TX_OUT_MEMO_DATA_SIZE_BYTES} bytes that correspond to the memo payload.
     **/
    static DestinationWithPaymentIntentMemo create(
            @NonNull final AccountKey accountKey,
            @NonNull final TxOut txOut,
            @NonNull final byte[] memoData) {
        if (memoData.length != TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES) {
            throw new IllegalArgumentException("Memo data byte array must have a length of " +
                    TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES + ". Instead, the length was: " + memoData.length);
        }
        return new DestinationWithPaymentIntentMemo(accountKey, txOut, memoData);
    }

    private DestinationWithPaymentIntentMemo(final AccountKey accountKey, final TxOut txOut, final byte[] memoData) {
        super(TxOutMemoType.DESTINATION_WITH_PAYMENT_INTENT);
        try {
            init_jni_from_memo_data(memoData);
        } catch (Exception e) {
            IllegalArgumentException illegalArgumentException =
                    new IllegalArgumentException("Failed to create an AccountKey", e);
            Util.logException(TAG, illegalArgumentException);
            throw illegalArgumentException;
        }
        validated = is_valid(accountKey, txOut);
        AddressHash addressHash = AddressHash.createAddressHash(get_address_hash_data());
        UnsignedLong fee = UnsignedLong.fromLongBits(get_fee());
        UnsignedLong totalOutlay = UnsignedLong.fromLongBits(get_total_outlay());
        UnsignedLong paymentIntentId = UnsignedLong.fromLongBits(get_payment_intent_id());
        destinationWithPaymentIntentMemoData = DestinationWithPaymentIntentMemoData.create(
                addressHash,
                get_number_of_recipients(),
                fee,
                totalOutlay,
                paymentIntentId
        );
    }

    // TODO: Doc
    public DestinationWithPaymentIntentMemoData getDestinationWithPaymentIntentMemoData() throws InvalidTxOutMemoException {
        if (!validated) {
            throw new InvalidTxOutMemoException("The DestinationWithPaymentIntentMemo is invalid.");
        }
        return destinationWithPaymentIntentMemoData;
    }

    private DestinationWithPaymentIntentMemo(@NonNull Parcel parcel) {
        super(TxOutMemoType.DESTINATION_WITH_PAYMENT_INTENT);
        destinationWithPaymentIntentMemoData = parcel.readParcelable(DestinationWithPaymentIntentMemoData.class.getClassLoader());
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeParcelable(destinationWithPaymentIntentMemoData, flags);
    }

    public static Creator<DestinationWithPaymentIntentMemo> CREATOR = new Creator<DestinationWithPaymentIntentMemo>() {
        @Override
        public DestinationWithPaymentIntentMemo createFromParcel(@NonNull Parcel parcel) {
            return new DestinationWithPaymentIntentMemo(parcel);
        }

        @Override
        public DestinationWithPaymentIntentMemo[] newArray(int length) {
            return new DestinationWithPaymentIntentMemo[length];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (o instanceof DestinationWithPaymentIntentMemo) {
            DestinationWithPaymentIntentMemo that = (DestinationWithPaymentIntentMemo) o;
            return Objects.equals(this.memoType, that.memoType) &&
                    Objects.equals(this.destinationWithPaymentIntentMemoData, that.destinationWithPaymentIntentMemoData);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoType, destinationWithPaymentIntentMemoData);
    }

    private native void init_jni_from_memo_data(byte[] memoData);

    private native boolean is_valid(@NonNull AccountKey accountKey, @NonNull TxOut txOut);

    private native byte[] get_address_hash_data();

    private native short get_number_of_recipients();

    private native long get_fee();

    private native long get_total_outlay();

    private native long get_payment_intent_id();

}
