package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.math.BigInteger;

public class Amount implements Parcelable {

    private final BigInteger value;
    private final UnsignedLong tokenId;

    Amount(BigInteger value, UnsignedLong tokenId) {
        this.value = value;
        this.tokenId = tokenId;
    }

    @NonNull
    public BigInteger getValue() {
        return this.value;
    }

    @NonNull
    public UnsignedLong getTokenId() {
        return this.tokenId;
    }

    private Amount(@NonNull Parcel parcel) {
        value = (BigInteger)parcel.readSerializable();
        tokenId = parcel.readParcelable(UnsignedLong.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeSerializable(value);
        parcel.writeParcelable(tokenId, flags);
    }

    public static final Creator<Amount> CREATOR = new Creator<Amount>() {
        @Override
        public Amount createFromParcel(@NonNull Parcel parcel) {
            return new Amount(parcel);
        }

        @Override
        public Amount[] newArray(int length) {
            return new Amount[length];
        }
    };

}
