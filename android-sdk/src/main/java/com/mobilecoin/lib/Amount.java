package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mobilecoin.api.MobileCoinAPI;

import java.math.BigInteger;
import java.util.Objects;

public class Amount implements Parcelable, Comparable<Amount> {

    private final BigInteger value;
    private final UnsignedLong tokenId;

    /**
     * Create an amount with the specified value and token ID
     *
     * @param value   The value stored in this amount
     * @param tokenId The ID of the token that this amount represents
     */
    Amount(BigInteger value, UnsignedLong tokenId) {
        this.value = value;
        this.tokenId = tokenId;
    }

    /**
     * Adds this amount to another amount and returns the sum as another amount
     * If an attempting to add Amounts with different token IDs, an IllegalArgumentException is thrown
     *
     * @param addend The amount to add to this amount
     * @return a new Amount object with the same token ID and a value equal to the sum of the two Amount's values
     */
    @NonNull
    public Amount add(@NonNull Amount addend) {
        if(!this.tokenId.equals(addend.tokenId)) {
            throw new IllegalArgumentException("Unable to add amounts of different tokens");
        }
        return new Amount(this.getValue().add(addend.getValue()), this.tokenId);
    }

    /**
     * Subtracts subtrahend from this amount and returns the difference as another amount
     * If an attempting to subtract Amounts with different token IDs, an IllegalArgumentException is thrown
     *
     * @param subtrahend The amount to subtract from this amount
     * @return a new Amount object with the same token ID and a value equal to the difference of the two Amount's values
     */
    @NonNull
    public Amount subtract(@NonNull Amount subtrahend) {
        if(!this.tokenId.equals(subtrahend.tokenId)) {
            throw new IllegalArgumentException("Unable to subtract amounts of different tokens");
        }
        return new Amount(this.getValue().subtract(subtrahend.getValue()), this.tokenId);
    }

    /**
     * Multiplies this amount by another amount and returns the product as another amount
     * If an attempting to multiply Amounts with different token IDs, an IllegalArgumentException is thrown
     *
     * @param multiplier The amount to multiply by this amount
     * @return a new Amount object with the same token ID and a value equal to the product of the two Amount's values
     */
    @NonNull
    public Amount multiply(@NonNull Amount multiplier) {
        if(!this.tokenId.equals(multiplier.tokenId)) {
            throw new IllegalArgumentException("Unable to multiply amounts of different tokens");
        }
        return new Amount(this.getValue().multiply(multiplier.getValue()), this.tokenId);
    }

    /**
     * Divides this amount by another amount and returns the quotient as another amount
     * If an attempting to divide Amounts with different token IDs, an IllegalArgumentException is thrown
     *
     * @param divisor The amount divide this amount by
     * @return a new Amount object with the same token ID and a value equal to the quotient of the two Amount's values
     */
    @NonNull
    public Amount divide(@NonNull Amount divisor) {
        if(!this.tokenId.equals(divisor.tokenId)) {
            throw new IllegalArgumentException("Unable to divide amounts of different tokens");
        }
        return new Amount(this.getValue().divide(divisor.getValue()), this.tokenId);
    }

    /**
     * Gets the value of this Amount
     *
     * @return The value of this Amount
     */
    @NonNull
    public BigInteger getValue() {
        return this.value;
    }

    /**
     * Gets the token ID of this Amount
     * @return The token ID of this Amount
     */
    @NonNull
    public UnsignedLong getTokenId() {
        return this.tokenId;
    }

    /**
     * Compares this Amount to another Amount
     * If the specified Amount has a different token ID, an IllegalArgumentException is thrown
     *
     * @param o The Amount to compare with
     * @return -1 if this < o, 0 if this == o, 1 if this > o
     */
    @Override
    public int compareTo(@NonNull Amount o) {
        if(!this.tokenId.equals(o.tokenId)) {
            throw new IllegalArgumentException("Unable to compare amounts of different tokens");
        }
        return this.value.compareTo(o.getValue());
    }

    /**
     * Checks if this Amount is equal to another Object
     *
     * @param o The Object to compare to
     * @return true if the Objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Amount)) {
            return false;
        }
        Amount that = (Amount)o;
        return Objects.equals(this.value, that.value) &&
                Objects.equals(this.tokenId, that.tokenId);
    }

    /**
     * Hashes this Amount and returns a 32 bit hash code
     *
     * @return a 32 bit hash code of this Amount
     */
    @Override
    public int hashCode() {
        return Objects.hash((this.value.longValue() >> 1), this.tokenId);
    }

    /**
     * Generates a String representation of this Amount
     * The format of the output is [value] [token ID string]
     * @return
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(this.value.toString()).append(' ');
        if(
                this.tokenId.longValue() <= Integer.MAX_VALUE &&
                        this.tokenId.longValue() >= 0 &&
                        MobileCoinAPI.KnownTokenId.internalGetVerifier().isInRange((int)this.tokenId.longValue())
        ) {
            b.append(MobileCoinAPI.KnownTokenId.forNumber((int)this.tokenId.longValue()).name());
        }
        else {
            b.append("Unknown token(id:").append(this.tokenId).append(')');
        }
        return b.toString();
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
