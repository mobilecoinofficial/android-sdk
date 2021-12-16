package com.mobilecoin.lib.network.services;

import com.mobilecoin.lib.exceptions.NetworkException;

import fog_ledger.Ledger;

public interface FogBlockService {
    Ledger.BlockResponse getBlocks(Ledger.BlockRequest request) throws NetworkException;
}
