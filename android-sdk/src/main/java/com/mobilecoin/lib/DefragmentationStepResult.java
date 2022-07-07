package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import consensus_common.ConsensusCommon;

public class DefragmentationStepResult {

    private final boolean shouldContinue;
    @NonNull
    private final ConsensusCommon.ProposeTxResult stepTxResult;

    DefragmentationStepResult(boolean shouldContinue, @NonNull ConsensusCommon.ProposeTxResult stepTxResult) {
        this.shouldContinue = shouldContinue;
        this.stepTxResult = stepTxResult;
    }

    public boolean shouldContinue() {
        return this.shouldContinue;
    }

    @NonNull
    public ConsensusCommon.ProposeTxResult getStepTxResult() {
        return this.stepTxResult;
    }

    public boolean stepSucceeded() {
        return this.stepTxResult == ConsensusCommon.ProposeTxResult.Ok;
    }

}
