package com.mobilecoin.lib.network.services;

import com.mobilecoin.lib.exceptions.NetworkException;

import fog_ledger.Ledger;

public interface FogUntrustedService {
    Ledger.TxOutResponse getTxOuts(Ledger.TxOutRequest request) throws NetworkException;
}
