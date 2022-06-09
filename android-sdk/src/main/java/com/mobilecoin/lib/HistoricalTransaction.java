package com.mobilecoin.lib;

import androidx.annotation.NonNull;

/**
 *
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
     *
     * @return
     */
    @NonNull
    public OwnedTxOut getTxOut() {
        return this.txOut;
    }

    /**
     *
     * @return
     */
    @NonNull
    public TxOutMemo getMemo() {
        return this.memo;
    }

}
