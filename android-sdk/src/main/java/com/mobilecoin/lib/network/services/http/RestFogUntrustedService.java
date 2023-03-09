package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.services.FogUntrustedService;
import com.mobilecoin.lib.network.services.http.clients.RestClient;

import fog_ledger.FogUntrustedTxOutApiHttp;
import fog_ledger.Ledger;

public class RestFogUntrustedService extends RestService implements FogUntrustedService {

    public RestFogUntrustedService(@NonNull RestClient restClient) {
        super(restClient);
    }

    @Override
    public Ledger.TxOutResponse getTxOuts(Ledger.TxOutRequest request) throws NetworkException {
        try {
            return FogUntrustedTxOutApiHttp.getTxOuts(request, getRestClient());
        } catch (InvalidProtocolBufferException exception) {
            throw new NetworkException(NetworkResult.INVALID_ARGUMENT, exception);
        }
    }
}
