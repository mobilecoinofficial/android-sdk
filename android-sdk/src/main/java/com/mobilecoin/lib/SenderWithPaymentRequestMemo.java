package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;

import java.util.Objects;

/**
 * Represents a sender memo with a payment request id, which corresponds to the
 * "AuthenticatedSenderWithPaymentRequestIdMemo" specification.
 **/
public final class SenderWithPaymentRequestMemo extends TxOutMemo {

  private static final String TAG = SenderWithPaymentRequestMemo.class.getSimpleName();

  private final RistrettoPublic txOutPublicKey;
  private final SenderWithPaymentRequestMemoData senderWithPaymentRequestMemoData;
  /**
   * Creates a {@link SenderWithPaymentRequestMemo} from a decrypted memo data that hasn't been
   * validated yet.
   *
   * @param memoData - The {@value TxOutMemo#TX_OUT_MEMO_DATA_SIZE_BYTES} bytes that correspond to the memo payload.
   **/
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
          new IllegalArgumentException("Failed to create a SenderWithPaymentRequestMemo", e);
      Util.logException(TAG, illegalArgumentException);
      throw illegalArgumentException;
    }
    UnsignedLong paymentRequestId = UnsignedLong.fromLongBits(get_payment_request_id());
    senderWithPaymentRequestMemoData = SenderWithPaymentRequestMemoData.create(getAddressHash(), paymentRequestId);
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
