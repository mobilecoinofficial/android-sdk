package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

public class EmptyMemo extends TxOutMemo {

    public EmptyMemo(TxOutMemoType memoType) {
        super(memoType);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof EmptyMemo) {
            EmptyMemo that = (EmptyMemo)o;
            return this.memoType == that.memoType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return memoType.hashCode();
    }

    private EmptyMemo(@NonNull Parcel parcel) {
        super(parcel.readParcelable(TxOutMemoType.class.getClassLoader()));
    }

    public static final Creator<EmptyMemo> CREATOR = new Creator<EmptyMemo>() {
        @Override
        public EmptyMemo createFromParcel(@NonNull Parcel parcel) {
            return new EmptyMemo(parcel);
        }

        @Override
        public EmptyMemo[] newArray(int length) {
            return new EmptyMemo[length];
        }
    };

}
