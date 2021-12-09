package com.mobilecoin.lib.network.services;

import com.mobilecoin.lib.exceptions.NetworkException;

import attest.Attest;

public interface FogMerkleProofService {
    Attest.Message getOutputs(Attest.Message request) throws NetworkException;
}
