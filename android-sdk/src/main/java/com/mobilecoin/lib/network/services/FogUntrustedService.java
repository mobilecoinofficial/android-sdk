package com.mobilecoin.lib.network.services;

import fog_ledger.Ledger;

public interface FogUntrustedService {
    Ledger.TxOutResponse getTxOuts(Ledger.TxOutRequest request);
}
