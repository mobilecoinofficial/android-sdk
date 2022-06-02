package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mobilecoin.api.MobileCoinAPI;

import java.util.Objects;

public class TokenId implements Parcelable {

    private final UnsignedLong id;

    public static final TokenId MOB = new TokenId(UnsignedLong.ZERO);

    private TokenId(@NonNull UnsignedLong id) {
        this.id = id;
    }

    @NonNull
    public UnsignedLong getId() {
        return this.id;
    }

    @NonNull
    public String getName() {
        long longValue = id.longValue();
        if(longValue <= Integer.MAX_VALUE && longValue >= 0 &&
                MobileCoinAPI.KnownTokenId.internalGetVerifier().isInRange((int)longValue)) {
            return MobileCoinAPI.KnownTokenId.forNumber((int)longValue).name();
        }
        else {
            return "Unknown Token: " + id.toString();
        }
    }

    @NonNull
    public static TokenId from(@NonNull UnsignedLong id) {
        return new TokenId(id);
    }

    @NonNull
    @Override
    public String toString() {
        return this.getName();
    }

    public boolean equals(Object o) {
        if(this == o) return true;
        if(o instanceof TokenId) {
            TokenId that = (TokenId)o;
            return Objects.equals(this.id, that.id);
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeParcelable(id, flags);
    }

    private TokenId(@NonNull Parcel parcel) {
        id = parcel.readParcelable(UnsignedLong.class.getClassLoader());
    }

    public static final Creator<TokenId> CREATOR = new Creator<TokenId>() {
        @Override
        public TokenId createFromParcel(@NonNull Parcel parcel) {
            return new TokenId(parcel);
        }

        @Override
        public TokenId[] newArray(int length) {
            return new TokenId[length];
        }
    };

}
