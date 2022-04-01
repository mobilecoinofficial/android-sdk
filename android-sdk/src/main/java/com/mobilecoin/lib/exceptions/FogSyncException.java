package com.mobilecoin.lib.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.UnsignedLong;

/**
 * This Exception is thrown when Fog is in an invalid state
 *
 * Fog View and Fog Ledger each have their own index indicating their highest processed block count.
 * This Exception is thrown if these values are out of sync by 10 blocks or if Fog is out of sync with Consensus by 10 blocks .
 */
public class FogSyncException extends MobileCoinException {

    public FogSyncException(@NonNull Throwable throwable) {
        super(throwable);
    }

    public FogSyncException(@Nullable String message) {
        super(message);
    }

    public FogSyncException(@Nullable String message, @Nullable Throwable exception) {
        super(message, exception);
    }

}
