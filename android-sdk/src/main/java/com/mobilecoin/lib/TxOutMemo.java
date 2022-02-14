package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/** Represents a TxOut memo. */
public abstract class TxOutMemo extends Native implements Parcelable {

  @NonNull
  protected final TxOutMemoType memoType;
  protected transient boolean validated = false;

  protected TxOutMemo(@NonNull TxOutMemoType memoType) {
    this.memoType = memoType;
  }

  @NonNull
  public TxOutMemoType getTxOutMemoType() {
    return this.memoType;
  }

  @Override
  public void writeToParcel(@NonNull Parcel parcel, int flags) {
    parcel.writeParcelable(memoType, flags);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final int TX_OUT_MEMO_DATA_SIZE_BYTES = 64;
  public static final int TX_OUT_MEMO_TYPE_SIZE_BYTES = 2;

}
