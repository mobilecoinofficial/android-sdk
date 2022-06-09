package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;

import java.util.Arrays;

/** Helps parse TxOutMemo payloads. */
final class TxOutMemoParser {

  private TxOutMemoParser() {}

  /** Parses decrypted memo bytes into the appropriate memo object.
   *
   * @param decryptedMemoPayload the memo payload bytes that have been decrypted.
   * @param recipientAccountKey of the user who received the memo.
   **/
  public static TxOutMemo parseTxOutMemo(
          byte[] decryptedMemoPayload,
          AccountKey recipientAccountKey,
          TxOut nativeTxOut
  ) throws InvalidTxOutMemoException {
    if (decryptedMemoPayload.length == 0) {
      return new EmptyMemo(TxOutMemoType.NOT_SET);
    }

    byte[] memoType = Arrays.copyOfRange(
            decryptedMemoPayload,
            0,
            TxOutMemo.TX_OUT_MEMO_TYPE_SIZE_BYTES
    );
    byte[] memoData = Arrays.copyOfRange(
            decryptedMemoPayload,
            TxOutMemo.TX_OUT_MEMO_TYPE_SIZE_BYTES,
            decryptedMemoPayload.length
    );
    TxOutMemoType txOutMemoType = TxOutMemoType.fromBytes(memoType);

    switch (txOutMemoType) {
      case UNUSED:
        return new EmptyMemo(TxOutMemoType.UNUSED);
      case SENDER:
        return SenderMemo.create(nativeTxOut.getPublicKey(), memoData);
      case DESTINATION:
        return DestinationMemo.create(recipientAccountKey, nativeTxOut, memoData);
      case SENDER_WITH_PAYMENT_REQUEST:
        return SenderWithPaymentRequestMemo.create(nativeTxOut.getPublicKey(), memoData);
      case UNKNOWN:
        return new EmptyMemo(TxOutMemoType.UNKNOWN);
      default:
        throw new IllegalArgumentException(
            "Unexpected error when parsing TxOutMemoType. Shouldn't be reached.");
    }
  }
}
