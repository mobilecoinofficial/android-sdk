package com.mobilecoin.lib;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mobilecoin.lib.network.services.BlockchainService;
import com.mobilecoin.lib.network.services.ServiceAPIManager;
import com.mobilecoin.lib.network.uri.ConsensusUri;
import com.mobilecoin.lib.network.uri.MobileCoinUri;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

import consensus_common.ConsensusCommon;

public class BlockchainClientTest {
    @Test
    public void clientCachesLastBlockInfo() throws Exception {
        ConsensusUri consensusUri = new ConsensusUri(
            Environment.getTestFogConfig().getConsensusUri());
        BlockchainClient blockchainClient = new BlockchainClient(
                RandomLoadBalancer.create(consensusUri),
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
    public void clientCachesLastBlockInfoOffline() throws Exception {
        // Setup mock API manager and blockchain services
        ServiceAPIManager apiManager = mock(ServiceAPIManager.class);
        BlockchainService blockchainService = mock(BlockchainService.class);
        when(blockchainService.getLastBlockInfo(any())).thenReturn(
                ConsensusCommon.LastBlockInfoResponse.newBuilder()
                        .setIndex(1)
                        .setMinimumFee(1)
                        .build()
        ).thenReturn(
                ConsensusCommon.LastBlockInfoResponse.newBuilder()
                        .setIndex(2)
                        .setMinimumFee(3)
                        .build()
        );
        when(apiManager.getBlockchainService(any())).thenReturn(blockchainService);

        // Setup blockchain client
        BlockchainClient blockchainClient = new BlockchainClient(
                createLoadBalancer(),
                Environment.getTestFogConfig().getClientConfig().consensus,
                Duration.ofHours(1),
                apiManager);

        // Get initial block response and cache it
        ConsensusCommon.LastBlockInfoResponse lastBlockInfoResponse1 =
                blockchainClient.getOrFetchLastBlockInfo();

        // Test client caching last block info
        ConsensusCommon.LastBlockInfoResponse lastBlockInfoResponse2 =
                blockchainClient.getOrFetchLastBlockInfo();
        Assert.assertSame(lastBlockInfoResponse1, lastBlockInfoResponse2);
    }

    @Test
    public void clientRespectsCacheTTL() throws Exception {
        ConsensusUri consensusUri = new ConsensusUri(
            Environment.getTestFogConfig().getConsensusUri());
        BlockchainClient blockchainClient = new BlockchainClient(
                RandomLoadBalancer.create(consensusUri),
                Environment.getTestFogConfig().getClientConfig().consensus,
                Duration.ofMillis(1));
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

    @Test
    public void clientRespectsCacheTTLOffline() throws Exception {
        // Setup mock API manager and blockchain services
        ServiceAPIManager apiManager = mock(ServiceAPIManager.class);
        BlockchainService blockchainService = mock(BlockchainService.class);
        when(blockchainService.getLastBlockInfo(any())).thenReturn(
                ConsensusCommon.LastBlockInfoResponse.newBuilder()
                        .setIndex(1)
                        .setMinimumFee(1)
                        .build()
        ).thenReturn(
                ConsensusCommon.LastBlockInfoResponse.newBuilder()
                        .setIndex(1)
                        .setMinimumFee(1)
                        .build()
        );
        when(apiManager.getBlockchainService(any())).thenReturn(blockchainService);

        // Setup blockchain client
        BlockchainClient blockchainClient = new BlockchainClient(
                createLoadBalancer(),
                Environment.getTestFogConfig().getClientConfig().consensus,
                Duration.ofMillis(1), apiManager);

        // Get initial block response and cache it
        ConsensusCommon.LastBlockInfoResponse lastBlockInfoResponse1 =
                blockchainClient.getOrFetchLastBlockInfo();
        Thread.sleep(1000);

        // Test client respects cache TTL
        ConsensusCommon.LastBlockInfoResponse lastBlockInfoResponse2 =
                blockchainClient.getOrFetchLastBlockInfo();
        Assert.assertNotSame(lastBlockInfoResponse1, lastBlockInfoResponse2);
    }

    private static LoadBalancer createLoadBalancer() throws Exception {
        ConsensusUri consensusUri =
            new ConsensusUri(Environment.getTestFogConfig().getConsensusUri());
        List<MobileCoinUri> consensusUris = new ArrayList<>();
        consensusUris.add(consensusUri);

        return RandomLoadBalancer.create(consensusUris);
    }

}
