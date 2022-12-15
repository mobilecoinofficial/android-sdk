package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * This class represents a MobileCoin <a href="https://github.com/mobilecoinfoundation/mcips/blob/main/text/0031-transactions-with-contingent-inputs.md">Signed Contingent Input</a>.
 * A {@link SignedContingentInput} represents an {@link Amount} that a user has signed to be spent by another user.
 * The spending of the pre-signed {@link Amount} is contingent on another user supplying an {@link Amount}
 * of a different {@link TokenId} in exchange. This allows users to perform swaps of different tokens
 * safely and conveniently. There is no way for the other party to spend the provided {@link Amount} without
 * supplying the required {@link Amount} to the user that built the {@link SignedContingentInput}.
 * The user that creates a {@link SignedContingentInput} may also cancel it before it has been spent.
 * This is accomplished by spending the input that was previously signed by sending it back to the user that signed it.
 * In order for the transaction to be accepted, a transaction fee will be required.
 *
 * @see <a href="https://github.com/mobilecoinfoundation/mcips/blob/main/text/0031-transactions-with-contingent-inputs.md">Signed Contingent Inputs</a>
 * @see MobileCoinTransactionClient#createSignedContingentInput(Amount, Amount)
 * @see MobileCoinTransactionClient#createSignedContingentInput(Amount, Amount, PublicAddress)
 * @see MobileCoinTransactionClient#cancelSignedContingentInput(SignedContingentInput, Amount)
 * @see MobileCoinTransactionClient#prepareTransaction(SignedContingentInput, Amount)
 * @see MobileCoinTransactionClient#prepareTransaction(SignedContingentInput, Amount, Rng)
 * @since 4.0.0
 */
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

    /**
     * Returns the {@link Amount} that will go to the party that provides the required {@link Amount}
     * and fulfills this {@link SignedContingentInput}. This {@link Amount} (minus any {@link Transaction} fees)
     * can be kept by the fulfilling party and is provided by the builder of this {@link SignedContingentInput}.
     *
     * @return the {@link Amount} rewarded to the fulfilling party of this {@link SignedContingentInput}
     * @see SignedContingentInput#getRequiredAmount()
     * @see SignedContingentInput
     * @see MobileCoinTransactionClient#prepareTransaction(SignedContingentInput, Amount, Rng)
     * @see MobileCoinTransactionClient#prepareTransaction(SignedContingentInput, Amount)
     * @see MobileCoinTransactionClient#estimateTotalFee(Amount)
     * @see Amount
     * @since 4.0.0
     */
    @NonNull
    public Amount getRewardAmount() {
        if(null == cachedRewardAmount) {
            cachedRewardAmount = getPseudoOutputAmount().subtract(getChangeAmount());
        }
        return cachedRewardAmount;
    }

    /**
     * Returns the {@link Amount} that must be provided in order to spend this {@link SignedContingentInput}.
     * This {@link Amount} is provided by the fulfilling party and will be sent to the {@link PublicAddress}
     * optionally specified when building this {@link SignedContingentInput}
     * (see {@link MobileCoinTransactionClient#createSignedContingentInput(Amount, Amount, PublicAddress)}).
     * If not {@link PublicAddress} is specified, the {@link Amount} is sent back to the user who built
     * this {@link SignedContingentInput}.
     *
     * @return the {@link Amount} that must be provided in order to fulfill this {@link SignedContingentInput}
     * @see SignedContingentInput#getRewardAmount()
     * @see SignedContingentInput
     * @see MobileCoinTransactionClient#prepareTransaction(SignedContingentInput, Amount, Rng)
     * @see MobileCoinTransactionClient#prepareTransaction(SignedContingentInput, Amount)
     * @see Amount
     * @since 4.0.0
     */
    @NonNull
    public Amount getRequiredAmount() {
        if(null == cachedRequiredAmount) {
            cachedRequiredAmount = Arrays.stream(getRequiredOutputAmounts())
                    .filter(amount -> !amount.getTokenId().equals(getPseudoOutputAmount().getTokenId())).findFirst()
                    .orElse(new Amount(BigInteger.ZERO, getPseudoOutputAmount().getTokenId()));
        }
        return cachedRequiredAmount;
    }

    /**
     * Serializes this {@link SignedContingentInput} to a byte array. The resultant byte array can be transmitted
     * or shared with another client. A client receiving a serialized {@link SignedContingentInput} can recover the
     * object using {@link SignedContingentInput#fromByteArray(byte[])}.
     * @return a byte array containing all of the data in this {@link SignedContingentInput}
     * @throws SerializationException if an {@link Exception} is encountered during serialization
     * @see SignedContingentInput
     * @see SignedContingentInput#fromByteArray(byte[])
     * @since 4.0.0
     */
    @NonNull
    public byte[] toByteArray() throws SerializationException {
        try {
            return to_byte_array();
        } catch(Exception e) {
            Logger.e(TAG, e);
            throw new SerializationException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Deserializes a {@link SignedContingentInput} from the provided array of bytes. If the provided byte
     * array cannot be deserialized as a {@link SignedContingentInput}, a {@link SerializationException}
     * will be thrown.
     *
     * @param serializedBytes a byte array representing a serialized {@link SignedContingentInput}
     * @return a {@link SignedContingentInput} that is strictly equal to the one originally serialized
     * @throws SerializationException if the byte array does not represent a serialized {@link SignedContingentInput}
     * @see SignedContingentInput
     * @see SignedContingentInput#toByteArray()
     * @since 4.0.0
     */
    @NonNull
    public static SignedContingentInput fromByteArray(@NonNull final byte[] serializedBytes) throws SerializationException {
        return new SignedContingentInput(serializedBytes);
    }

    /**
     * Checks whether or not this {@link SignedContingentInput} is valid. A valid {@link SignedContingentInput}
     * is one that can be successfully spent using the MobileCoin mobile SDKs (so long as the contingent Amount is supplied).
     * Conversely, an invalid {@link SignedContingentInput} will not be able to be spent. This could be
     * due to the data becoming corrupted or being built using an incompatible client.
     *
     * @return true if this {@link SignedContingentInput} is valid, false otherwise
     * @see MobileCoinTransactionClient#prepareTransaction(SignedContingentInput, Amount)
     * @see MobileCoinTransactionClient#prepareTransaction(SignedContingentInput, Amount, Rng rng)
     * @since 4.0.0
     */
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
