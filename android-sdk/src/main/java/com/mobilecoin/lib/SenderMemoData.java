package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;
import java.util.Objects;

/**
 * Contains data associated with a destination memo.
 *
 * <p>The data has been validated, which means that we've verified that the correct sender
 * wrote the memo and that the data has not been corrupted.
 **/
public final class SenderMemoData extends MemoData {

  /** Creates a {@link SenderMemoData} instance with all of the expected fields. */
  public static SenderMemoData create(@NonNull AddressHash addressHash) {
    return new SenderMemoData(addressHash);
  }

  private SenderMemoData(@NonNull AddressHash addressHash) {
    super(addressHash);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SenderMemoData that = (SenderMemoData) o;
    return Objects.equals(addressHash, that.addressHash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(addressHash);
  }

  private SenderMemoData(@NonNull Parcel parcel) {
    super(parcel.readParcelable(AddressHash.class.getClassLoader()));
  }

  @Override
  public void writeToParcel(@NonNull Parcel parcel, int flags) {
    super.writeToParcel(parcel, flags);
  }

  public static final Creator<SenderMemoData> CREATOR = new Creator<SenderMemoData>() {
    @Override
    public SenderMemoData createFromParcel(@NonNull Parcel parcel) {
      return new SenderMemoData(parcel);
    }

    @Override
    public SenderMemoData[] newArray(int length) {
      return new SenderMemoData[length];
    }
  };

}
