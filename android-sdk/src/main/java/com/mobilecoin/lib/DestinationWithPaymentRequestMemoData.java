package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Objects;

// TODO: doc
public class DestinationWithPaymentRequestMemoData extends MemoData {

    @NonNull
    private final UnsignedLong fee, totalOutlay, paymentRequestId;

    private final int numberOfRecipients;

    // TODO: doc
    public static DestinationWithPaymentRequestMemoData create(
            @NonNull final AddressHash addressHash,
            final int numberOfRecipients,
            @NonNull final UnsignedLong fee,
            @NonNull final UnsignedLong totalOutlay,
            @NonNull final UnsignedLong paymentRequestId
    ) {
        return new DestinationWithPaymentRequestMemoData(addressHash, numberOfRecipients, fee, totalOutlay, paymentRequestId);
    }

    private DestinationWithPaymentRequestMemoData(
            @NonNull final AddressHash addressHash,
            final int numberOfRecipients,
            @NonNull final UnsignedLong fee,
            @NonNull final UnsignedLong totalOutlay,
            @NonNull final UnsignedLong paymentRequestId
    ) {
        super(addressHash);
        this.numberOfRecipients = numberOfRecipients;
        this.fee = fee;
        this.totalOutlay = totalOutlay;
        this.paymentRequestId = paymentRequestId;
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
    public UnsignedLong getPaymentRequestId() {
        return paymentRequestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DestinationWithPaymentRequestMemoData that = (DestinationWithPaymentRequestMemoData) o;
        return numberOfRecipients == that.numberOfRecipients &&
                Objects.equals(addressHash, that.addressHash) &&
                Objects.equals(fee, that.fee) &&
                Objects.equals(totalOutlay, that.totalOutlay) &&
                Objects.equals(paymentRequestId, that.paymentRequestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressHash, numberOfRecipients, fee, totalOutlay, paymentRequestId);
    }

    private DestinationWithPaymentRequestMemoData(@NonNull Parcel parcel) {
        super(parcel.readParcelable(AddressHash.class.getClassLoader()));
        fee = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        totalOutlay = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        numberOfRecipients = parcel.readInt();
        paymentRequestId = parcel.readParcelable(UnsignedLong.class.getClassLoader());
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeParcelable(fee, flags);
        parcel.writeParcelable(totalOutlay, flags);
        parcel.writeInt(numberOfRecipients);
        parcel.writeParcelable(paymentRequestId, flags);
    }

    public static final Creator<DestinationWithPaymentRequestMemoData> CREATOR = new Creator<DestinationWithPaymentRequestMemoData>() {
        @Override
        public DestinationWithPaymentRequestMemoData createFromParcel(@NonNull Parcel parcel) {
            return new DestinationWithPaymentRequestMemoData(parcel);
        }

        @Override
        public DestinationWithPaymentRequestMemoData[] newArray(int length) {
            return new DestinationWithPaymentRequestMemoData[length];
        }
    };

}