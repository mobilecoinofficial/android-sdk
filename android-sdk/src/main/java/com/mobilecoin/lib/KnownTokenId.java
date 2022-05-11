package com.mobilecoin.lib;

import androidx.annotation.NonNull;

public enum KnownTokenId {

    MOB(UnsignedLong.ZERO);

    private KnownTokenId(@NonNull UnsignedLong id) {
        this.id = id;
    }

    @NonNull
    public UnsignedLong getId() {
        return this.id;
    }

    private UnsignedLong id;

}
