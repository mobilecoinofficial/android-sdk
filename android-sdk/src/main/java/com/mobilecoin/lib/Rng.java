package com.mobilecoin.lib;

public interface Rng {

    int nextInt();

    long nextLong();

    byte[] nextBytes(int length);

}
