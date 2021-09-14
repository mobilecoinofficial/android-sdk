package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.network.services.ConsensusClientService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import attest.Attest;
import consensus_common.ConsensusCommon;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RestConsensusClientService extends RestService implements ConsensusClientService {
    public static final String SERVICE_NAME = "consensus_client.ConsensusClientAPI";

    public RestConsensusClientService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public ConsensusCommon.ProposeTxResponse clientTxPropose(Attest.Message request) {
        try {
            byte[] responseData = getRestClient().makeRequest(
                    PREFIX + SERVICE_NAME + "/" + "ClientTxPropose",
                    request.toByteArray()
            );
            return ConsensusCommon.ProposeTxResponse.parseFrom(responseData);
        } catch (InvalidProtocolBufferException exception) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
        }
    }
}
