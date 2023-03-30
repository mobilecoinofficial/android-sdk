package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.FogViewService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import attest.Attest;
import fog_view.FogViewAPIHttp;

public class RestFogViewService extends RestService implements FogViewService {

    public RestFogViewService(@NonNull RestClient restClient) {
        super(restClient);
    }

    public Attest.AuthMessage auth(Attest.AuthMessage authMessage) throws NetworkException {
        try {
            return FogViewAPIHttp.auth(authMessage, getRestClient());
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
        }
    }

    public Attest.Message query(Attest.Message queryMessage) throws NetworkException {
        try {
            return FogViewAPIHttp.query(queryMessage, getRestClient());
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
        }
    }
}
