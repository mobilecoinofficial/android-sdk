package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Arrays;

/**
 * Represents a merlin hash of a {@link PublicAddress}.
 *
 * <p>Used in the memo verification process for recoverable transaction history memos.
 *
 **/
public final class AddressHash implements Parcelable {

  private final byte[] hashData;

  /** Creates an {@link AddressHash} from hash data bytes. */
  public static AddressHash createAddressHash(byte[] hashData) {
    return new AddressHash(hashData);
  }

  private AddressHash(byte[] hashData) {
    this.hashData = hashData;
  }

  public byte[] getHashData() {
    return hashData;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AddressHash that = (AddressHash) o;
    return Arrays.equals(this.hashData, that.hashData);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(hashData);
  }

  @Override
  public String toString() {
    return "AddressHash: { " +
        "hashData= " + Arrays.toString(hashData) +
        "}";
  }

  private AddressHash(@NonNull Parcel parcel) {
    hashData = parcel.createByteArray();
  }

  @Override
  public void writeToParcel(@NonNull Parcel parcel, int flags) {
    parcel.writeByteArray(hashData);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<AddressHash> CREATOR = new Creator<AddressHash>() {
    @Override
    public AddressHash createFromParcel(@NonNull Parcel parcel) {
      return new AddressHash(parcel);
    }

    @Override
    public AddressHash[] newArray(int length) {
      return new AddressHash[length];
    }
  };

}
