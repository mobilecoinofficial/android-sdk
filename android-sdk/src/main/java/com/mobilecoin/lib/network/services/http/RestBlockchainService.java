package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.BlockchainService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import consensus_common.BlockchainAPIHttp;
import consensus_common.ConsensusCommon;

public class RestBlockchainService extends RestService implements BlockchainService {

    public RestBlockchainService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public ConsensusCommon.LastBlockInfoResponse getLastBlockInfo(Empty request) throws NetworkException {
        try {
            return BlockchainAPIHttp.getLastBlockInfo(request, getRestClient());
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
        }
    }
}
