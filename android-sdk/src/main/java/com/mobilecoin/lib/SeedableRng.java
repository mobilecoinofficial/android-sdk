package com.mobilecoin.lib;

public interface SeedableRng extends Rng {

    public byte[] getSeed();

}
