package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Contains data associated with a sender with payment request memo.
 *
 * <p>The data has been validated, which means that we've verified that the correct sender
 * wrote the memo and that the data has not been corrupted.
 **/
public final class SenderWithPaymentRequestMemoData extends MemoData {

  @NonNull
  private final UnsignedLong paymentRequestId;

  /**
   * Creates a {@link SenderWithPaymentRequestMemoData} instance with all of the expected fields.
   * */
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

  @NonNull
  public UnsignedLong getPaymentRequestId() {
    return paymentRequestId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof SenderWithPaymentRequestMemoData) {
      SenderWithPaymentRequestMemoData that = (SenderWithPaymentRequestMemoData) o;
      return super.equals(that) &&
             Objects.equals(paymentRequestId, that.paymentRequestId);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), paymentRequestId);
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
