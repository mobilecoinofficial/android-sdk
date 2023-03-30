package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.FogKeyImageService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import attest.Attest;
import fog_ledger.FogKeyImageAPIHttp;

public class RestFogKeyImageService extends RestService implements FogKeyImageService {

    public RestFogKeyImageService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public Attest.AuthMessage auth(Attest.AuthMessage authMessage) throws NetworkException {
        try {
            return FogKeyImageAPIHttp.auth(authMessage, getRestClient());
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
        }
    }

    @Override
    public Attest.Message checkKeyImages(Attest.Message request) throws NetworkException {
        try {
            return FogKeyImageAPIHttp.checkKeyImages(request, getRestClient());
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
        }
    }
}
