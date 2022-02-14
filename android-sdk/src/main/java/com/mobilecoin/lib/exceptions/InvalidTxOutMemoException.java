package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

/** Thrown when a TxOut memo cannot be validated. */
public final class InvalidTxOutMemoException extends MobileCoinException {

  public InvalidTxOutMemoException(@Nullable String message) {
    super(message);
  }

  public InvalidTxOutMemoException(@Nullable String message, @Nullable Throwable throwable) {
    super(message, throwable);
  }
}
