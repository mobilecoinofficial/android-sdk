package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public abstract class MemoData implements Parcelable {

    @NonNull
    protected final AddressHash addressHash;

    protected MemoData(@NonNull AddressHash addressHash) {
        this.addressHash = addressHash;
    }

    @NonNull
    public AddressHash getAddressHash() {
        return this.addressHash;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeParcelable(addressHash, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
