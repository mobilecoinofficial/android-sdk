package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.network.services.AttestedService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import attest.Attest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RestAttestedService extends RestService implements AttestedService {
    public static final String SERVICE_NAME = "attest.AttestedApi";

    public RestAttestedService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public Attest.AuthMessage auth(Attest.AuthMessage authMessage) {
            try {
                byte[] responseData = getRestClient().makeRequest(
                        PREFIX + SERVICE_NAME + "/" + "Auth",
                        authMessage.toByteArray()
                );
                return Attest.AuthMessage.parseFrom(responseData);
            } catch (InvalidProtocolBufferException exception) {
                throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
            }
        }
}
