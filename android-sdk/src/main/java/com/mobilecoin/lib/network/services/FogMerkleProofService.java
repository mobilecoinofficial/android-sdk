package com.mobilecoin.lib.network.services;

import attest.Attest;

public interface FogMerkleProofService {
    Attest.Message getOutputs(Attest.Message request);
}
