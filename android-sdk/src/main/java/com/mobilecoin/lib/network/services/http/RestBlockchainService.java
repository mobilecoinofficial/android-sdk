package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.network.services.BlockchainService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import consensus_common.ConsensusCommon;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RestBlockchainService extends RestService implements BlockchainService {
    public static final String SERVICE_NAME = "consensus_common.BlockchainAPI";

    public RestBlockchainService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public ConsensusCommon.LastBlockInfoResponse getLastBlockInfo(Empty request) {
        try {
            byte[] responseData = getRestClient().makeRequest(
                    PREFIX + SERVICE_NAME + "/" + "GetLastBlockInfo",
                    request.toByteArray()
            );
            return ConsensusCommon.LastBlockInfoResponse.parseFrom(responseData);
        } catch (InvalidProtocolBufferException exception) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
        }
    }
}
