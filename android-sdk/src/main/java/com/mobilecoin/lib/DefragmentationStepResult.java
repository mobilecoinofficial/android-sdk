package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import consensus_common.ConsensusCommon;

/**
 * <p>
 * The {@code DefragmentationStepResult} class represents the result of a {@link DefragmentationStep}.
 * <br /><br />
 * A {@code DefragmentationStepResult} contains two things:
 * <ol>
 *     <li>a boolean signifying whether or not defragmentation should continue after this step</li>
 *     <li>a {@link consensus_common.ConsensusCommon.ProposeTxResult} which results from executing
 *     the {@link Transaction} of a {@link DefragmentationStep}</li>
 * </ol>
 * </p>
 *
 * @see DefragmentationStep
 * @see DefragmentationDelegate#onStepReady(DefragmentationStep)
 * @see MobileCoinClient#defragmentAccount(Amount, DefragmentationDelegate, boolean)
 * @since 1.2.2
 */
public class DefragmentationStepResult {

    private final boolean shouldContinue;
    @NonNull
    private final ConsensusCommon.ProposeTxResult stepTxResult;

    /**
     * Creates a new {@code DefragmentationStepResult} with the provided boolean and
     * {@link consensus_common.ConsensusCommon.ProposeTxResult}.
     *
     * The boolean signifies whether or not the defragmentation should continue (true) or be
     * canceled (false) after this result is returned.
     *
     * The {@link consensus_common.ConsensusCommon.ProposeTxResult} is returned after proposing the
     * {@link Transaction} at the current {@link DefragmentationStep}.
     *
     * @param shouldContinue whether or not defragmentation should continue after this step
     * @param stepTxResult the result of the {@link Transaction} submitted during a {@link DefragmentationStep}
     *
     * @see DefragmentationStep
     * @see DefragmentationDelegate#onStepReady(DefragmentationStep)
     * @since 1.2.2
     */
    public DefragmentationStepResult(boolean shouldContinue, @NonNull ConsensusCommon.ProposeTxResult stepTxResult) {
        this.shouldContinue = shouldContinue;
        this.stepTxResult = stepTxResult;
    }

    /**
     * @return true if defragmentation should continue or false if it should be canceled
     *
     * @see DefragmentationDelegate#onStepReady(DefragmentationStep)
     * @since 1.2.2
     */
    public boolean shouldContinue() {
        return this.shouldContinue;
    }

    /**
     * @return the result of the {@link Transaction} submitted as part of a {@link DefragmentationStep}
     *
     * @see DefragmentationDelegate#onStepReady(DefragmentationStep)
     * @since 1.2.2
     */
    @NonNull
    public ConsensusCommon.ProposeTxResult getStepTxResult() {
        return this.stepTxResult;
    }

    /**
     * If the {@link Transaction} submitted as part of a {@link DefragmentationStep} is accepted,
     * the {@link consensus_common.ConsensusCommon.ProposeTxResult} will have a value of
     * {@code ProposeTxResult.Ok}. This method will check the {@link consensus_common.ConsensusCommon.ProposeTxResult}
     * and return true if the {@link Transaction} was accepted or false if it was not for any reason.
     *
     * @return true if the {@link DefragmentationStep} succeeded, false otherwise
     * @since 1.2.2
     */
    public boolean stepSucceeded() {
        return this.stepTxResult == ConsensusCommon.ProposeTxResult.Ok;
    }

}
