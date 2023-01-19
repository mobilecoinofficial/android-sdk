package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.KexRngException;

import fog_view.View;

public interface FogSeedProvider {

    public FogSeed fogSeedFor(RistrettoPrivate privateViewKey, View.RngRecord rngRecord)
            throws KexRngException;

    public ViewFogSeed viewFogSeedFor(RistrettoPrivate privateViewKey, View.RngRecord rngRecord)
            throws KexRngException;


}
