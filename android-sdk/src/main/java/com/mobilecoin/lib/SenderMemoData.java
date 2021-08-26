package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import java.util.Objects;

/**
 * Contains data associated with a destination memo.
 *
 * <p>The data has been validated, which means that we've verified that the correct sender
 * wrote the memo and that the data has not been corrupted.
 **/
public final class SenderMemoData {

  private final AddressHash addressHash;

  /** Creates a {@link SenderMemoData} instance with all of the expected fields. */
  public static SenderMemoData create(@NonNull AddressHash addressHash) {
    return new SenderMemoData(addressHash);
  }

  private SenderMemoData(AddressHash addressHash) {
    this.addressHash = addressHash;
  }

  public AddressHash getAddressHash() {
    return addressHash;
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
}
