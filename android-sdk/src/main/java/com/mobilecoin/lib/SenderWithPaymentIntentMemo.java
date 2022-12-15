package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;

import java.util.Objects;

/**
 * This class represents a {@link SenderMemo} tied to a specific <strong>payment intent</strong>.
 *
 * This should be interpreted the same as a {@link SenderMemo} but has one additional field, a <strong>payment intent</strong> ID.
 * This memo is paired with a {@link DestinationWithPaymentIntentMemo} which the sender sends to themselves and contains the same <strong>payment intent</strong> ID.
 *
 * @see SenderMemo
 * @see DestinationWithPaymentIntentMemo
 * @see SenderWithPaymentIntentMemoData
 * @see SenderWithPaymentIntentMemoData#getPaymentIntentId()
 * @see TxOutMemo
 * @since 4.0.0
 */
public final class SenderWithPaymentIntentMemo extends TxOutMemo {

    private static final String TAG = SenderWithPaymentIntentMemo.class.getSimpleName();

    private final RistrettoPublic txOutPublicKey;
    private final SenderWithPaymentIntentMemoData senderWithPaymentIntentMemoData;
    /**
     * Creates a {@link SenderWithPaymentIntentMemo} from a decrypted memo data that hasn't been
     * validated yet.
     *
     * @param memoData - The {@value TxOutMemo#TX_OUT_MEMO_DATA_SIZE_BYTES} bytes that correspond to the memo payload.
     */
    static SenderWithPaymentIntentMemo create(
            @NonNull RistrettoPublic txOutPublicKey,
            @NonNull byte[] memoData
    ) {
        if (memoData.length != TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES) {
            throw new IllegalArgumentException("Memo data byte array must have a length of " +
                    TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES + ". Instead, the length was: " + memoData.length);
        }
        return new SenderWithPaymentIntentMemo(txOutPublicKey, memoData);
    }

    private SenderWithPaymentIntentMemo(
            @NonNull RistrettoPublic txOutPublicKey,
            @NonNull byte[] memoData
    ) {
        super(TxOutMemoType.SENDER_WITH_PAYMENT_INTENT);
        this.txOutPublicKey = txOutPublicKey;
        try {
            init_jni_from_memo_data(memoData);
        } catch(Exception e) {
            IllegalArgumentException illegalArgumentException =
                    new IllegalArgumentException("Failed to create a SenderWithPaymentIntentMemo", e);
            Util.logException(TAG, illegalArgumentException);
            throw illegalArgumentException;
        }
        UnsignedLong paymentIntentId = UnsignedLong.fromLongBits(get_payment_intent_id());
        senderWithPaymentIntentMemoData = SenderWithPaymentIntentMemoData.create(getAddressHash(), paymentIntentId);
    }


    /**
     * Returns the {@link AddressHash} stored in this {@link SenderWithPaymentIntentMemo} without validating it first.
     *
     * To get the validated {@link AddressHash}, use {@link SenderWithPaymentIntentMemoData#getAddressHash()}.
     *
     * It is recommended to compare the output of this method to {@link AddressHash}es of known {@link PublicAddress}.
     * If the output of this method matches any known {@link PublicAddress} {@link AddressHash}, the memo
     * can be validated using that {@link PublicAddress}.
     *
     * @return the {@link AddressHash} in this memo without validating it first
     *
     * @see AddressHash
     * @see SenderWithPaymentIntentMemoData
     * @see SenderWithPaymentIntentMemoData#getAddressHash()
     * @see SenderWithPaymentIntentMemo#getSenderWithPaymentIntentMemoData(PublicAddress, RistrettoPrivate)
     * @see SenderMemo
     * @see SenderMemo#getUnvalidatedAddressHash()
     * @since 4.0.0
     */
    public AddressHash getUnvalidatedAddressHash() {
        return getAddressHash();
    }

    private AddressHash getAddressHash() {
        byte[] addressHashData = get_address_hash_data();
        return AddressHash.createAddressHash(addressHashData);
    }

    /**
     * Returns the {@link SenderWithPaymentIntentMemoData} for this {@link SenderWithPaymentIntentMemo} if valid.
     *
     * If validation of the memo fails, an {@link InvalidTxOutMemoException} is thrown
     *
     * Before calling this method, call {@link SenderWithPaymentIntentMemo#getUnvalidatedAddressHash()} and see if the
     * {@link AddressHash} corresponds to a {@link PublicAddress} that is known by the user.
     * If the {@link PublicAddress} is not known by the user, then do not call this method because the
     * memo is automatically invalid.
     *
     * @return the {@link SenderWithPaymentIntentMemoData}, if valid
     * @throws InvalidTxOutMemoException if validation of the memo fails
     *
     * @see SenderWithPaymentIntentMemoData
     * @see MemoData
     * @see PublicAddress
     * @see AddressHash
     * @see InvalidTxOutMemoException
     * @see SenderWithPaymentIntentMemo#getUnvalidatedAddressHash()
     * @since 4.0.0
     */
    public SenderWithPaymentIntentMemoData getSenderWithPaymentIntentMemoData(
            @NonNull PublicAddress senderPublicAddress,
            @NonNull RistrettoPrivate receiverSubaddressViewKey) throws InvalidTxOutMemoException {
        if(!validated) {
            if (!(validated = is_valid(
                    senderPublicAddress,
                    receiverSubaddressViewKey,
                    txOutPublicKey)
            )) {
                throw new InvalidTxOutMemoException("The sender memo is invalid.");
            }
        }
        return senderWithPaymentIntentMemoData;
    }

    /**
     * Returns the {@link SenderWithPaymentIntentMemoData} for this {@link SenderWithPaymentIntentMemo} without validating.
     *
     * There is no guarantee that the {@link AddressHash} in this memo was actually calculated from the
     * sender's {@link PublicAddress}. To validate the sender's {@link AddressHash}, use
     * {@link SenderWithPaymentIntentMemo#getSenderWithPaymentIntentMemoData(PublicAddress, RistrettoPrivate)}
     *
     * @return the {@link SenderWithPaymentIntentMemoData}
     *
     * @see SenderWithPaymentIntentMemoData
     * @see MemoData
     * @see PublicAddress
     * @see AddressHash
     * @since 4.0.0
     */
    public SenderWithPaymentIntentMemoData getUnvalidatedSenderWithPaymentIntentMemoData() {
        return senderWithPaymentIntentMemoData;
    }

    private SenderWithPaymentIntentMemo(@NonNull Parcel parcel) {
        super(TxOutMemoType.SENDER_WITH_PAYMENT_INTENT);
        txOutPublicKey = parcel.readParcelable(RistrettoPublic.class.getClassLoader());
        senderWithPaymentIntentMemoData = parcel.readParcelable(SenderWithPaymentIntentMemoData.class.getClassLoader());
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeParcelable(txOutPublicKey, flags);
        parcel.writeParcelable(senderWithPaymentIntentMemoData, flags);
    }

    public static Creator<SenderWithPaymentIntentMemo> CREATOR = new Creator<SenderWithPaymentIntentMemo>() {
        @Override
        public SenderWithPaymentIntentMemo createFromParcel(@NonNull Parcel parcel) {
            return new SenderWithPaymentIntentMemo(parcel);
        }

        @Override
        public SenderWithPaymentIntentMemo[] newArray(int length) {
            return new SenderWithPaymentIntentMemo[length];
        }
    };

    @Override
    public boolean equals(Object o) {
        if(o instanceof SenderWithPaymentIntentMemo) {
            SenderWithPaymentIntentMemo that = (SenderWithPaymentIntentMemo)o;
            return Objects.equals(this.memoType, that.memoType) &&
                    Objects.equals(this.txOutPublicKey, that.txOutPublicKey) &&
                    Objects.equals(this.senderWithPaymentIntentMemoData, that.senderWithPaymentIntentMemoData);
        }
        return false;
    }

    private native void init_jni_from_memo_data(byte[] memoData);

    // Returns true if the sender with payment intent memo is valid.
    private native boolean is_valid(
            @NonNull PublicAddress senderPublicAddress,
            @NonNull RistrettoPrivate receiverSubaddressViewKey,
            @NonNull RistrettoPublic txOutPublicKey
    );

    private native byte[] get_address_hash_data();

    private native long get_payment_intent_id();
}
