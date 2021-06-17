package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.network.services.FogViewService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import attest.Attest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RestFogViewService extends RestService implements FogViewService {
    public static final String SERVICE_NAME = "fog_view.FogViewAPI";

    public RestFogViewService(@NonNull RestClient restClient) {
        super(restClient);
    }

    public Attest.AuthMessage auth(Attest.AuthMessage authMessage) {
        try {
            byte[] responseData = getRestClient().makeRequest(
                    SERVICE_NAME + "/" + "Auth",
                    authMessage.toByteArray()
            );
            return Attest.AuthMessage.parseFrom(responseData);
        } catch (InvalidProtocolBufferException exception) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
        }
    }

    public Attest.Message query(Attest.Message queryMessage) {
        try {
            byte[] responseData = getRestClient().makeRequest(
                    SERVICE_NAME + "/" + "Query",
                    queryMessage.toByteArray()
            );
            return Attest.Message.parseFrom(responseData);
        } catch (InvalidProtocolBufferException exception) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
        }
    }
}
