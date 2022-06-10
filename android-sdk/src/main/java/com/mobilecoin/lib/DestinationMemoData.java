package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Objects;

/** Contains data associated with a destination memo. */
public final class DestinationMemoData extends MemoData {

  @NonNull
  private final UnsignedLong fee, totalOutlay;

  private final int numberOfRecipients;

  /** Creates a {@link DestinationMemoData} instance with all of the expected fields. */
  public static DestinationMemoData create(
      @NonNull AddressHash addressHash,
      int numberOfRecipients,
      @NonNull UnsignedLong fee,
      @NonNull UnsignedLong totalOutlay) {
    return new DestinationMemoData(addressHash, numberOfRecipients, fee, totalOutlay);
  }

  private DestinationMemoData(
      @NonNull AddressHash addressHash,
      int numberOfRecipients,
      @NonNull UnsignedLong fee,
      @NonNull UnsignedLong totalOutlay) {
    super(addressHash);
    this.numberOfRecipients = numberOfRecipients;
    this.fee = fee;
    this.totalOutlay = totalOutlay;
  }

  public int getNumberOfRecipients() {
    return numberOfRecipients;
  }

  @NonNull
  public UnsignedLong getFee() {
    return fee;
  }

  @NonNull
  public UnsignedLong getTotalOutlay() {
    return totalOutlay;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if(o instanceof DestinationMemoData) {
      DestinationMemoData that = (DestinationMemoData)o;
      return super.equals(that) &&
             numberOfRecipients == that.numberOfRecipients &&
             Objects.equals(fee, that.fee) &&
             Objects.equals(totalOutlay, that.totalOutlay);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), numberOfRecipients, fee, totalOutlay);
  }

  private DestinationMemoData(@NonNull Parcel parcel) {
    super(parcel.readParcelable(AddressHash.class.getClassLoader()));
    fee = parcel.readParcelable(UnsignedLong.class.getClassLoader());
    totalOutlay = parcel.readParcelable(UnsignedLong.class.getClassLoader());
    numberOfRecipients = parcel.readInt();
  }

  @Override
  public void writeToParcel(@NonNull Parcel parcel, int flags) {
    super.writeToParcel(parcel, flags);
    parcel.writeParcelable(fee, flags);
    parcel.writeParcelable(totalOutlay, flags);
    parcel.writeInt(numberOfRecipients);
  }

  public static final Creator<DestinationMemoData> CREATOR = new Creator<DestinationMemoData>() {
    @Override
    public DestinationMemoData createFromParcel(@NonNull Parcel parcel) {
      return new DestinationMemoData(parcel);
    }

    @Override
    public DestinationMemoData[] newArray(int length) {
      return new DestinationMemoData[length];
    }
  };

}
