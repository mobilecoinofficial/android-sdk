package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.network.services.FogUntrustedService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import fog_ledger.Ledger;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RestFogUntrustedService extends RestService implements FogUntrustedService {
    public static final String SERVICE_NAME = "fog_ledger.FogUntrustedTxOutApi";

    public RestFogUntrustedService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public Ledger.TxOutResponse getTxOuts(Ledger.TxOutRequest request) {
        try {
            byte[] responseData = getRestClient().makeRequest(
                    PREFIX + SERVICE_NAME + "/" + "GetTxOuts",
                    request.toByteArray()
            );
            return Ledger.TxOutResponse.parseFrom(responseData);
        } catch (InvalidProtocolBufferException exception) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
        }
    }
}
