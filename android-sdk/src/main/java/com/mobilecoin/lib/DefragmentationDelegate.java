// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.InvalidTransactionException;
import com.mobilecoin.lib.exceptions.NetworkException;

import java.math.BigInteger;

/**
 * This class is used to monitor and control the defragmentation process.
 * The account balance consists of multiple coins, if there are no big enough coins to
 * successfully send transaction, the account needs to be defragmented. If the account is too
 * fragmented, there may be a need to defragment the account more than once.
 */
public interface DefragmentationDelegate {
    /**
     * Called when defragmentation process begins
     */
    void onStart();

    /**
     * Called for each step of the defragmentation process.
     * The delegate is responsible for the submission of the provided defrag step
     * This method should return false to cancel the defragmentation process
     *
     * @param defragStepTx is a ready to submit defragmentation transaction and a receipt
     * @param fee          cost to submit provided transaction to the ledger
     * @return whether or not defragmentation process should continue
     */
    boolean onStepReady(@NonNull PendingTransaction defragStepTx, @NonNull BigInteger fee)
            throws NetworkException, InvalidTransactionException, AttestationException;

    /**
     * Called upon the successful completion of the defragmentation process
     */
    void onComplete();

    /**
     * Called to clean up after the defragmentation was cancelled
     */
    void onCancel();
}
