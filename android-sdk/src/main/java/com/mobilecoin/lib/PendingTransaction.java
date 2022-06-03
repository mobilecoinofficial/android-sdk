// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.log.Logger;

/**
 * Wrapper for the Transaction and Receipt objects obtained via {@link
 * MobileCoinTransactionClient#prepareTransaction(PublicAddress, Amount, Amount, TxOutMemoBuilder)}
 */
public final class PendingTransaction {
    private final static String TAG = PendingTransaction.class.getName();
    private final Transaction transaction;
    private final Receipt receipt;
    private final TxOutContext payloadTxOutContext;
    private final TxOutContext changeTxOutContext;

    public PendingTransaction(
            @NonNull Transaction tx,
            @NonNull Receipt receipt,
            @NonNull TxOutContext payloadTxOutContext,
            @NonNull TxOutContext changeTxOutContext
    ) {
        this.transaction = tx;
        this.receipt = receipt;
        this.payloadTxOutContext = payloadTxOutContext;
        this.changeTxOutContext = changeTxOutContext;
        Logger.i(TAG, "Created PendingTransaction", null,
                "receipt:", receipt,
                "transaction:", tx, "payloadTxOutContext:", payloadTxOutContext,
                "changeTxOutContext:", changeTxOutContext);
    }

    @NonNull
    public Receipt getReceipt() {
        Logger.i(TAG, "Getting receipt", null, receipt);
        return receipt;
    }

    @NonNull
    public Transaction getTransaction() {
        Logger.i(TAG, "Getting transaction", null, transaction);
        return transaction;
    }

    @NonNull
    public TxOutContext getPayloadTxOutContext() {
        return payloadTxOutContext;
    }

    @NonNull
    public TxOutContext getChangeTxOutContext() {
        return changeTxOutContext;
    }

}
