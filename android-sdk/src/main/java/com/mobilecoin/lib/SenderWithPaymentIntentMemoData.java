package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Contains data associated with a {@link SenderWithPaymentIntentMemo}.
 *
 * The data has been validated, which means that the sender of the {@link SenderWithPaymentIntentMemo}
 * has been verified and that the data has not been corrupted.
 *
 * @see SenderWithPaymentIntentMemo
 * @see MemoData
 * @see AddressHash
 * @since 2.0.0
 */
public final class SenderWithPaymentIntentMemoData extends MemoData {

    @NonNull
    private final UnsignedLong paymentIntentId;

    /**
     * Creates a {@link SenderWithPaymentIntentMemoData} instance with all of the expected fields.
     */
    static SenderWithPaymentIntentMemoData create(
            @NonNull AddressHash addressHash,
            @NonNull UnsignedLong paymentIntentId
    ) {
        return new SenderWithPaymentIntentMemoData(addressHash, paymentIntentId);
    }

    private SenderWithPaymentIntentMemoData(@NonNull AddressHash addressHash, @NonNull UnsignedLong paymentIntentId) {
        super(addressHash);
        this.paymentIntentId = paymentIntentId;
    }

    /**
     * Gets the <strong>payment intent</strong> ID stored in this {@link SenderWithPaymentIntentMemoData}
     *
     * For additional information about this field, see
     * {@link TxOutMemoBuilder#createSenderPaymentIntentAndDestinationRTHMemoBuilder(AccountKey, UnsignedLong)}.
     *
     * @return the ID of a <strong>payment intent</strong>
     *
     * @see TxOutMemoBuilder#createSenderPaymentIntentAndDestinationRTHMemoBuilder(AccountKey, UnsignedLong)
     * @see SenderWithPaymentIntentMemo
     * @see DestinationWithPaymentIntentMemoData#getPaymentIntentId()
     * @since 2.0.0
     */
    @NonNull
    public UnsignedLong getPaymentIntentId() {
        return paymentIntentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SenderWithPaymentIntentMemoData that = (SenderWithPaymentIntentMemoData) o;

        return Objects.equals(addressHash, that.addressHash) &&
                Objects.equals(paymentIntentId, that.paymentIntentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressHash, paymentIntentId);
    }

    private SenderWithPaymentIntentMemoData(@NonNull Parcel parcel) {
        super(parcel.readParcelable(AddressHash.class.getClassLoader()));
        paymentIntentId = parcel.readParcelable(UnsignedLong.class.getClassLoader());
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeParcelable(paymentIntentId, flags);
    }

    public static final Creator<SenderWithPaymentIntentMemoData> CREATOR = new Creator<SenderWithPaymentIntentMemoData>() {
        @Override
        public SenderWithPaymentIntentMemoData createFromParcel(@NonNull Parcel parcel) {
            return new SenderWithPaymentIntentMemoData(parcel);
        }

        @Override
        public SenderWithPaymentIntentMemoData[] newArray(int length) {
            return new SenderWithPaymentIntentMemoData[length];
        }
    };

}
