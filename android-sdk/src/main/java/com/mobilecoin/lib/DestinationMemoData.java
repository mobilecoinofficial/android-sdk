package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import java.util.Objects;

/** Contains data associated with a destination memo. */
public final class DestinationMemoData {

  private final AddressHash addressHash;
  private final int numberOfRecipients;
  private final UnsignedLong fee;
  private final UnsignedLong totalOutlay;

  /** Creates a {@link DestinationMemoData} instance with all of the expected fields. */
  public static DestinationMemoData create(
      @NonNull AddressHash addressHash,
      @NonNull int numberOfRecipients,
      @NonNull UnsignedLong fee,
      @NonNull UnsignedLong totalOutlay) {
    return new DestinationMemoData(addressHash, numberOfRecipients, fee, totalOutlay);
  }

  private DestinationMemoData(
      AddressHash addressHash,
      int numberOfRecipients,
      UnsignedLong fee,
      UnsignedLong totalOutlay) {
    this.addressHash = addressHash;
    this.numberOfRecipients = numberOfRecipients;
    this.fee = fee;
    this.totalOutlay = totalOutlay;
  }

  public AddressHash getAddressHash() {
    return addressHash;
  }

  public int getNumberOfRecipients() {
    return numberOfRecipients;
  }

  public UnsignedLong getFee() {
    return fee;
  }

  public UnsignedLong getTotalOutlay() {
    return totalOutlay;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DestinationMemoData that = (DestinationMemoData) o;
    return numberOfRecipients == that.numberOfRecipients &&
        Objects.equals(addressHash, that.addressHash) &&
        Objects.equals(fee, that.fee) &&
        Objects.equals(totalOutlay, that.totalOutlay);
  }

  @Override
  public int hashCode() {
    return Objects.hash(addressHash, numberOfRecipients, fee, totalOutlay);
  }
}
