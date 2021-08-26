package com.mobilecoin.lib;

/**
 * Corresponds to the different types of a TxOut memo as indicated
 * by the first two bytes of the MemoPayload.
 **/
public enum TxOutMemoType {
  // If the memo is not set or the MemoPayload cannot be parsed.
  UNKNOWN,
  // Corresponds to the "AuthenticatedSenderMemo".
  SENDER,
  // Corresponds to the "DestinationMemo".
  DESTINATION,
}
