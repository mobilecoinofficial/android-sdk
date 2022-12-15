package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Contains data associated with a {@link DestinationWithPaymentIntentMemo}.
 *
 * The data has been validated, which means that the account accessing this {@link DestinationWithPaymentIntentMemoData}
 * is the same one which send the associated {@link Transaction}.
 *
 * @see DestinationWithPaymentIntentMemo
 * @see MemoData
 * @see AddressHash
 * @since 4.0.0
 */
public final class DestinationWithPaymentIntentMemoData extends MemoData {

    @NonNull
    private final UnsignedLong fee, totalOutlay, paymentIntentId;

    private final int numberOfRecipients;

    static DestinationWithPaymentIntentMemoData create(
            @NonNull final AddressHash addressHash,
            final int numberOfRecipients,
            @NonNull final UnsignedLong fee,
            @NonNull final UnsignedLong totalOutlay,
            @NonNull final UnsignedLong paymentRequestId
    ) {
        return new DestinationWithPaymentIntentMemoData(addressHash, numberOfRecipients, fee, totalOutlay, paymentRequestId);
    }

    private DestinationWithPaymentIntentMemoData(
            @NonNull final AddressHash addressHash,
            final int numberOfRecipients,
            @NonNull final UnsignedLong fee,
            @NonNull final UnsignedLong totalOutlay,
            @NonNull final UnsignedLong paymentIntentId
    ) {
        super(addressHash);
        this.numberOfRecipients = numberOfRecipients;
        this.fee = fee;
        this.totalOutlay = totalOutlay;
        this.paymentIntentId = paymentIntentId;
    }

    /**
     * Gets the number of recipients from the {@link Transaction} that created the associated {@link DestinationWithPaymentIntentMemo}
     *
     * As of 2.0.0, {@link Transaction}s built using this SDK are limited to a single recipient. As a result,
     * for {@link Transaction}s built using this SDK, this value will always be 1. This may not be the case if the
     * account is also used with different client implementation.
     *
     * @return the number of recipients of the associated {@link Transaction}
     *
     * @see MobileCoinClient#prepareTransaction(PublicAddress, Amount, Amount, TxOutMemoBuilder)
     * @since 4.0.0
     */
    public int getNumberOfRecipients() {
        return numberOfRecipients;
    }

    /**
     * Gets the fee that was paid to submit the associated {@link Transaction}
     *
     * The value returned does not come with a {@link TokenId}. To get the {@link TokenId} of the fee,
     * check the {@link OwnedTxOut#getAmount()} of the {@link OwnedTxOut} to which the {@link DestinationWithPaymentIntentMemo}
     * is attached.
     *
     * @return the value of the fee paid to submit the {@link Transaction}
     *
     * @see MobileCoinClient#prepareTransaction(PublicAddress, Amount, Amount, TxOutMemoBuilder)
     * @see OwnedTxOut
     * @see OwnedTxOut#getAmount()
     * @see Amount
     * @see Amount#getTokenId()
     * @see OwnedTxOut#getTxOutMemo()
     * @since 4.0.0
     */
    @NonNull
    public UnsignedLong getFee() {
        return fee;
    }

    /**
     * Gets the <strong>total outlay</strong> of the associated {@link Transaction}.
     *
     * The <strong>total outlay</strong> is the amount being sent plus fees.
     * The value returned does not come with a {@link TokenId}. To get the {@link TokenId} of the fee,
     * check the {@link OwnedTxOut#getAmount()} of the {@link OwnedTxOut} to which the {@link DestinationWithPaymentIntentMemo}
     * is attached.
     *
     * @return the value of the <strong>total outlay</strong> of the {@link Transaction}
     *
     * @see MobileCoinClient#prepareTransaction(PublicAddress, Amount, Amount, TxOutMemoBuilder)
     * @see OwnedTxOut
     * @see OwnedTxOut#getAmount()
     * @see Amount
     * @see Amount#getTokenId()
     * @see OwnedTxOut#getTxOutMemo()
     * @since 4.0.0
     */
    @NonNull
    public UnsignedLong getTotalOutlay() {
        return totalOutlay;
    }

    /**
     * Gets the <strong>payment intent</strong> ID stored in this {@link DestinationWithPaymentIntentMemoData}
     *
     * For additional information about this field, see
     * {@link TxOutMemoBuilder#createSenderPaymentIntentAndDestinationRTHMemoBuilder(AccountKey, UnsignedLong)}.
     *
     * @return the ID of a <strong>payment intent</strong>
     *
     * @see TxOutMemoBuilder#createSenderPaymentIntentAndDestinationRTHMemoBuilder(AccountKey, UnsignedLong)
     * @see DestinationWithPaymentIntentMemo
     * @see SenderWithPaymentIntentMemoData#getPaymentIntentId()
     * @since 4.0.0
     */
    @NonNull
    public UnsignedLong getPaymentIntentId() {
        return paymentIntentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DestinationWithPaymentIntentMemoData that = (DestinationWithPaymentIntentMemoData) o;
        return numberOfRecipients == that.numberOfRecipients &&
                Objects.equals(addressHash, that.addressHash) &&
                Objects.equals(fee, that.fee) &&
                Objects.equals(totalOutlay, that.totalOutlay) &&
                Objects.equals(paymentIntentId, that.paymentIntentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressHash, numberOfRecipients, fee, totalOutlay, paymentIntentId);
    }

    private DestinationWithPaymentIntentMemoData(@NonNull Parcel parcel) {
        super(parcel.readParcelable(AddressHash.class.getClassLoader()));
        fee = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        totalOutlay = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        numberOfRecipients = parcel.readInt();
        paymentIntentId = parcel.readParcelable(UnsignedLong.class.getClassLoader());
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeParcelable(fee, flags);
        parcel.writeParcelable(totalOutlay, flags);
        parcel.writeInt(numberOfRecipients);
        parcel.writeParcelable(paymentIntentId, flags);
    }

    public static final Creator<DestinationWithPaymentIntentMemoData> CREATOR = new Creator<DestinationWithPaymentIntentMemoData>() {
        @Override
        public DestinationWithPaymentIntentMemoData createFromParcel(@NonNull Parcel parcel) {
            return new DestinationWithPaymentIntentMemoData(parcel);
        }

        @Override
        public DestinationWithPaymentIntentMemoData[] newArray(int length) {
            return new DestinationWithPaymentIntentMemoData[length];
        }
    };

}