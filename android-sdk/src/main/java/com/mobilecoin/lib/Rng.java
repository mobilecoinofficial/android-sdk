package com.mobilecoin.lib;

public interface Rng {

    public int nextInt();

    public long nextLong();

    public byte[] nextBytes(int length);

}
