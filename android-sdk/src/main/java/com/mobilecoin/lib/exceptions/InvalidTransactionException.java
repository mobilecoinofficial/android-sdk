// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.NonNull;

import consensus_common.ConsensusCommon;

public final class InvalidTransactionException extends MobileCoinException {

    private final ConsensusCommon.ProposeTxResult result;

    public InvalidTransactionException(@NonNull ConsensusCommon.ProposeTxResult result) {
        super(result.toString());
        this.result = result;
    }

    public ConsensusCommon.ProposeTxResult getResult() {
        return this.result;
    }

}
