// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import consensus_common.ConsensusCommon;

public final class InvalidTransactionException extends MobileCoinException {

    @Nullable
    private final ConsensusCommon.ProposeTxResult result;

    public InvalidTransactionException(@NonNull ConsensusCommon.ProposeTxResult result) {
        super(result.toString());
        this.result = result;
    }

    @Deprecated
    public InvalidTransactionException(@Nullable String message) {
        super(message);
        this.result = null;
    }

    @Nullable
    public ConsensusCommon.ProposeTxResult getResult() {
        return this.result;
    }

}
