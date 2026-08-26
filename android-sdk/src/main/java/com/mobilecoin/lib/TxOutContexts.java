package com.mobilecoin.lib;

import androidx.annotation.NonNull;

/**
 * The payload and change outputs a transaction's RNG seed produces.
 * <p>
 * Returned on its own by
 * {@link MobileCoinClient#getTxOutContexts(PublicAddress, Amount, Amount, TxOutMemoBuilder, Rng)},
 * which derives the outputs without selecting inputs or building a
 * transaction, and carried inside {@link PendingTransaction} when a
 * transaction is actually built.
 */
public final class TxOutContexts {

    @NonNull
    private final TxOutContext payload;

    @NonNull
    private final TxOutContext change;

    TxOutContexts(@NonNull final TxOutContext payload, @NonNull final TxOutContext change) {
        this.payload = payload;
        this.change = change;
    }

    /** The output sent to the recipient. */
    @NonNull
    public TxOutContext getPayload() {
        return payload;
    }

    /** The output returning the remainder to the sender. */
    @NonNull
    public TxOutContext getChange() {
        return change;
    }

}
