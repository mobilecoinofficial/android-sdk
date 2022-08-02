package com.mobilecoin.lib;

/**
 * This class represents the default MobileCoin RNG
 *
 * @see Rng
 * @since 1.2.3
 */
public final class DefaultRng extends Native implements Rng {

    /**
     * Create an instance of DefaultRng
     * The newly created DefaultRng is seeded randomly
     *
     * @return a new instance of DefaultRng
     */
    public static DefaultRng createInstance() {
        return init_jni();
    }

    private DefaultRng(long rustObj) {
        this.rustObj = rustObj;
    }

    @Override
    public int nextInt() {
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
    protected void finalize() throws Throwable {
        if(rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    private static native DefaultRng init_jni();

    private native int next_int();

    private native long next_long();

    private native byte[] next_bytes(int length);

    private native void finalize_jni();

}
