package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.FogMerkleProofService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import attest.Attest;

public class RestFogMerkleProofService extends RestService implements FogMerkleProofService {
    public static final String SERVICE_NAME = "fog_ledger.FogMerkleProofAPI";

    public RestFogMerkleProofService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public Attest.Message getOutputs(Attest.Message request) throws NetworkException {
        try {
            byte[] responseData = getRestClient().makeRequest(
                    PREFIX + SERVICE_NAME + "/" + "GetOutputs",
                    request.toByteArray()
            );
            return Attest.Message.parseFrom(responseData);
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT);
        }
    }
}
