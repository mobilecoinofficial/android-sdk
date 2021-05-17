package com.mobilecoin.lib.network.services;

import attest.Attest;
import consensus_common.ConsensusCommon;

public interface ConsensusClientService {
    ConsensusCommon.ProposeTxResponse clientTxPropose(Attest.Message request);
}
