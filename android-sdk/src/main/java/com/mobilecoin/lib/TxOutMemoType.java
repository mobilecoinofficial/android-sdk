package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.util.Hex;

import java.util.Arrays;

/**
 * Corresponds to the different types of a TxOut memo as indicated
 * by the first two bytes of the MemoPayload.
 **/
public enum TxOutMemoType implements Parcelable {
  // Corresponds to when the sender did not write a memo.
  NOT_SET(""),
  // Corresponds to the "UnusedMemo."
  UNUSED("0000"),
  // Corresponds to the "BurnRedemptionMemo."
  //BURN_REDEPMTION_MEMO("0001"),
  // Corresponds to the "GiftCodeSenderMemo."
  //GIFT_CODE_SENDER_MEMO("0002"),
  // Corresponds to the "AuthenticatedSenderMemo."
  SENDER("0100"),
  // Corresponds to the "AuthenticatedSenderWithPaymentRequestIdMemo."
  SENDER_WITH_PAYMENT_REQUEST("0101"),
  // Corresponds to the "AuthenticatedSenderWithPaymentIntentIdMemo."
  SENDER_WITH_PAYMENT_INTENT("0102"),
  // Corresponds to the "DestinationMemo."
  DESTINATION("0200"),
  // Corresponds to the "GiftCodeFundingMemo."
  //GIFT_CODE_FUNDING_MEMO("0201"),
  // Corresponds to the "GiftCodeCancellationMemo."
  //GIFT_CODE_CANCELLATION_MEMO("0202"),
  // Corresponds to the "DestinationWithPaymentRequestMemo."
  DESTINATION_WITH_PAYMENT_REQUEST("0203"),
  // Corresponds to the "DestinationWithPaymentIntentMemo."
  DESTINATION_WITH_PAYMENT_INTENT("0204"),
  // Corresponds to when the sender wrote a memo type that isn't understood by the client yet.
  UNKNOWN("----");

  private final byte[] memoTypeBytes;

  TxOutMemoType(String memoTypeHexBytes) {
    this.memoTypeBytes = Hex.toByteArray(memoTypeHexBytes);
  }

  static TxOutMemoType fromBytes(byte[] memoTypeBytes) {
    if(memoTypeBytes.length == 0) {
      return NOT_SET;
    }
    else if (memoTypeBytes.length != TxOutMemo.TX_OUT_MEMO_TYPE_SIZE_BYTES) {
      throw new IllegalArgumentException("Memo type bytes should be of length " +
              TxOutMemo.TX_OUT_MEMO_TYPE_SIZE_BYTES + ". Was: " + memoTypeBytes.length);
    }

    for(TxOutMemoType memoType : TxOutMemoType.values()) {
      if(Arrays.equals(memoTypeBytes, memoType.memoTypeBytes)) {
        return memoType;
      }
    }

    return TxOutMemoType.UNKNOWN;

  }

  @Override
  public void writeToParcel(@NonNull Parcel parcel, int flags) {
    parcel.writeByteArray(memoTypeBytes);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<TxOutMemoType> CREATOR = new Creator<TxOutMemoType>() {
    @Override
    public TxOutMemoType createFromParcel(@NonNull Parcel parcel) {
      return TxOutMemoType.fromBytes(parcel.createByteArray());
    }

    @Override
    public TxOutMemoType[] newArray(int length) {
      return new TxOutMemoType[length];
    }
  };

}
