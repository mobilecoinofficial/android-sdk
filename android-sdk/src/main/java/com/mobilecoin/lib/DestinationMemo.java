package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;
import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;

import java.util.Objects;

/** Represents a destination memo, which corresponds to the "DestinationMemo" specification. */
public final class DestinationMemo extends TxOutMemo {

  private static final String TAG = SenderMemo.class.getSimpleName();

  private final DestinationMemoData destinationMemoData;

  /**
   * Creates a {@link DestinationMemo} from decrypted memo data that hasn't been validated yet.
   *
   * @param accountKey
   * @param txOut
   * @param memoData - The {@value TxOutMemo#TX_OUT_MEMO_DATA_SIZE_BYTES} bytes that correspond to the memo payload.
   **/
  static DestinationMemo create(
      @NonNull AccountKey accountKey,
      @NonNull TxOut txOut,
      @NonNull byte[] memoData) {
    if (memoData.length != TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES) {
      throw new IllegalArgumentException("Memo data byte array must have a length of " +
              TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES + ". Instead, the length was: " + memoData.length);
    }
    return new DestinationMemo(accountKey, txOut, memoData);
  }

  private DestinationMemo(AccountKey accountKey, TxOut txOut, byte[] memoData) {
    super(TxOutMemoType.DESTINATION);
    try {
      init_jni_from_memo_data(memoData);
    } catch(Exception e) {
      IllegalArgumentException illegalArgumentException =
          new IllegalArgumentException("Failed to create an AccountKey", e);
      Util.logException(TAG, illegalArgumentException);
      throw illegalArgumentException;
    }
    validated = is_valid(accountKey, txOut);
    AddressHash addressHash = AddressHash.createAddressHash(get_address_hash_data());
    UnsignedLong fee = UnsignedLong.fromLongBits(get_fee());
    UnsignedLong totalOutlay = UnsignedLong.fromLongBits(get_total_outlay());
    destinationMemoData = DestinationMemoData.create(
            addressHash,
            get_number_of_recipients(),
            fee,
            totalOutlay
    );
  }

  /** Retrieves the destination memo data. **/
  public DestinationMemoData getDestinationMemoData() throws InvalidTxOutMemoException {
    if(!validated) {
      throw new InvalidTxOutMemoException("The sender memo is invalid.");
    }
    return destinationMemoData;
  }

  private DestinationMemo(@NonNull Parcel parcel) {
    super(TxOutMemoType.DESTINATION);
    destinationMemoData = parcel.readParcelable(DestinationMemo.class.getClassLoader());
  }

  @Override
  public void writeToParcel(@NonNull Parcel parcel, int flags) {
    parcel.writeParcelable(destinationMemoData, flags);
  }

  public static Creator<DestinationMemo> CREATOR = new Creator<DestinationMemo>() {
    @Override
    public DestinationMemo createFromParcel(@NonNull Parcel parcel) {
      return new DestinationMemo(parcel);
    }

    @Override
    public DestinationMemo[] newArray(int length) {
      return new DestinationMemo[length];
    }
  };

  @Override
  public boolean equals(Object o) {
    if(o instanceof DestinationMemo) {
      DestinationMemo that = (DestinationMemo)o;
      return Objects.equals(this.memoType, that.memoType) &&
             Objects.equals(this.destinationMemoData, that.destinationMemoData);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(memoType, destinationMemoData);
  }

  private native void init_jni_from_memo_data(byte[] memoData);

  private native boolean is_valid(@NonNull AccountKey accountKey, @NonNull TxOut txOut);

  private native byte[] get_address_hash_data();

  private native short get_number_of_recipients();

  private native long get_fee();

  private native long get_total_outlay();

}
