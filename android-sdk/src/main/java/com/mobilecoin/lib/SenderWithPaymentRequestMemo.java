package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;

/**
 * Represents a sender memo with a payment request id, which corresponds to the
 * "AuthenticatedSenderWithPaymentRequestIdMemo" specification.
 **/
public final class SenderWithPaymentRequestMemo extends Native implements TxOutMemo {

  private static final String TAG = SenderWithPaymentRequestMemo.class.getSimpleName();

  private final RistrettoPrivate receiverSubaddressViewKey;
  private final RistrettoPublic txOutPublicKey;

  private SenderWithPaymentRequestMemoData senderWithPaymentRequestMemoData;
  /**
   * Creates a {@link SenderWithPaymentRequestMemo} from a decrypted memo data that hasn't been
   * validated yet.
   *
   * @param memoData - The 44 bytes that correspond to the memo payload.
   **/
  static SenderWithPaymentRequestMemo create(
      @NonNull RistrettoPrivate receiverSubaddressViewKey,
      @NonNull RistrettoPublic txOutPublicKey,
      @NonNull byte[] memoData
  ) {
    if (memoData.length != 44) {
      throw new IllegalArgumentException("Memo data byte array must have a length of 44. Instead, the length was: " + memoData.length);
    }
    return new SenderWithPaymentRequestMemo(receiverSubaddressViewKey, txOutPublicKey, memoData);
  }

  @Override
  public TxOutMemoType getTxOutMemoType() {
    return TxOutMemoType.SENDER_WITH_PAYMENT_REQUEST;
  }

  private SenderWithPaymentRequestMemo(
      @NonNull RistrettoPrivate receiverSubaddressViewKey,
      @NonNull RistrettoPublic txOutPublicKey,
      @NonNull byte[] memoData
  ) {
    this.receiverSubaddressViewKey = receiverSubaddressViewKey;
    this.txOutPublicKey = txOutPublicKey;
    try {
      init_jni_from_memo_data(memoData);
    } catch(Exception e) {
      IllegalArgumentException illegalArgumentException =
          new IllegalArgumentException("Failed to create a SenderWithPaymentRequestMemo", e);
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
   * Validates then retrieves the sender with payment request memo data.
   *
   * <p>Before calling this method, call {@link #getUnvalidatedAddressHash()} and see if the
   * {@link AddressHash} corresponds to a {@link PublicAddress} that is known by the user.
   * If the {@link PublicAddress} is not known by the user, then do not call this method because the
   * memo is automatically invalid.
   **/
  public SenderWithPaymentRequestMemoData getSenderWithPaymentRequestMemoData(
      @NonNull PublicAddress senderPublicAddress)
      throws InvalidTxOutMemoException {
    if (senderWithPaymentRequestMemoData != null) {
      return senderWithPaymentRequestMemoData;
    }

    if (!is_valid(
        senderPublicAddress,
        receiverSubaddressViewKey,
        txOutPublicKey
    )) {
      throw new InvalidTxOutMemoException("The sender memo is invalid.");
    }

    UnsignedLong paymentRequestId = UnsignedLong.fromLongBits(get_payment_request_id());
    senderWithPaymentRequestMemoData = SenderWithPaymentRequestMemoData.create(getAddressHash(), paymentRequestId);

    return senderWithPaymentRequestMemoData;
  }

  private native void init_jni_from_memo_data(byte[] memoData);

  // Returns true if the sender with payment request memo is valid.
  private native boolean is_valid(
      @NonNull PublicAddress senderPublicAddress,
      @NonNull RistrettoPrivate receiverSubaddressViewKey,
      @NonNull RistrettoPublic txOutPublicKey
  );

  private native byte[] get_address_hash_data();

  private native long get_payment_request_id();
}
