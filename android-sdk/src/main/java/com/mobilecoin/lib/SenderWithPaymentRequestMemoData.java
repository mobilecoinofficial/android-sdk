package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Contains data associated with a {@link SenderWithPaymentRequestMemo}.
 *
 * The data has been validated, which means that the sender of the {@link SenderWithPaymentRequestMemo}
 * has been verified and that the data has not been corrupted.
 *
 * @see SenderWithPaymentRequestMemo
 * @see MemoData
 * @see AddressHash
 * @since 1.2.0
 */
public final class SenderWithPaymentRequestMemoData extends MemoData {

  @NonNull
  private final UnsignedLong paymentRequestId;

  /**
   * Creates a {@link SenderWithPaymentRequestMemoData} instance with all of the expected fields.
   */
  static SenderWithPaymentRequestMemoData create(
      @NonNull AddressHash addressHash,
      @NonNull UnsignedLong paymentRequestId
  ) {
    return new SenderWithPaymentRequestMemoData(addressHash, paymentRequestId);
  }

  private SenderWithPaymentRequestMemoData(@NonNull AddressHash addressHash, @NonNull UnsignedLong paymentRequestId) {
    super(addressHash);
    this.paymentRequestId = paymentRequestId;
  }

  /**
   * Gets the <strong>payment request</strong> ID stored in this {@link SenderWithPaymentRequestMemoData}
   *
   * For additional information about this field, see
   * {@link TxOutMemoBuilder#createSenderPaymentRequestAndDestinationRTHMemoBuilder(AccountKey, UnsignedLong)}.
   *
   * @return the ID of a <strong>payment request</strong>
   *
   * @see TxOutMemoBuilder#createSenderPaymentRequestAndDestinationRTHMemoBuilder(AccountKey, UnsignedLong)
   * @see SenderWithPaymentRequestMemo
   * @see DestinationWithPaymentRequestMemoData#getPaymentRequestId()
   * @since 1.2.0
   */
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
