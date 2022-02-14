package com.mobilecoin.lib;

import java.util.Arrays;

/**
 * Represents a merlin hash of a {@link PublicAddress}.
 *
 * <p>Used in the memo verification process for recoverable transaction history memos.
 *
 **/
public final class AddressHash {

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
}
