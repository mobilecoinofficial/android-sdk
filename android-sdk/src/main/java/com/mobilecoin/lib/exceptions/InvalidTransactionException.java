// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.Amount;
import com.mobilecoin.lib.DefragmentationDelegate;
import com.mobilecoin.lib.PendingTransaction;
import com.mobilecoin.lib.PublicAddress;
import com.mobilecoin.lib.Transaction;
import com.mobilecoin.lib.TxOutMemoBuilder;
import com.mobilecoin.lib.UnsignedLong;

import java.math.BigInteger;

import consensus_common.ConsensusCommon;

/**
 * This class represents a {@link MobileCoinException} that results from a failed/rejected
 * {@link com.mobilecoin.lib.Transaction} proposal.
 *
 * @see com.mobilecoin.lib.Transaction
 * @see com.mobilecoin.lib.MobileCoinClient#prepareTransaction(PublicAddress, Amount, Amount, TxOutMemoBuilder)
 * @see com.mobilecoin.lib.MobileCoinAccountClient#defragmentAccount(Amount, DefragmentationDelegate, boolean)
 * @see com.mobilecoin.lib.MobileCoinTransactionClient#submitTransaction(Transaction)
 * @see DefragmentationDelegate#onStepReady(PendingTransaction, BigInteger)
 * @see ConsensusCommon.ProposeTxResult
 * @since 1.0.0
 */
public final class InvalidTransactionException extends MobileCoinException {

    @Nullable
    private final ConsensusCommon.ProposeTxResult result;
    @Nullable
    private final UnsignedLong blockIndex;

    /**
     * Creates an {@code InvalidTransactionException} with the given {@link ConsensusCommon.ProposeTxResult}.
     *
     * @param result the result of a {@link Transaction} proposal
     * @param blockIndex the blockIndex from consensus
     * @see ConsensusCommon.ProposeTxResult
     * @since 1.2.2
     */
    public InvalidTransactionException(@NonNull ConsensusCommon.ProposeTxResult result, @NonNull UnsignedLong blockIndex) {
        super(result.toString());
        this.result = result;
        this.blockIndex = blockIndex;
    }

    /**
     * Creates an InvalidTransactionException with the specified message.
     *
     * @param message the {@link Exception} message
     * @deprecated Deprecated as of 1.2.2. Please use
     * {@link InvalidTransactionException#InvalidTransactionException(ConsensusCommon.ProposeTxResult, UnsignedLong)}.
     * @see InvalidTransactionException#InvalidTransactionException(ConsensusCommon.ProposeTxResult, UnsignedLong) .
     * @see ConsensusCommon.ProposeTxResult
     * @since 1.0.0
     */
    @Deprecated
    public InvalidTransactionException(@Nullable String message) {
        super(message);
        this.result = null;
        this.blockIndex = null;
    }

    /**
     * Returns the {@link ConsensusCommon.ProposeTxResult} that caused this {@code InvalidTransactionException}.
     *
     * @return the failure reason for the {@link Transaction}
     * @see ConsensusCommon.ProposeTxResult
     * @since 1.2.2
     */
    @Nullable
    public ConsensusCommon.ProposeTxResult getResult() {
        return this.result;
    }

    /**
     * Returns the consensus blockIndex.
     *
     * @return blockIndex at the time of consensus that returned the error
     * @see ConsensusCommon.ProposeTxResult
     * @since 1.2.2
     */
    @Nullable
    public UnsignedLong getBlockIndex() {
        return this.blockIndex;
    }

}
