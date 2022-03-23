package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;
import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;

import java.util.Objects;

/** Represents a sender memo, which corresponds to the "AuthenticatedSenderMemo" specificatoin. */
public final class SenderMemo extends TxOutMemo {

  private static final String TAG = SenderMemo.class.getSimpleName();

  private final RistrettoPublic txOutPublicKey;
  private final SenderMemoData senderMemoData;

  /**
   * Creates a {@link SenderMemo} from a decrypted memo data that hasn't been validated yet.
   *
   * @param memoData - The {@value TxOutMemo#TX_OUT_MEMO_DATA_SIZE_BYTES} bytes that correspond to the memo payload.
   **/
  static SenderMemo create(
      @NonNull RistrettoPublic txOutPublicKey,
      @NonNull byte[] memoData
  ) {
    if (memoData.length != TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES) {
      throw new IllegalArgumentException("Memo data byte array must have a length of " +
              TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES + ". Instead, the length was: " + memoData.length);
    }
    return new SenderMemo(txOutPublicKey, memoData);
  }

  private SenderMemo(
      @NonNull RistrettoPublic txOutPublicKey,
      @NonNull byte[] memoData
  ) {
    super(TxOutMemoType.SENDER);
    this.txOutPublicKey = txOutPublicKey;
    try {
      init_jni_from_memo_data(memoData);
    } catch(Exception e) {
      IllegalArgumentException illegalArgumentException =
          new IllegalArgumentException("Failed to create an AccountKey", e);
      Util.logException(TAG, illegalArgumentException);
      throw illegalArgumentException;
    }
    senderMemoData = SenderMemoData.create(getAddressHash());
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
  public SenderMemoData getSenderMemoData(
          @NonNull PublicAddress senderPublicAddress,
          @NonNull RistrettoPrivate receiverSubaddressViewKey) throws InvalidTxOutMemoException {
    if(!this.validated) {
      if (!(this.validated = is_valid(
              senderPublicAddress,
              receiverSubaddressViewKey,
              txOutPublicKey)
      )) {
        throw new InvalidTxOutMemoException("The sender memo is invalid.");
      }
    }
    return senderMemoData;
  }

  private SenderMemo(@NonNull Parcel parcel) {
    super(TxOutMemoType.SENDER);
    txOutPublicKey = parcel.readParcelable(RistrettoPublic.class.getClassLoader());
    senderMemoData = parcel.readParcelable(SenderMemoData.class.getClassLoader());
  }

  @Override
  public void writeToParcel(@NonNull Parcel parcel, int flags) {
    parcel.writeParcelable(txOutPublicKey, flags);
    parcel.writeParcelable(senderMemoData, flags);
  }

  public static final Creator<SenderMemo> CREATOR = new Creator<SenderMemo>() {
    @Override
    public SenderMemo createFromParcel(@NonNull Parcel parcel) {
      return new SenderMemo(parcel);
    }

    @Override
    public SenderMemo[] newArray(int length) {
      return new SenderMemo[length];
    }
  };

  @Override
  public boolean equals(Object o) {
    if(o instanceof SenderMemo) {
      SenderMemo that = (SenderMemo)o;
      return Objects.equals(this.memoType, that.memoType) &&
             Objects.equals(this.txOutPublicKey, that.txOutPublicKey) &&
             Objects.equals(this.senderMemoData, that.senderMemoData);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(memoType, txOutPublicKey, senderMemoData);
  }

  private native void init_jni_from_memo_data(byte[] memoData);

  // Returns true if the sender memo is valid.
  private native boolean is_valid(
      @NonNull PublicAddress senderPublicAddress,
      @NonNull RistrettoPrivate receiverSubaddressViewKey,
      @NonNull RistrettoPublic txOutPublicKey
  );

  private native byte[] get_address_hash_data();
}
