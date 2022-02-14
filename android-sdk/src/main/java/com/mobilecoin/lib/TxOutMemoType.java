package com.mobilecoin.lib;

import android.icu.number.UnlocalizedNumberFormatter;
import com.mobilecoin.lib.util.Hex;
import java.util.Arrays;

/**
 * Corresponds to the different types of a TxOut memo as indicated
 * by the first two bytes of the MemoPayload.
 **/
public enum TxOutMemoType {
  // Corresponds to when the sender did not write a memo.
  NOT_SET(""),
  // Corresponds to the "UnusedMemo."
  UNUSED("0000"),
  // Corresponds to the "AuthenticatedSenderMemo".
  SENDER("0100"),
  // Corresponds to the "AuthenticatedSenderWithPaymentRequestIdMemo."
  SENDER_WITH_PAYMENT_REQUEST("0101"),
  // Corresponds to the "DestinationMemo."
  DESTINATION("0200"),
  // Corresponds to when the sender wrote a memo type that isn't understood by the client yet.
  UNKNOWN("----");

  private byte[] memoTypeBytes;

  TxOutMemoType(String memoTypeHexBytes) {
    this.memoTypeBytes = Hex.toByteArray(memoTypeHexBytes);
  }

  static TxOutMemoType fromBytes(byte[] memoTypeBytes) {
    if (memoTypeBytes.length != 2) {
      throw new IllegalArgumentException("Memo type bytes should be of length 2. Was: " + memoTypeBytes.length);
    }

    if (Arrays.equals(memoTypeBytes, UNUSED.memoTypeBytes)) {
      return UNUSED;
    }
    if (Arrays.equals(memoTypeBytes, SENDER.memoTypeBytes)) {
      return SENDER;
    }
    if (Arrays.equals(memoTypeBytes, DESTINATION.memoTypeBytes)) {
      return DESTINATION;
    }
    if (Arrays.equals(memoTypeBytes, SENDER_WITH_PAYMENT_REQUEST.memoTypeBytes)) {
      return SENDER_WITH_PAYMENT_REQUEST;
    }

    return UNKNOWN;
  }
}
