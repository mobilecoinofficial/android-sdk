package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o instanceof MemoData) {
            MemoData that = (MemoData)o;
            return Objects.equals(this.addressHash, that.addressHash);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.addressHash);
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
