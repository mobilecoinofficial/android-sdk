package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.FogBlockService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import fog_ledger.FogBlockAPIHttp;
import fog_ledger.Ledger;

public class RestFogBlockService extends RestService implements FogBlockService {

    public RestFogBlockService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public Ledger.BlockResponse getBlocks(Ledger.BlockRequest request) throws NetworkException {
        try {
            return FogBlockAPIHttp.getBlocks(request, getRestClient());
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
        }
    }
}
