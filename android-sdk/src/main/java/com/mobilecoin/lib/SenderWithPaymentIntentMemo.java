package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;

import java.util.Objects;

// TODO: doc
public final class SenderWithPaymentIntentMemo extends TxOutMemo {

    private static final String TAG = SenderWithPaymentIntentMemo.class.getSimpleName();

    private final RistrettoPublic txOutPublicKey;
    private final SenderWithPaymentIntentMemoData senderWithPaymentIntentMemoData;
    /**
     * Creates a {@link SenderWithPaymentIntentMemo} from a decrypted memo data that hasn't been
     * validated yet.
     *
     * @param memoData - The {@value TxOutMemo#TX_OUT_MEMO_DATA_SIZE_BYTES} bytes that correspond to the memo payload.
     **/
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


    // TODO: doc
    public AddressHash getUnvalidatedAddressHash() {
        return getAddressHash();
    }

    private AddressHash getAddressHash() {
        byte[] addressHashData = get_address_hash_data();
        return AddressHash.createAddressHash(addressHashData);
    }

    /**
     * Validates then retrieves the sender with payment intent memo data.
     *
     * <p>Before calling this method, call {@link #getUnvalidatedAddressHash()} and see if the
     * {@link AddressHash} corresponds to a {@link PublicAddress} that is known by the user.
     * If the {@link PublicAddress} is not known by the user, then do not call this method because the
     * memo is automatically invalid.
     **/// TODO: doc
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
