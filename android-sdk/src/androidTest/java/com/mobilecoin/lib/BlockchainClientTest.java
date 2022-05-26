package com.mobilecoin.lib;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mobilecoin.lib.network.services.BlockchainService;
import com.mobilecoin.lib.network.services.ServiceAPIManager;
import com.mobilecoin.lib.network.uri.ConsensusUri;
import com.mobilecoin.lib.network.uri.MobileCoinUri;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import consensus_common.ConsensusCommon;

public class BlockchainClientTest {
    @Test
    public void clientCachesLastBlockInfo() throws Exception {
        TestFogConfig fogConfig = Environment.getTestFogConfig();
        ConsensusUri consensusUri = new ConsensusUri(
                fogConfig.getConsensusUri());
        BlockchainClient blockchainClient = new BlockchainClient(
                RandomLoadBalancer.create(consensusUri),
                fogConfig.getClientConfig().consensus,
                Duration.ofHours(1).toMillis(),
                fogConfig.getTransportProtocol());
        blockchainClient.setAuthorization(
                fogConfig.getUsername(),
                fogConfig.getPassword()
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
                        .putMinimumFees(0, 400000000L)
                        .build()
        ).thenReturn(
                ConsensusCommon.LastBlockInfoResponse.newBuilder()
                        .setIndex(2)
                        .putMinimumFees(0, 400000000L)
                        .build()
        );
        when(apiManager.getBlockchainService(any())).thenReturn(blockchainService);

        TestFogConfig fogConfig = Environment.getTestFogConfig();
        // Setup blockchain client
        BlockchainClient blockchainClient = new BlockchainClient(
                createLoadBalancer(),
                fogConfig.getClientConfig().consensus,
                Duration.ofHours(1).toMillis(),
                fogConfig.getTransportProtocol());

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
        TestFogConfig fogConfig = Environment.getTestFogConfig();
        ConsensusUri consensusUri = new ConsensusUri(
                fogConfig.getConsensusUri());
        BlockchainClient blockchainClient = new BlockchainClient(
                RandomLoadBalancer.create(consensusUri),
                fogConfig.getClientConfig().consensus,
                Duration.ofMillis(1).toMillis(),
                fogConfig.getTransportProtocol());
        blockchainClient.setAuthorization(
                fogConfig.getUsername(),
                fogConfig.getPassword()
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
                        .putMinimumFees(0, 400000000L)
                        .build()
        ).thenReturn(
                ConsensusCommon.LastBlockInfoResponse.newBuilder()
                        .setIndex(1)
                        .putMinimumFees(0, 400000000L)
                        .build()
        );
        when(apiManager.getBlockchainService(any())).thenReturn(blockchainService);

        // Setup blockchain client
        TestFogConfig fogConfig = Environment.getTestFogConfig();
        BlockchainClient blockchainClient = new BlockchainClient(
                createLoadBalancer(),
                fogConfig.getClientConfig().consensus,
                Duration.ofMillis(1).toMillis(),
                fogConfig.getTransportProtocol());

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
