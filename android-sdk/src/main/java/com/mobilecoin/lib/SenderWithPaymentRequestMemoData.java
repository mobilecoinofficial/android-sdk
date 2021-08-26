package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import java.util.Objects;

/**
 * Contains data associated with a sender with payment request memo.
 *
 * <p>The data has been validated, which means that we've verified that the correct sender
 * wrote the memo and that the data has not been corrupted.
 **/
public final class SenderWithPaymentRequestMemoData {

  private final AddressHash addressHash;
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

  private SenderWithPaymentRequestMemoData(AddressHash addressHash, UnsignedLong paymentRequestId) {
    this.addressHash = addressHash;
    this.paymentRequestId = paymentRequestId;
  }

  public AddressHash getAddressHash() {
    return addressHash;
  }

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
}
