package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Contains data associated with a sender with payment intent memo.
 *
 * <p>The data has been validated, which means that we've verified that the correct sender
 * wrote the memo and that the data has not been corrupted.
 **///TODO: doc
public final class SenderWithPaymentIntentMemoData extends MemoData {

    @NonNull
    private final UnsignedLong paymentIntentId;

    /**
     * Creates a {@link SenderWithPaymentIntentMemoData} instance with all of the expected fields.
     * */// TODO: doc
    public static SenderWithPaymentIntentMemoData create(
            @NonNull AddressHash addressHash,
            @NonNull UnsignedLong paymentIntentId
    ) {
        return new SenderWithPaymentIntentMemoData(addressHash, paymentIntentId);
    }

    private SenderWithPaymentIntentMemoData(@NonNull AddressHash addressHash, @NonNull UnsignedLong paymentIntentId) {
        super(addressHash);
        this.paymentIntentId = paymentIntentId;
    }

    //TODO: doc
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
