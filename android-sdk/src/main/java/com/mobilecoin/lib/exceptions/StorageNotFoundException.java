package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public class StorageNotFoundException extends MobileCoinException {

  public StorageNotFoundException(@Nullable String message) {
    super(message);
  }

  public StorageNotFoundException(@Nullable String message, @Nullable Throwable throwable) {
    super(message, throwable);
  }
}
