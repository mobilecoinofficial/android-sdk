package com.mobilecoin.lib;

public class DefaultRng extends Native implements Rng {

    public static DefaultRng createInstance() {
        return init_jni();
    }

    private DefaultRng(long rustObj) {
        this.rustObj = rustObj;
    }

    static DefaultRng fromJNI(long rustObj) {
        return new DefaultRng(rustObj);
    }

    @Override
    public int nextInt() {
        return (int)(this.next_long() & Integer.MAX_VALUE);
    }

    @Override
    public long nextLong() {
        return this.next_long();
    }

    @Override
    public byte[] nextBytes(int length) {
        return this.next_bytes(length);
    }

    private static native DefaultRng init_jni();

    private native long next_long();

    private native byte[] next_bytes(int length);

}
