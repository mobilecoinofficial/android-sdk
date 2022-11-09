package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Objects;

// TODO: doc
public class DestinationWithPaymentIntentMemoData extends MemoData {

    @NonNull
    private final UnsignedLong fee, totalOutlay, paymentIntentId;

    private final int numberOfRecipients;

    // TODO: doc
    public static DestinationWithPaymentIntentMemoData create(
            @NonNull final AddressHash addressHash,
            final int numberOfRecipients,
            @NonNull final UnsignedLong fee,
            @NonNull final UnsignedLong totalOutlay,
            @NonNull final UnsignedLong paymentRequestId
    ) {
        return new DestinationWithPaymentIntentMemoData(addressHash, numberOfRecipients, fee, totalOutlay, paymentRequestId);
    }

    private DestinationWithPaymentIntentMemoData(
            @NonNull final AddressHash addressHash,
            final int numberOfRecipients,
            @NonNull final UnsignedLong fee,
            @NonNull final UnsignedLong totalOutlay,
            @NonNull final UnsignedLong paymentIntentId
    ) {
        super(addressHash);
        this.numberOfRecipients = numberOfRecipients;
        this.fee = fee;
        this.totalOutlay = totalOutlay;
        this.paymentIntentId = paymentIntentId;
    }

    // TODO: doc
    public int getNumberOfRecipients() {
        return numberOfRecipients;
    }

    // TODO: doc
    @NonNull
    public UnsignedLong getFee() {
        return fee;
    }

    //TODO: doc
    @NonNull
    public UnsignedLong getTotalOutlay() {
        return totalOutlay;
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
        DestinationWithPaymentIntentMemoData that = (DestinationWithPaymentIntentMemoData) o;
        return numberOfRecipients == that.numberOfRecipients &&
                Objects.equals(addressHash, that.addressHash) &&
                Objects.equals(fee, that.fee) &&
                Objects.equals(totalOutlay, that.totalOutlay) &&
                Objects.equals(paymentIntentId, that.paymentIntentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressHash, numberOfRecipients, fee, totalOutlay, paymentIntentId);
    }

    private DestinationWithPaymentIntentMemoData(@NonNull Parcel parcel) {
        super(parcel.readParcelable(AddressHash.class.getClassLoader()));
        fee = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        totalOutlay = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        numberOfRecipients = parcel.readInt();
        paymentIntentId = parcel.readParcelable(UnsignedLong.class.getClassLoader());
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeParcelable(fee, flags);
        parcel.writeParcelable(totalOutlay, flags);
        parcel.writeInt(numberOfRecipients);
        parcel.writeParcelable(paymentIntentId, flags);
    }

    public static final Creator<DestinationWithPaymentIntentMemoData> CREATOR = new Creator<DestinationWithPaymentIntentMemoData>() {
        @Override
        public DestinationWithPaymentIntentMemoData createFromParcel(@NonNull Parcel parcel) {
            return new DestinationWithPaymentIntentMemoData(parcel);
        }

        @Override
        public DestinationWithPaymentIntentMemoData[] newArray(int length) {
            return new DestinationWithPaymentIntentMemoData[length];
        }
    };

}