package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import java.math.BigInteger;
import java.util.Arrays;

//TODO: doc
public class SignedContingentInput extends Native implements Parcelable {

    private Amount cachedRewardAmount;
    private Amount cachedChangeAmount;
    private Amount cachedRequiredAmount;

    private SignedContingentInput(long rustObj) {
        this.rustObj = rustObj;
    }

    private SignedContingentInput(@NonNull final byte[] serializedBytes) throws SerializationException {
        try {
            init_from_bytes(serializedBytes);
        } catch(Exception e) {
            Logger.e(TAG, e);
            throw new SerializationException(e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    static SignedContingentInput fromJNI(long rustObj) {
        return new SignedContingentInput(rustObj);
    }

    @NonNull
    Amount getChangeAmount() {
        if(null == cachedChangeAmount) {
            cachedChangeAmount = Arrays.stream(getRequiredOutputAmounts())
                    .filter(amount -> amount.getTokenId().equals(getPseudoOutputAmount().getTokenId())).findFirst()
                    .orElse(new Amount(BigInteger.ZERO, getPseudoOutputAmount().getTokenId()));
        }
        return cachedChangeAmount;
    }

    // TODO: doc
    @NonNull
    public Amount getRewardAmount() {
        if(null == cachedRewardAmount) {
            cachedRewardAmount = getPseudoOutputAmount().subtract(getChangeAmount());
        }
        return cachedRewardAmount;
    }

    // TODO: doc
    @NonNull
    public Amount getRequiredAmount() {
        if(null == cachedRequiredAmount) {
            cachedRequiredAmount = Arrays.stream(getRequiredOutputAmounts())
                    .filter(amount -> !amount.getTokenId().equals(getPseudoOutputAmount().getTokenId())).findFirst()
                    .orElse(new Amount(BigInteger.ZERO, getPseudoOutputAmount().getTokenId()));
        }
        return cachedRequiredAmount;
    }

    // TODO: doc
    @NonNull
    public byte[] toByteArray() throws SerializationException {
        try {
            return to_byte_array();
        } catch(Exception e) {
            Logger.e(TAG, e);
            throw new SerializationException(e.getLocalizedMessage(), e);
        }
    }

    // TODO: doc
    @NonNull
    public static SignedContingentInput fromByteArray(@NonNull final byte[] serializedBytes) throws SerializationException {
        return new SignedContingentInput(serializedBytes);
    }

    // TODO: doc
    public boolean isValid() {
        if(!is_valid()) return false;
        final Amount[] requiredOutputAmounts = getRequiredOutputAmounts();
        final int numAmounts = requiredOutputAmounts.length;
        if((numAmounts > 0) && (numAmounts < 3)) {
            final TokenId changeTokenId = getChangeAmount().getTokenId();
            int numChangeOutputs = 0;
            int numRequiredOutputs = 0;
            for (Amount requiredAmount : requiredOutputAmounts) {
                if (requiredAmount.getTokenId().equals(changeTokenId)) {
                    numChangeOutputs += 1;
                } else {
                    numRequiredOutputs += 1;
                }
            }
            return ((numRequiredOutputs == 1) && (numChangeOutputs <= 1));
        }
        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    @NonNull
    Amount[] getRequiredOutputAmounts() {
        return get_required_output_amounts();
    }

    @NonNull
    Amount getPseudoOutputAmount() {
        return get_pseudo_output_amount();
    }

    @NonNull
    TxOut[] getRing() throws SerializationException {
        final byte[][] ringBytes = get_ring_bytes();
        final TxOut[] ring = new TxOut[ringBytes.length];

        for(int i = 0; i < ring.length; i++) {
            ring[i] = TxOut.fromBytes(ringBytes[i]);
        }

        return ring;

    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null) return false;
        if(o instanceof SignedContingentInput) {
            SignedContingentInput that = (SignedContingentInput)o;
            try {
                return Arrays.equals(this.toByteArray(), that.toByteArray());
            } catch(Exception e) {
                Logger.e(TAG, e);
                return false;
            }
        }
        return false;
    }

    @NonNull
    private native Amount[] get_required_output_amounts();

    @NonNull
    private native Amount get_pseudo_output_amount();

    @NonNull
    private native byte[][] get_ring_bytes();

    @NonNull
    private native byte[] to_byte_array();

    private native void init_from_bytes(@NonNull byte[] bytes);

    private native boolean is_valid();

    private native void finalize_jni();

    private static final String TAG = SignedContingentInput.class.getName();

    protected SignedContingentInput(@NonNull Parcel parcel) throws SerializationException {
        try {
            init_from_bytes(parcel.createByteArray());
        } catch(Exception e) {
            throw new SerializationException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        try {
            parcel.writeByteArray(toByteArray());
        } catch (SerializationException e) {
            Logger.e(TAG, e);
        }
    }

    public static final Creator<SignedContingentInput> CREATOR = new Creator<SignedContingentInput>() {
        @Override
        public SignedContingentInput createFromParcel(@NonNull Parcel parcel) {
            try {
                return new SignedContingentInput(parcel);
            } catch(SerializationException e) {
                Logger.e(TAG, e);
                return null;
            }

        }

        @Override
        public SignedContingentInput[] newArray(int length) {
            return new SignedContingentInput[length];
        }
    };

    public enum CancelationResult {

        SUCCESS,
        FAILED_ALREADY_SPENT,
        FAILED_UNOWNED_TX_OUT,
        FAILED_INVALID,
        FAILED_UNKNOWN

    }

}
