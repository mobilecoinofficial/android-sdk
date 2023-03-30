package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.ConsensusClientService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import attest.Attest;
import consensus_client.ConsensusClientAPIHttp;
import consensus_common.ConsensusCommon;

public class RestConsensusClientService extends RestService implements ConsensusClientService {

    public RestConsensusClientService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public ConsensusCommon.ProposeTxResponse clientTxPropose(Attest.Message request) throws NetworkException {
        try {
            return ConsensusClientAPIHttp.clientTxPropose(request, getRestClient());
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
        }
    }
}
