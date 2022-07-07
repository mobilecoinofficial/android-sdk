package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import java.math.BigInteger;

/**
 * <p>
 * The {@code DefragmentationStep} class represents a step in the process of defragmenting an account.
 * <br /><br />
 * Defragmentation is needed when an account has many small TxOuts and would require a very large
 * number to be added together to send a {@link Transaction} (See {@link DefragmentationDelegate}).
 * <br /><br />
 * A {@link DefragmentationStep} will be used by a {@link DefragmentationDelegate} if an account
 * requires defragmentation to send a transaction.
 * <br /><br />
 * The {@code DefragmentationStep} class is immutable.
 * </p>
 *
 * @see DefragmentationDelegate
 * @see DefragmentationDelegate#onStepReady(DefragmentationStep)
 * @see DefragmentationStepResult
 * @see MobileCoinClient#defragmentAccount(Amount, DefragmentationDelegate, boolean)
 * @since 1.2.2
 */
public class DefragmentationStep {

    @NonNull
    private final Transaction defragStepTx;
    @NonNull
    private final BigInteger fee;

    /**
     * Creates a {@code DefragmentationStep} with the provided {@link Transaction} and
     * {@code fee}.
     *
     * The provided {@link Transaction} will be executed at this step of the defragmentation process.
     * The provided {@code fee} is the fee to be paid to execute the {@link Transaction}.
     *
     * @param defragStepTx
     * @param fee
     * @see Transaction
     * @since 1.2.2
     */
    public DefragmentationStep(@NonNull Transaction defragStepTx, @NonNull BigInteger fee) {
        this.defragStepTx = defragStepTx;
        this.fee = fee;
    }

    /**
     * Returns the {@link Transaction} that will be executed at this step of the defragmentation
     * process. The {@link consensus_common.ConsensusCommon.ProposeTxResult} of submitting this
     * {@link Transaction} should be used to construct a {@link DefragmentationStepResult} to return
     * from {@link DefragmentationDelegate#onStepReady(DefragmentationStep)}.
     *
     * @return this {@code DefragmentationStep}'s {@link Transaction}
     * @see Transaction
     * @see DefragmentationDelegate
     * @since 1.2.2
     */
    @NonNull
    public Transaction getTransaction() {
        return this.defragStepTx;
    }

    /**
     * Returns the {@code fee} of the {@link Transaction} that will be executed at this step of the
     * defragmentation process.
     *
     * @return the {@link} the fee that will be paid to execute the {@link Transaction} of this
     * {@code DefragmentationStep}
     * @see Transaction
     * @since 1.2.2
     */
    @NonNull
    public BigInteger getFee() {
        return this.fee;
    }

}
