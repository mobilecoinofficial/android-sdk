package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;

import java.util.Objects;

/**
 * This class represents a {@link SenderMemo} tied to a specific <strong>payment request</strong>.
 *
 * This should be interpreted the same as a {@link SenderMemo} but has one additional field, a <strong>payment request</strong> ID.
 * This memo is paired with a {@link DestinationWithPaymentRequestMemo} which the sender sends to themselves and contains the same <strong>payment request</strong> ID.
 *
 * @see SenderMemo
 * @see DestinationWithPaymentRequestMemo
 * @see SenderWithPaymentRequestMemoData
 * @see SenderWithPaymentRequestMemoData#getPaymentRequestId()
 * @see TxOutMemo
 * @since 1.2.0
 */
public final class SenderWithPaymentRequestMemo extends TxOutMemo {

  private static final String TAG = SenderWithPaymentRequestMemo.class.getSimpleName();

  private final RistrettoPublic txOutPublicKey;
  private final SenderWithPaymentRequestMemoData senderWithPaymentRequestMemoData;
  /**
   * Creates a {@link SenderWithPaymentRequestMemo} from a decrypted memo data that hasn't been
   * validated yet.
   *
   * @param memoData - The {@value TxOutMemo#TX_OUT_MEMO_DATA_SIZE_BYTES} bytes that correspond to the memo payload.
   */
  static SenderWithPaymentRequestMemo create(
      @NonNull RistrettoPublic txOutPublicKey,
      @NonNull byte[] memoData
  ) {
    if (memoData.length != TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES) {
      throw new IllegalArgumentException("Memo data byte array must have a length of " +
              TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES + ". Instead, the length was: " + memoData.length);
    }
    return new SenderWithPaymentRequestMemo(txOutPublicKey, memoData);
  }

  private SenderWithPaymentRequestMemo(
      @NonNull RistrettoPublic txOutPublicKey,
      @NonNull byte[] memoData
  ) {
    super(TxOutMemoType.SENDER_WITH_PAYMENT_REQUEST);
    this.txOutPublicKey = txOutPublicKey;
    try {
      init_jni_from_memo_data(memoData);
    } catch(Exception e) {
      IllegalArgumentException illegalArgumentException =
          new IllegalArgumentException("Failed to create SenderWithPaymentRequestMemo", e);
      Util.logException(TAG, illegalArgumentException);
      throw illegalArgumentException;
    }
    UnsignedLong paymentRequestId = UnsignedLong.fromLongBits(get_payment_request_id());
    senderWithPaymentRequestMemoData = SenderWithPaymentRequestMemoData.create(getAddressHash(), paymentRequestId);
  }


  /**
   * Returns the {@link AddressHash} stored in this {@link SenderWithPaymentRequestMemo} without validating it first.
   *
   * To get the validated {@link AddressHash}, use {@link SenderWithPaymentRequestMemoData#getAddressHash()}.
   *
   * It is recommended to compare the output of this method to {@link AddressHash}es of known {@link PublicAddress}.
   * If the output of this method matches any known {@link PublicAddress} {@link AddressHash}, the memo
   * can be validated using that {@link PublicAddress}.
   *
   * @return the {@link AddressHash} in this memo without validating it first
   *
   * @see AddressHash
   * @see SenderWithPaymentRequestMemoData
   * @see SenderWithPaymentRequestMemoData#getAddressHash()
   * @see SenderWithPaymentRequestMemo#getSenderWithPaymentRequestMemoData(PublicAddress, RistrettoPrivate)
   * @see SenderMemo
   * @see SenderMemo#getUnvalidatedAddressHash()
   * @since 1.2.0
   */
  public AddressHash getUnvalidatedAddressHash() {
    return getAddressHash();
  }

  private AddressHash getAddressHash() {
    byte[] addressHashData = get_address_hash_data();
    return AddressHash.createAddressHash(addressHashData);
  }

  /**
   * Returns the {@link SenderWithPaymentRequestMemoData} for this {@link SenderWithPaymentRequestMemo} if valid.
   *
   * If validation of the memo fails, an {@link InvalidTxOutMemoException} is thrown
   *
   * Before calling this method, call {@link SenderWithPaymentRequestMemo#getUnvalidatedAddressHash()} and see if the
   * {@link AddressHash} corresponds to a {@link PublicAddress} that is known by the user.
   * If the {@link PublicAddress} is not known by the user, then do not call this method because the
   * memo is automatically invalid.
   *
   * @return the {@link SenderWithPaymentRequestMemoData}, if valid
   * @throws InvalidTxOutMemoException if validation of the memo fails
   *
   * @see SenderWithPaymentRequestMemoData
   * @see MemoData
   * @see PublicAddress
   * @see AddressHash
   * @see SenderWithPaymentRequestMemo#getUnvalidatedAddressHash()
   * @see InvalidTxOutMemoException
   * @since 1.2.0
   */
  public SenderWithPaymentRequestMemoData getSenderWithPaymentRequestMemoData(
      @NonNull PublicAddress senderPublicAddress,
      @NonNull RistrettoPrivate receiverSubaddressViewKey) throws InvalidTxOutMemoException {
    if(!validated) {
      if (!(validated = is_valid(
              senderPublicAddress,
              receiverSubaddressViewKey,
              txOutPublicKey)
      )) {
        throw new InvalidTxOutMemoException("The sender memo is invalid.");
      }
    }
    return senderWithPaymentRequestMemoData;
  }

  /**
   * Returns the {@link SenderWithPaymentRequestMemoData} for this {@link SenderWithPaymentRequestMemo} without validating.
   *
   * There is no guarantee that the {@link AddressHash} in this memo was actually calculated from the
   * sender's {@link PublicAddress}. To validate the sender's {@link AddressHash}, use
   * {@link SenderWithPaymentRequestMemo#getSenderWithPaymentRequestMemoData(PublicAddress, RistrettoPrivate)}
   *
   * @return the {@link SenderWithPaymentRequestMemoData}
   *
   * @see SenderWithPaymentRequestMemoData
   * @see MemoData
   * @see PublicAddress
   * @see AddressHash
   * @since 4.0.0
   */
  public SenderWithPaymentRequestMemoData getUnvalidatedSenderWithPaymentRequestMemoData() {
    return senderWithPaymentRequestMemoData;
  }

  private SenderWithPaymentRequestMemo(@NonNull Parcel parcel) {
    super(TxOutMemoType.SENDER_WITH_PAYMENT_REQUEST);
    txOutPublicKey = parcel.readParcelable(RistrettoPublic.class.getClassLoader());
    senderWithPaymentRequestMemoData = parcel.readParcelable(SenderWithPaymentRequestMemoData.class.getClassLoader());
  }

  @Override
  public void writeToParcel(@NonNull Parcel parcel, int flags) {
    parcel.writeParcelable(txOutPublicKey, flags);
    parcel.writeParcelable(senderWithPaymentRequestMemoData, flags);
  }

  public static Creator<SenderWithPaymentRequestMemo> CREATOR = new Creator<SenderWithPaymentRequestMemo>() {
    @Override
    public SenderWithPaymentRequestMemo createFromParcel(@NonNull Parcel parcel) {
      return new SenderWithPaymentRequestMemo(parcel);
    }

    @Override
    public SenderWithPaymentRequestMemo[] newArray(int length) {
      return new SenderWithPaymentRequestMemo[length];
    }
  };

  @Override
  public boolean equals(Object o) {
    if(o instanceof SenderWithPaymentRequestMemo) {
      SenderWithPaymentRequestMemo that = (SenderWithPaymentRequestMemo)o;
      return Objects.equals(this.memoType, that.memoType) &&
             Objects.equals(this.txOutPublicKey, that.txOutPublicKey) &&
             Objects.equals(this.senderWithPaymentRequestMemoData, that.senderWithPaymentRequestMemoData);
    }
    return false;
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
