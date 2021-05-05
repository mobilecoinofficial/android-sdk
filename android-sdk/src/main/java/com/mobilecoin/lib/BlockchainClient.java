package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.google.protobuf.Empty;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.uri.ConsensusUri;
import com.mobilecoin.lib.util.NetworkingCall;

import java.math.BigInteger;

import consensus_common.BlockchainAPIGrpc;
import consensus_common.ConsensusCommon;
import io.grpc.StatusRuntimeException;

class BlockchainClient extends AnyClient {
    private static final String TAG = BlockchainClient.class.getName();
    private static final BigInteger DEFAULT_TX_FEE = BigInteger.valueOf(10000000000L);
    private ConsensusCommon.LastBlockInfoResponse lastBlockInfo;

    /**
     * Creates and initializes an instance of {@link BlockchainClient}
     *
     * @param uri           a uri of the service
     * @param serviceConfig service configuration passed to MobileCoinClient
     */
    BlockchainClient(@NonNull ConsensusUri uri,
                     @NonNull ClientConfig.Service serviceConfig) {
        super(uri.getUri(), serviceConfig);
    }

    /**
     * Fetch or return cached current minimal fee
     */
    UnsignedLong getOrFetchMinimumFee() throws NetworkException {
        ConsensusCommon.LastBlockInfoResponse response = getOrFetchLastBlockInfo();
        long minimumFeeBits = response.getMinimumFee();
        UnsignedLong minimumFee = UnsignedLong.fromLongBits(minimumFeeBits);
        if (minimumFee.equals(UnsignedLong.ZERO)) {
            minimumFee = UnsignedLong.fromBigInteger(DEFAULT_TX_FEE);
        }
        return minimumFee;
    }

    /**
     * Fetch or return cached last block info
     */
    synchronized
    ConsensusCommon.LastBlockInfoResponse getOrFetchLastBlockInfo() throws NetworkException {
        if (lastBlockInfo == null) {
            lastBlockInfo = fetchLastBlockInfo();
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
            BlockchainAPIGrpc.BlockchainAPIBlockingStub blockchainAPIStub =
                    getAPIManager().getBlockchainAPIStub(getManagedChannel());
            networkingCall = new NetworkingCall<>(() -> {
                        try {
                            return blockchainAPIStub.getLastBlockInfo(Empty.newBuilder().build());
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
