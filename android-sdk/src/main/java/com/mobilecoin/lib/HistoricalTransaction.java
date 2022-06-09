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

}
