package com.mobilecoin.lib.network.services;

import com.google.protobuf.Empty;

import consensus_common.ConsensusCommon;

public interface BlockchainService {
    ConsensusCommon.LastBlockInfoResponse getLastBlockInfo(Empty request);
}
