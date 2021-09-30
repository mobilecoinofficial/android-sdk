package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.Empty;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.services.BlockchainService;
import com.mobilecoin.lib.network.services.ServiceAPIManager;
import com.mobilecoin.lib.network.uri.ConsensusUri;
import com.mobilecoin.lib.network.uri.FogUri;
import com.mobilecoin.lib.util.NetworkingCall;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;

import consensus_common.ConsensusCommon;
import io.grpc.StatusRuntimeException;

final class BlockchainClient extends AnyClient {
    private static final String TAG = BlockchainClient.class.getName();
    private static final BigInteger DEFAULT_TX_FEE = BigInteger.valueOf(10000000000L);
    private final Duration minimumFeeCacheTTL;
    private ConsensusCommon.LastBlockInfoResponse lastBlockInfo;
    private LocalDateTime lastBlockInfoTimestamp;

    /**
     * Creates and initializes an instance of {@link BlockchainClient}
     *
     * @param uri                a uri of the service
     * @param serviceConfig      service configuration passed to MobileCoinClient
     * @param minimumFeeCacheTTL duration of the minimum fee cache lifetime
     */
    BlockchainClient(@NonNull ConsensusUri uri,
                     @NonNull ClientConfig.Service serviceConfig,
                     @NonNull Duration minimumFeeCacheTTL) {
        super(uri, serviceConfig);
        this.minimumFeeCacheTTL = minimumFeeCacheTTL;
    }

    BlockchainClient(@NonNull ConsensusUri uri,
                   @NonNull ClientConfig.Service serviceConfig,
                   @NonNull Duration minimumFeeCacheTTL,
                   @NonNull ServiceAPIManager apiManager) {
        super(uri, serviceConfig, apiManager);
        this.minimumFeeCacheTTL = minimumFeeCacheTTL;
        Logger.i(TAG, "Created new BlockchainClient", null,
                "uri:", uri,
                "verifier:", serviceConfig,
                "apiManager:", apiManager);
    }

    /**
     * Fetch or return cached current minimal fee
     */
    @NonNull
    synchronized UnsignedLong getOrFetchMinimumFee() throws NetworkException {
        ConsensusCommon.LastBlockInfoResponse response = getOrFetchLastBlockInfo();
        long minimumFeeBits = response.getMinimumFee();
        UnsignedLong minimumFee = UnsignedLong.fromLongBits(minimumFeeBits);
        if (minimumFee.equals(UnsignedLong.ZERO)) {
            minimumFee = UnsignedLong.fromBigInteger(DEFAULT_TX_FEE);
        }
        return minimumFee;
    }

    /**
     * Reset cache
     */
    synchronized void resetCache() {
        lastBlockInfo = null;
        lastBlockInfoTimestamp = null;
    }

    /**
     * Fetch or return cached last block info
     */
    @NonNull
    synchronized ConsensusCommon.LastBlockInfoResponse getOrFetchLastBlockInfo() throws NetworkException {
        if (lastBlockInfo == null ||
                lastBlockInfoTimestamp
                        .plus(minimumFeeCacheTTL)
                        .compareTo(LocalDateTime.now()) <= 0) {
            lastBlockInfo = fetchLastBlockInfo();
            lastBlockInfoTimestamp = LocalDateTime.now();
        }
        return lastBlockInfo;
    }

    /**
     * Fetch last block info
     */
    @NonNull
    ConsensusCommon.LastBlockInfoResponse fetchLastBlockInfo()
            throws NetworkException {
        Logger.i(TAG, "Fetching last block info via Blockchain API");
        NetworkingCall<ConsensusCommon.LastBlockInfoResponse> networkingCall;
        try {
            BlockchainService blockchainService =
                    getAPIManager().getBlockchainService(getNetworkTransport());
            networkingCall = new NetworkingCall<>(() -> {
                try {
                    return blockchainService.getLastBlockInfo(Empty.newBuilder().build());
                } catch (StatusRuntimeException exception) {
                    Logger.w(TAG, "Unable to post transaction with consensus", exception);
                    throw new NetworkException(exception);
                }
            });
        } catch (AttestationException exception) {
            throw new IllegalStateException("BUG", exception);
        }
        ConsensusCommon.LastBlockInfoResponse response;
        try {
            response = networkingCall.run();
        } catch (NetworkException | RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("BUG: unreachable code");
        }
        return response;
    }
}
