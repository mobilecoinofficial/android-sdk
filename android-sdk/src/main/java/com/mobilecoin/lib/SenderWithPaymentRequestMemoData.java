package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Contains data associated with a sender with payment request memo.
 *
 * <p>The data has been validated, which means that we've verified that the correct sender
 * wrote the memo and that the data has not been corrupted.
 **///TODO: doc
public final class SenderWithPaymentRequestMemoData extends MemoData {

  @NonNull
  private final UnsignedLong paymentRequestId;

  /**
   * Creates a {@link SenderWithPaymentRequestMemoData} instance with all of the expected fields.
   * */// TODO: doc
  public static SenderWithPaymentRequestMemoData create(
      @NonNull AddressHash addressHash,
      @NonNull UnsignedLong paymentRequestId
  ) {
    return new SenderWithPaymentRequestMemoData(addressHash, paymentRequestId);
  }

  private SenderWithPaymentRequestMemoData(@NonNull AddressHash addressHash, @NonNull UnsignedLong paymentRequestId) {
    super(addressHash);
    this.paymentRequestId = paymentRequestId;
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
    SenderWithPaymentRequestMemoData that = (SenderWithPaymentRequestMemoData) o;

    return Objects.equals(addressHash, that.addressHash) &&
        Objects.equals(paymentRequestId, that.paymentRequestId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(addressHash, paymentRequestId);
  }

  private SenderWithPaymentRequestMemoData(@NonNull Parcel parcel) {
    super(parcel.readParcelable(AddressHash.class.getClassLoader()));
    paymentRequestId = parcel.readParcelable(UnsignedLong.class.getClassLoader());
  }

  @Override
  public void writeToParcel(@NonNull Parcel parcel, int flags) {
    super.writeToParcel(parcel, flags);
    parcel.writeParcelable(paymentRequestId, flags);
  }

  public static final Creator<SenderWithPaymentRequestMemoData> CREATOR = new Creator<SenderWithPaymentRequestMemoData>() {
    @Override
    public SenderWithPaymentRequestMemoData createFromParcel(@NonNull Parcel parcel) {
      return new SenderWithPaymentRequestMemoData(parcel);
    }

    @Override
    public SenderWithPaymentRequestMemoData[] newArray(int length) {
      return new SenderWithPaymentRequestMemoData[length];
    }
  };

}
