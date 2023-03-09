package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.AttestedService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import attest.Attest;
import attest.AttestedApiHttp;

public class RestAttestedService extends RestService implements AttestedService {

    public RestAttestedService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public Attest.AuthMessage auth(Attest.AuthMessage authMessage) throws NetworkException {
            try {
                return AttestedApiHttp.auth(authMessage, getRestClient());
            } catch (InvalidProtocolBufferException exception) {
                throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
            }
        }
}
