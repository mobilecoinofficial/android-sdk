package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;

/** Represents a sender memo, which corresponds to the "AuthenticatedSenderMemo" specificatoin. */
public final class SenderMemo extends Native implements TxOutMemo {

  private static final String TAG = SenderMemo.class.getSimpleName();

  private final RistrettoPrivate receiverViewKey;
  private final RistrettoPublic txOutPublicKey;

  private SenderMemoData senderMemoData;

  /**
   * Creates a {@link SenderMemo} from a decrypted memo data that hasn't been validated yet.
   *
   * @param memoData - The 44 bytes that correspond to the memo payload.
   **/
  static SenderMemo create(
      @NonNull RistrettoPrivate receiverViewKey,
      @NonNull RistrettoPublic txOutPublicKey,
      @NonNull byte[] memoData
  ) {
    if (memoData.length != 44) {
      throw new IllegalArgumentException("Memo data byte array must have a lenght of 44. Instead, the length was: " + memoData.length);
    }
    return new SenderMemo(receiverViewKey, txOutPublicKey, memoData);
  }

  @Override
  public TxOutMemoType getTxOutMemoType() {
    return TxOutMemoType.SENDER;
  }

  private SenderMemo(
      @NonNull RistrettoPrivate receiverViewKey,
      @NonNull RistrettoPublic txOutPublicKey,
      @NonNull byte[] memoData
  ) {
    this.receiverViewKey = receiverViewKey;
    this.txOutPublicKey = txOutPublicKey;
    try {
      init_jni_from_memo_data(memoData);
    } catch(Exception e) {
      IllegalArgumentException illegalArgumentException =
          new IllegalArgumentException("Failed to create an AccountKey", e);
      Util.logException(TAG, illegalArgumentException);
      throw illegalArgumentException;
    }
  }


  /**
   * Retrieves the {@link AddressHash} that hasn't been validated yet.
   *
   * <p>This is used to see if the user who wrote the sender memo is known by the current user.
   * */
  public AddressHash getUnvalidatedAddressHash() {
    return getAddressHash();
  }

  private AddressHash getAddressHash() {
    byte[] addressHashData = get_address_hash_data();
    return AddressHash.createAddressHash(addressHashData);
  }

  /**
   * Validates then retrieves the sender memo data.
   *
   * <p>Before calling this method, call {@link #getUnvalidatedAddressHash()} and see if the
   * {@link AddressHash} corresponds to a {@link PublicAddress} that is known by the user.
   * If the {@link PublicAddress} is not known by the user, then do not call this method because the
   * memo is automatically invalid.
   **/
  public SenderMemoData getSenderMemoData(@NonNull PublicAddress senderPublicAddress)
      throws InvalidTxOutMemoException {
    if (senderMemoData != null) {
      return senderMemoData;
    }

    if (!is_valid(
        senderPublicAddress,
        receiverViewKey,
        txOutPublicKey
    )) {
      throw new InvalidTxOutMemoException("The sender memo is invalid.");
    }

    senderMemoData = SenderMemoData.create(getAddressHash());
    return senderMemoData;
  }

  private native void init_jni_from_memo_data(byte[] memoData);

  // Returns true if the sender memo is valid.
  private native boolean is_valid(
      @NonNull PublicAddress senderPublicAddress,
      @NonNull RistrettoPrivate receiverViewKey,
      @NonNull RistrettoPublic txOutPublicKey
  );

  private native byte[] get_address_hash_data();
}
