package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.Empty;
import com.mobilecoin.lib.ClientConfig.Service;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.BlockchainService;
import com.mobilecoin.lib.util.NetworkingCall;

import java.math.BigInteger;

import consensus_common.ConsensusCommon;

class BlockchainClient extends AnyClient {
    private static final String TAG = BlockchainClient.class.getName();
    private static final BigInteger DEFAULT_TX_FEE = BigInteger.valueOf(10000000000L);
    private final long minimumFeeCacheTTL_ms;
    private volatile ConsensusCommon.LastBlockInfoResponse lastBlockInfo;
    private long lastBlockInfoTimestamp_ms;

    public static final int TOKEN_ID_BLOCK_VERSION = 2;

    /**
     * Creates and initializes an instance of {@link BlockchainClient}
     *  @param loadBalancer                a uri of the service
     * @param serviceConfig      service configuration passed to MobileCoinClient
     * @param minimumFeeCacheTTL_ms duration of the minimum fee cache lifetime
     */
    BlockchainClient(@NonNull LoadBalancer loadBalancer,
                     @NonNull Service serviceConfig,
                     long minimumFeeCacheTTL_ms,
                     @NonNull TransportProtocol transportProtocol) {
        super(loadBalancer, serviceConfig, transportProtocol);
        this.minimumFeeCacheTTL_ms = minimumFeeCacheTTL_ms;
    }

    /**
     * Fetch or return cached current minimal fee for a specified token
     *
     * @param tokenId the token ID for which to fetch the minimum fee
     */
    @NonNull
    Amount getOrFetchMinimumFee(@NonNull UnsignedLong tokenId) throws NetworkException {
        ConsensusCommon.LastBlockInfoResponse response = getOrFetchLastBlockInfo();
        if((!tokenId.equals(KnownTokenId.MOB.getId())) && (response.getNetworkBlockVersion() < TOKEN_ID_BLOCK_VERSION)) {
            throw(new IllegalArgumentException("Network block version does not support different tokens"));
        }
        long minimumFeeBits = response.getMobMinimumFee();
        if(response.getNetworkBlockVersion() >= 1) {//Needed for compatibility with old networks
            Long minFeeLookup;
            if((minFeeLookup = response.getMinimumFeesMap().get(tokenId.longValue())) == null) {
                throw new IllegalArgumentException("Invalid Token ID");
            }
            minimumFeeBits = minFeeLookup;
        }
        UnsignedLong minimumFee = UnsignedLong.fromLongBits(minimumFeeBits);
        if (minimumFee.equals(UnsignedLong.ZERO)) {
            minimumFee = UnsignedLong.fromBigInteger(DEFAULT_TX_FEE);
        }
        return new Amount(minimumFee.toBigInteger(), tokenId);
    }

    /**
     * Get or fetch and return network block version
     */
    int getOrFetchNetworkBlockVersion() throws NetworkException {
        return getOrFetchLastBlockInfo().getNetworkBlockVersion();
    }

    /**
     * Reset cache
     */
    synchronized void resetCache() {
        lastBlockInfo = null;
        lastBlockInfoTimestamp_ms = 0L;
    }

    /**
     * Fetch or return cached last block info
     */
    @NonNull
    synchronized ConsensusCommon.LastBlockInfoResponse getOrFetchLastBlockInfo() throws NetworkException {
        if (lastBlockInfo == null ||
                lastBlockInfoTimestamp_ms + minimumFeeCacheTTL_ms <= System.currentTimeMillis()) {
            lastBlockInfo = fetchLastBlockInfo();
            lastBlockInfoTimestamp_ms = System.currentTimeMillis();
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
                } catch (NetworkException exception) {
                    Logger.w(TAG, "Unable to post transaction with consensus", exception);
                    throw exception;
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
