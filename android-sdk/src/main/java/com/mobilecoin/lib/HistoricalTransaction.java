package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import java.util.Set;

/**
 * Represents a {@link Transaction} with a contact which has already completed
 *
 * @see AccountActivity#recoverTransactions(Set)
 * @see AccountActivity#recoverContactTransactions(AddressHashProvider)
 * @see AddressHashProvider
 * @see OwnedTxOut
 * @see TxOutMemo
 * @since 1.2.2
 */
public class HistoricalTransaction {

    @NonNull
    private final OwnedTxOut txOut;
    @NonNull
    private final TxOutMemo memo;

    HistoricalTransaction(@NonNull OwnedTxOut txOut) {
        this.txOut = txOut;
        this.memo = txOut.getTxOutMemo();
    }

    /**
     * Get the {@link OwnedTxOut} associated with this {@link HistoricalTransaction}.
     * The {@link OwnedTxOut} can be used to get information such as the {@link Amount} of the
     * transaction or the time/block index it was received.
     *
     * @return the {@link OwnedTxOut} for this {@link HistoricalTransaction}
     * @see OwnedTxOut
     * @see TxOutMemo
     * @since 1.2.2
     */
    @NonNull
    public OwnedTxOut getTxOut() {
        return this.txOut;
    }

    /**
     * Get the {@link TxOutMemo} of this {@link HistoricalTransaction}.
     * The {@link TxOutMemo} contains various transaction info depending on the {@link TxOutMemoType}.
     *
     * @return the {@link TxOutMemo} for this {@link HistoricalTransaction}
     * @see TxOutMemo
     * @see TxOutMemoType
     * @see DestinationMemo
     * @see SenderMemo
     * @see SenderWithPaymentRequestMemo
     * @since 1.2.2
     */
    @NonNull
    public TxOutMemo getMemo() {
        return this.memo;
    }

}
