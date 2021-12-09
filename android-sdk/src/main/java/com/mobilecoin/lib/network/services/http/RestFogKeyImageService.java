package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.FogKeyImageService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import attest.Attest;

public class RestFogKeyImageService extends RestService implements FogKeyImageService {
    public static final String SERVICE_NAME = "fog_ledger.FogKeyImageAPI";

    public RestFogKeyImageService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public Attest.AuthMessage auth(Attest.AuthMessage authMessage) throws NetworkException {
        try {
            byte[] responseData = getRestClient().makeRequest(
                    PREFIX + SERVICE_NAME + "/" + "Auth",
                    authMessage.toByteArray()
            );
            return Attest.AuthMessage.parseFrom(responseData);
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
        }
    }

    @Override
    public Attest.Message checkKeyImages(Attest.Message request) throws NetworkException {
        try {
            byte[] responseData = getRestClient().makeRequest(
                    PREFIX + SERVICE_NAME + "/" + "CheckKeyImages",
                    request.toByteArray()
            );
            return Attest.Message.parseFrom(responseData);
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
        }
    }
}
