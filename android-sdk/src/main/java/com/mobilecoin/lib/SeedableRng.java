package com.mobilecoin.lib;

import java.math.BigInteger;

public interface SeedableRng extends Rng {

    public byte[] getSeed();

    public BigInteger getWordPos();

    public void setWordPos(BigInteger wordPos);

}
