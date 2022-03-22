package com.mobilecoin.lib.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.UnsignedLong;

public class FogStatusException extends InvalidFogResponse {

    @Nullable
    private UnsignedLong fogViewIndex;

    @Nullable
    private UnsignedLong fogLedgerIndex;

    public FogStatusException(@Nullable String message) {
        super(message);
    }

    public FogStatusException(@NonNull Throwable throwable) {
        super(throwable);
    }

    public FogStatusException(@Nullable String message, @Nullable Throwable throwable) {
        super(message, throwable);
    }

    public FogStatusException(@NonNull UnsignedLong fogViewIndex, @NonNull UnsignedLong fogLedgerIndex) {
        super(String.format("Fog view and ledger block indices are out of sync. Try again later. " +
                "View index: %s, Ledger index: %s", fogViewIndex, fogLedgerIndex));
        this.fogViewIndex = fogViewIndex;
        this.fogLedgerIndex = fogLedgerIndex;
    }

    @Nullable
    public UnsignedLong getFogViewIndex() {
        return this.fogViewIndex;
    }

    @Nullable
    public UnsignedLong getFogLedgerIndex() {
        return this.fogLedgerIndex;
    }

}
