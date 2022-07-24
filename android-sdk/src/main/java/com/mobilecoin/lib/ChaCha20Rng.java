package com.mobilecoin.lib;

public class ChaCha20Rng extends Native implements SeedableRng {

    private ChaCha20Rng(long rustObj) {
        this.rustObj = rustObj;
    }

    public static ChaCha20Rng fromIntSeed(int seed) {
        return seed_from_int(seed);
    }

    public static ChaCha20Rng fromLongSeed(long seed) {
        return seed_from_long(seed);
    }

    public static ChaCha20Rng fromSeed(byte seed[]) {
        return seed_from_bytes(seed);
    }

    @Override
    public int nextInt() {
        //return (int)(this.next_long() & (int)(-1));
        return this.next_int();
    }

    @Override
    public long nextLong() {
        return this.next_long();
    }

    @Override
    public byte[] nextBytes(int length) {
        return this.next_bytes(length);
    }

    @Override
    public byte[] getSeed() {
        return this.get_seed();
    }

    protected void finalize() throws Throwable {
        if(this.rustObj != 0) {
            this.finalize_jni();
        }
        super.finalize();
    }

    private native int next_int();

    private native long next_long();

    private native byte[] next_bytes(int length);

    private native byte[] get_seed();

    private static native ChaCha20Rng seed_from_int(int seed);

    private static native ChaCha20Rng seed_from_long(long seed);

    private static native ChaCha20Rng seed_from_bytes(byte seed[]);

    private native void finalize_jni();

}
