package com.mobilecoin.lib;

import java.util.Arrays;

/** Helps parse TxOutMemo payloads. */
final class TxOutMemoParser {

  private TxOutMemoParser() {}

  /** Parses decrypted memo bytes into the appropriate memo object.
   *
   * @param decryptedMemoPayload the memo payload bytes that have been decrypted.
   * @param recipientAccountKey of the user who received the memo.
   **/
  public static TxOutMemo parseTxOutMemo(byte[] decryptedMemoPayload,
      AccountKey recipientAccountKey,
      TxOut nativeTxOut) {
    if (decryptedMemoPayload.length == 0) {
      return () -> TxOutMemoType.NOT_SET;
    }

    byte[] memoType = Arrays.copyOfRange(decryptedMemoPayload, 0, 2);
    byte[] memoData =
        Arrays.copyOfRange(decryptedMemoPayload, 2, decryptedMemoPayload.length);
    TxOutMemoType txOutMemoType = TxOutMemoType.fromBytes(memoType);

    switch (txOutMemoType) {
      case UNUSED:
        return () -> TxOutMemoType.UNUSED;
      case SENDER:
        return SenderMemo
            .create(recipientAccountKey.getSubAddressViewKey(), nativeTxOut.getPubKey(), memoData);
      case DESTINATION:
        return DestinationMemo.create(recipientAccountKey, nativeTxOut, memoData);
      case SENDER_WITH_PAYMENT_REQUEST:
        return SenderWithPaymentRequestMemo
            .create(recipientAccountKey.getSubAddressViewKey(), nativeTxOut.getPubKey(), memoData);
      case UNKNOWN:
        return () -> TxOutMemoType.UNKNOWN;
      default:
        throw new IllegalArgumentException(
            "Unexpected error when parsing TxOutMemoType. Shouldn't be reached.");
    }
  }
}
