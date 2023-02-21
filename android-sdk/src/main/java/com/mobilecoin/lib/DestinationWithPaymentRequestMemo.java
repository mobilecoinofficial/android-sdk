package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;

import java.util.Objects;

/***
 * This class represents a {@link DestinationMemo} tied to a specific <strong>payment request</strong>.
 *
 * This should be interpreted the same as a {@link DestinationMemo} but has one additional field, a <strong>payment request</strong> ID.
 * This memo is paired with a {@link SenderWithPaymentRequestMemo} which is sent to the recipient and contains the same <strong>payment request</strong> ID.
 *
 * @see DestinationMemo
 * @see SenderWithPaymentRequestMemo
 * @see DestinationWithPaymentRequestMemoData
 * @see TxOutMemo
 * @since 4.0.0
 */
public final class DestinationWithPaymentRequestMemo extends TxOutMemo {

    private static final String TAG = DestinationWithPaymentRequestMemo.class.getSimpleName();

    private final DestinationWithPaymentRequestMemoData destinationWithPaymentRequestMemoData;

    /**
     * Creates a {@link DestinationWithPaymentRequestMemo} from decrypted memo data that hasn't been validated yet.
     *
     * @param accountKey
     * @param txOut
     * @param memoData   - The {@value TxOutMemo#TX_OUT_MEMO_DATA_SIZE_BYTES} bytes that correspond to the memo payload.
     **/
    static DestinationWithPaymentRequestMemo create(
            @NonNull final AccountKey accountKey,
            @NonNull final TxOut txOut,
            @NonNull final byte[] memoData) {
        if (memoData.length != TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES) {
            throw new IllegalArgumentException("Memo data byte array must have a length of " +
                    TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES + ". Instead, the length was: " + memoData.length);
        }
        return new DestinationWithPaymentRequestMemo(accountKey, txOut, memoData);
    }

    private DestinationWithPaymentRequestMemo(final AccountKey accountKey, final TxOut txOut, final byte[] memoData) {
        super(TxOutMemoType.DESTINATION_WITH_PAYMENT_REQUEST);
        try {
            init_jni_from_memo_data(memoData);
        } catch (Exception e) {
            IllegalArgumentException illegalArgumentException =
                    new IllegalArgumentException("Failed to create DestinationWithPaymentRequestMemo", e);
            Util.logException(TAG, illegalArgumentException);
            throw illegalArgumentException;
        }
        validated = is_valid(accountKey, txOut);
        AddressHash addressHash = AddressHash.createAddressHash(get_address_hash_data());
        UnsignedLong fee = UnsignedLong.fromLongBits(get_fee());
        UnsignedLong totalOutlay = UnsignedLong.fromLongBits(get_total_outlay());
        UnsignedLong paymentRequestId = UnsignedLong.fromLongBits(get_payment_request_id());
        destinationWithPaymentRequestMemoData = DestinationWithPaymentRequestMemoData.create(
                addressHash,
                get_number_of_recipients(),
                fee,
                totalOutlay,
                paymentRequestId
        );
    }

    /**
     * Returns the {@link DestinationWithPaymentRequestMemoData} for this {@link DestinationWithPaymentRequestMemo} if valid.
     *
     * If validation of the memo fails, an {@link InvalidTxOutMemoException} is thrown
     *
     * @return the {@link DestinationWithPaymentRequestMemoData}, if valid
     * @throws InvalidTxOutMemoException if validation of the memo fails
     *
     * @see DestinationWithPaymentRequestMemoData
     * @see MemoData
     * @see InvalidTxOutMemoException
     * @since 4.0.0
     */
    public DestinationWithPaymentRequestMemoData getDestinationWithPaymentRequestMemoData() throws InvalidTxOutMemoException {
        if (!validated) {
            throw new InvalidTxOutMemoException("The DestinationWithPaymentRequestMemo is invalid.");
        }
        return destinationWithPaymentRequestMemoData;
    }

    private DestinationWithPaymentRequestMemo(@NonNull Parcel parcel) {
        super(TxOutMemoType.DESTINATION_WITH_PAYMENT_REQUEST);
        destinationWithPaymentRequestMemoData = parcel.readParcelable(DestinationWithPaymentRequestMemo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeParcelable(destinationWithPaymentRequestMemoData, flags);
    }

    public static Creator<DestinationWithPaymentRequestMemo> CREATOR = new Creator<DestinationWithPaymentRequestMemo>() {
        @Override
        public DestinationWithPaymentRequestMemo createFromParcel(@NonNull Parcel parcel) {
            return new DestinationWithPaymentRequestMemo(parcel);
        }

        @Override
        public DestinationWithPaymentRequestMemo[] newArray(int length) {
            return new DestinationWithPaymentRequestMemo[length];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (o instanceof DestinationWithPaymentRequestMemo) {
            DestinationWithPaymentRequestMemo that = (DestinationWithPaymentRequestMemo) o;
            return Objects.equals(this.memoType, that.memoType) &&
                    Objects.equals(this.destinationWithPaymentRequestMemoData, that.destinationWithPaymentRequestMemoData);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoType, destinationWithPaymentRequestMemoData);
    }

    private native void init_jni_from_memo_data(byte[] memoData);

    private native boolean is_valid(@NonNull AccountKey accountKey, @NonNull TxOut txOut);

    private native byte[] get_address_hash_data();

    private native short get_number_of_recipients();

    private native long get_fee();

    private native long get_total_outlay();

    private native long get_payment_request_id();

}
