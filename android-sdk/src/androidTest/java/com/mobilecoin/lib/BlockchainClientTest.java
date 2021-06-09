package com.mobilecoin.lib;

import com.mobilecoin.lib.uri.ConsensusUri;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

import consensus_common.ConsensusCommon;

public class BlockchainClientTest {
    @Test
    public void clientCachesLastBlockInfo() throws Exception {
        BlockchainClient blockchainClient = new BlockchainClient(
                new ConsensusUri(Environment.getTestFogConfig().getConsensusUri()),
                Environment.getTestFogConfig().getClientConfig().consensus,
                Duration.ofHours(1));
        blockchainClient.setAuthorization(
                Environment.getTestFogConfig().getUsername(),
                Environment.getTestFogConfig().getPassword()
        );
        ConsensusCommon.LastBlockInfoResponse lastBlockInfoResponse1 =
                blockchainClient.getOrFetchLastBlockInfo();
        ConsensusCommon.LastBlockInfoResponse lastBlockInfoResponse2 =
                blockchainClient.getOrFetchLastBlockInfo();
        Assert.assertSame(lastBlockInfoResponse1, lastBlockInfoResponse2);
    }

    @Test
    public void clientRespectsCacheTTL() throws Exception {
        BlockchainClient blockchainClient = new BlockchainClient(
                new ConsensusUri(Environment.getTestFogConfig().getConsensusUri()),
                Environment.getTestFogConfig().getClientConfig().consensus,
                Duration.ofSeconds(1));
        blockchainClient.setAuthorization(
                Environment.getTestFogConfig().getUsername(),
                Environment.getTestFogConfig().getPassword()
        );
        ConsensusCommon.LastBlockInfoResponse lastBlockInfoResponse1 =
                blockchainClient.getOrFetchLastBlockInfo();
        Thread.sleep(1000);
        ConsensusCommon.LastBlockInfoResponse lastBlockInfoResponse2 =
                blockchainClient.getOrFetchLastBlockInfo();
        Assert.assertNotSame(lastBlockInfoResponse1, lastBlockInfoResponse2);
    }
}
