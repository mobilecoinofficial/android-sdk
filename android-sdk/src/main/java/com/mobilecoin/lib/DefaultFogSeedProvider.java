package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.KexRngException;

import fog_view.View;

public class DefaultFogSeedProvider implements FogSeedProvider {


    @Override
    public FogSeed fogSeedFor(RistrettoPrivate privateViewKey, View.RngRecord rngRecord)
            throws KexRngException {
        return new FogSeed(privateViewKey, rngRecord);
    }

}
