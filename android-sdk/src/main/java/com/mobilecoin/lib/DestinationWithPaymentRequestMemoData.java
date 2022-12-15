package com.mobilecoin.lib;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Contains data associated with a {@link DestinationWithPaymentRequestMemo}.
 *
 * The data has been validated, which means that the account accessing this {@link DestinationWithPaymentRequestMemoData}
 * is the same one which send the associated {@link Transaction}.
 *
 * @see DestinationWithPaymentRequestMemo
 * @see MemoData
 * @see AddressHash
 * @since 4.0.0
 */
public final class DestinationWithPaymentRequestMemoData extends MemoData {

    @NonNull
    private final UnsignedLong fee, totalOutlay, paymentRequestId;

    private final int numberOfRecipients;

    static DestinationWithPaymentRequestMemoData create(
            @NonNull final AddressHash addressHash,
            final int numberOfRecipients,
            @NonNull final UnsignedLong fee,
            @NonNull final UnsignedLong totalOutlay,
            @NonNull final UnsignedLong paymentRequestId
    ) {
        return new DestinationWithPaymentRequestMemoData(addressHash, numberOfRecipients, fee, totalOutlay, paymentRequestId);
    }

    private DestinationWithPaymentRequestMemoData(
            @NonNull final AddressHash addressHash,
            final int numberOfRecipients,
            @NonNull final UnsignedLong fee,
            @NonNull final UnsignedLong totalOutlay,
            @NonNull final UnsignedLong paymentRequestId
    ) {
        super(addressHash);
        this.numberOfRecipients = numberOfRecipients;
        this.fee = fee;
        this.totalOutlay = totalOutlay;
        this.paymentRequestId = paymentRequestId;
    }

    /**
     * Gets the number of recipients from the {@link Transaction} that created the associated {@link DestinationWithPaymentRequestMemo}
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
     * check the {@link OwnedTxOut#getAmount()} of the {@link OwnedTxOut} to which the {@link DestinationWithPaymentRequestMemo}
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
     * check the {@link OwnedTxOut#getAmount()} of the {@link OwnedTxOut} to which the {@link DestinationWithPaymentRequestMemo}
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
     * Gets the <strong>payment request</strong> ID stored in this {@link DestinationWithPaymentRequestMemoData}
     *
     * For additional information about this field, see
     * {@link TxOutMemoBuilder#createSenderPaymentRequestAndDestinationRTHMemoBuilder(AccountKey, UnsignedLong)}.
     *
     * @return the ID of a <strong>payment request</strong>
     *
     * @see TxOutMemoBuilder#createSenderPaymentRequestAndDestinationRTHMemoBuilder(AccountKey, UnsignedLong)
     * @see DestinationWithPaymentRequestMemo
     * @see SenderWithPaymentRequestMemoData#getPaymentRequestId()
     * @since 4.0.0
     */
    @NonNull
    public UnsignedLong getPaymentRequestId() {
        return paymentRequestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DestinationWithPaymentRequestMemoData that = (DestinationWithPaymentRequestMemoData) o;
        return numberOfRecipients == that.numberOfRecipients &&
                Objects.equals(addressHash, that.addressHash) &&
                Objects.equals(fee, that.fee) &&
                Objects.equals(totalOutlay, that.totalOutlay) &&
                Objects.equals(paymentRequestId, that.paymentRequestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressHash, numberOfRecipients, fee, totalOutlay, paymentRequestId);
    }

    private DestinationWithPaymentRequestMemoData(@NonNull Parcel parcel) {
        super(parcel.readParcelable(AddressHash.class.getClassLoader()));
        fee = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        totalOutlay = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        numberOfRecipients = parcel.readInt();
        paymentRequestId = parcel.readParcelable(UnsignedLong.class.getClassLoader());
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeParcelable(fee, flags);
        parcel.writeParcelable(totalOutlay, flags);
        parcel.writeInt(numberOfRecipients);
        parcel.writeParcelable(paymentRequestId, flags);
    }

    public static final Creator<DestinationWithPaymentRequestMemoData> CREATOR = new Creator<DestinationWithPaymentRequestMemoData>() {
        @Override
        public DestinationWithPaymentRequestMemoData createFromParcel(@NonNull Parcel parcel) {
            return new DestinationWithPaymentRequestMemoData(parcel);
        }

        @Override
        public DestinationWithPaymentRequestMemoData[] newArray(int length) {
            return new DestinationWithPaymentRequestMemoData[length];
        }
    };

}