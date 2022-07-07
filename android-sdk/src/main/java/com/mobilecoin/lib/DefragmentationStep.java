package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import java.math.BigInteger;

public class DefragmentationStep {

    @NonNull
    private final PendingTransaction defragStepTx;
    @NonNull
    private final BigInteger fee;

    public DefragmentationStep(@NonNull PendingTransaction defragStepTx, @NonNull BigInteger fee) {
        this.defragStepTx = defragStepTx;
        this.fee = fee;
    }

    @NonNull
    public PendingTransaction getPendingTransaction() {
        return this.defragStepTx;
    }

    @NonNull
    public Transaction getTransaction() {
        return this.defragStepTx.getTransaction();
    }

    @NonNull
    public BigInteger getFee() {
        return this.fee;
    }

}
