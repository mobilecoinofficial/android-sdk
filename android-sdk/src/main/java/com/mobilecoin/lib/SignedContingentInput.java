package com.mobilecoin.lib;

import androidx.annotation.NonNull;

public class SignedContingentInput extends Native {

    private SignedContingentInput(long rustObj) {
        this.rustObj = rustObj;
    }

    @NonNull
    static SignedContingentInput fromJNI(long rustObj) {
        return new SignedContingentInput(rustObj);
    }

}
