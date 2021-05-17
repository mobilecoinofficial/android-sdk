package com.mobilecoin.lib.network.services;

import fog_ledger.Ledger;

public interface FogBlockService {
    Ledger.BlockResponse getBlocks(Ledger.BlockRequest request);
}
