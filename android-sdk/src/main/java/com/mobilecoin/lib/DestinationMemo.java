package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;

/** Represents a destination memo, which corresponds to the "DestinationMemo" specification. */
public final class DestinationMemo extends Native implements TxOutMemo {

  private static final String TAG = SenderMemo.class.getSimpleName();

  private final AccountKey accountKey;
  private final TxOut txOut;

  private DestinationMemoData destinationMemoData;

  /**
   * Creates a {@link DestinationMemo} from decrypted memo data that hasn't been validated yet.
   *
   * @param accountKey
   * @param txOut
   * @param memoData - The 44 bytes that correspond to the memo payload.
   **/
  static DestinationMemo create(
      @NonNull AccountKey accountKey,
      @NonNull TxOut txOut,
      @NonNull byte[] memoData) {
    if (memoData.length != 44) {
      throw new IllegalArgumentException("Memo data byte array must have a lenght of 44. Instead, the length was: " + memoData.length);
    }
    return new DestinationMemo(accountKey, txOut, memoData);
  }

  private DestinationMemo(AccountKey accountKey, TxOut txOut, byte[] memoData) {
    this.accountKey = accountKey;
    this.txOut = txOut;
    try {
      init_jni_from_memo_data(memoData);
    } catch(Exception e) {
      IllegalArgumentException illegalArgumentException =
          new IllegalArgumentException("Failed to create an AccountKey", e);
      Util.logException(TAG, illegalArgumentException);
      throw illegalArgumentException;
    }
  }

  /** Validates then retrieves the destination memo data. **/
  public DestinationMemoData getDestinationMemoData() throws InvalidTxOutMemoException {
    if (destinationMemoData != null) {
      return destinationMemoData;
    }

    if (!is_valid(accountKey, txOut)) {
      throw new InvalidTxOutMemoException("The sender memo is invalid.");
    }

    AddressHash addressHash = AddressHash.createAddressHash(get_address_hash_data());
    UnsignedLong fee = UnsignedLong.fromLongBits(get_fee());
    UnsignedLong totalOutlay = UnsignedLong.fromLongBits(get_total_outlay());
    destinationMemoData = DestinationMemoData.create(
        addressHash,
        get_number_of_recipients(),
        fee,
        totalOutlay
    );

    return destinationMemoData;
  }

  @Override
  public TxOutMemoType getTxOutMemoType() {
    return TxOutMemoType.DESTINATION;
  }

  private native void init_jni_from_memo_data(byte[] memoData);

  private native boolean is_valid(@NonNull AccountKey accountKey, @NonNull TxOut txOut);

  private native byte[] get_address_hash_data();

  private native short get_number_of_recipients();

  private native long get_fee();

  private native long get_total_outlay();

}
