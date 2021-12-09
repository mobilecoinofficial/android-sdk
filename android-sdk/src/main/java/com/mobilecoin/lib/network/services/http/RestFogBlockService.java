package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.FogBlockService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import fog_ledger.Ledger;

public class RestFogBlockService extends RestService implements FogBlockService {
    public static final String SERVICE_NAME = "fog_ledger.FogBlockAPI";

    public RestFogBlockService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public Ledger.BlockResponse getBlocks(Ledger.BlockRequest request) throws NetworkException {
        try {
            byte[] responseData = getRestClient().makeRequest(
                    PREFIX + SERVICE_NAME + "/" + "GetBlocks",
                    request.toByteArray()
            );
            return Ledger.BlockResponse.parseFrom(responseData);
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
        }
    }
}
