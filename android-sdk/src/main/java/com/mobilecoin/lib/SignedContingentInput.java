package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//TODO: doc
public class SignedContingentInput extends Native implements Parcelable {

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

    // TODO: doc
    @NonNull
    public Amount getIncomeAmount() {
        Amount grossIncome = getPseudoOutputAmount();
        // It is likely that if there is a required input with the same TokenId of the pseudo_output_amount, it is a change output
        // Still, it would be possible for required outputs of the same TokenId to be added, so we need to handle these cases
        Amount changeDeduction = Arrays.stream(getRequiredOutputAmounts())
                .filter(amount -> grossIncome.getTokenId().equals(amount.getTokenId()))
                .reduce(Amount::add).get();
        if(changeDeduction.compareTo(grossIncome) >= 0) {
            /*
            If the "change" amount is more than the income, there is no income.
            Instead of returning a negative income, the (income - change) will appear as a positive
            outlay in {@link SignedContingentInput#getTotalOutlays}
            */
            return new Amount(BigInteger.ZERO, grossIncome.getTokenId());
        }
        return grossIncome.subtract(changeDeduction);
    }

    // TODO: doc
    @NonNull
    public Map<TokenId, Amount> getTotalOutlays() {

        final Amount[] requiredAmounts = getRequiredOutputAmounts();
        Map<TokenId, Amount> totalOutlays = new HashMap<>();

        final Amount pseudoOutputAmount = getPseudoOutputAmount();
        Amount changeAmount = new Amount(BigInteger.ZERO, pseudoOutputAmount.getTokenId());

        for(Amount amount : requiredAmounts) {
            if(changeAmount.getTokenId().equals(amount.getTokenId())) {
                /*
                If there are any required amounts that have the same token ID as pseudoOutputAmount,
                they are likely change. There should be at most one. But these amounts may have been
                added as regular outputs. This doesn't really make sense, but valid SignedContingentInputs
                can be built like this.
                */
                changeAmount = changeAmount.add(amount);
            }
            else {
                //TODO: on API level 24, we can use getOrDefault to simplify the logic here
                Amount existingAmount = totalOutlays.get(amount.getTokenId());
                if(null == existingAmount) existingAmount = new Amount(BigInteger.ZERO, amount.getTokenId());
                totalOutlays.put(amount.getTokenId(), existingAmount.add(amount));
            }
        }

        /*
        This can happen if the party building a SignedContingentInput requests more than their entire
        payment as a required output. In such a case, the consumer of the SCI only spends tokens and does
        not receive anything.
         */
        if(changeAmount.compareTo(pseudoOutputAmount) > 0) {
            totalOutlays.put(changeAmount.getTokenId(), changeAmount);
        }

        return totalOutlays;

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
        return is_valid();
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

}
