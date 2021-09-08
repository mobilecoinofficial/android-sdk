package com.mobilecoin.lib;

import com.mobilecoin.lib.util.Hex;
import java.util.Arrays;

/**
 * Corresponds to the different types of a TxOut memo as indicated
 * by the first two bytes of the MemoPayload.
 **/
public enum TxOutMemoType {
  // If the memo is not set or the MemoPayload cannot be parsed.
  UNKNOWN("0000"),
  // Corresponds to the "AuthenticatedSenderMemo".
  SENDER("0100"),
  // Corresponds to the "DestinationMemo".
  DESTINATION("0200");

  private byte[] memoTypeBytes;

  private TxOutMemoType(String memoTypeHexBytes) {
    this.memoTypeBytes = Hex.toByteArray(memoTypeHexBytes);
  }

  static TxOutMemoType fromBytes(byte[] memoTypeBytes) {
    if (memoTypeBytes.length != 2) {
      throw new IllegalArgumentException("Memo type bytes should be of length 2. Was: " + memoTypeBytes.length);
    }

    if (Arrays.equals(memoTypeBytes, SENDER.memoTypeBytes)) {
      return SENDER;
    }
    if (Arrays.equals(memoTypeBytes, DESTINATION.memoTypeBytes)) {
      return SENDER;
    }

    return UNKNOWN;
  }
}
