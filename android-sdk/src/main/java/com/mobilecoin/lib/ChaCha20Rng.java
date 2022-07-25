package com.mobilecoin.lib;

import java.math.BigInteger;

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
        if(seed.length != SEED_SIZE_BYTES) {
            throw new IllegalArgumentException("seed size: " + SEED_SIZE_BYTES +
                    ". provided: " + seed.length);
        }
        return seed_from_bytes(seed);
    }

    public static ChaCha20Rng withRandomSeed() {
        return fromSeed(DefaultRng.createInstance().nextBytes(SEED_SIZE_BYTES));
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
    public byte[] getSeed() {
        return this.get_seed();
    }

    @Override
    public BigInteger getWordPos() {
        return this.get_word_pos();
    }

    @Override
    public void setWordPos(BigInteger wordPos) {
        byte bigIntBytes[] = wordPos.toByteArray();
        if(bigIntBytes.length > WORD_POS_SIZE_BYTES) {
            throw new IllegalArgumentException("word pos must fit within " + WORD_POS_SIZE_BYTES +
                    " bytes. provided: " + bigIntBytes.length);
        }
        /* We need a 16 byte array to create a u128 in the Rust code.
         * Therefore, we need to pad the beginning of the BigInt with (byte)0 so that it is 16 bytes.
         * Since Java is always big endian, the Rust code is written to expect a big endian array representation.
         */
        byte wordPosBytes[] = new byte[WORD_POS_SIZE_BYTES];
        System.arraycopy(
                bigIntBytes,
                0,
                wordPosBytes,
                // place significant bytes at end of full 16 byte representation
                wordPosBytes.length - bigIntBytes.length,
                bigIntBytes.length);
        this.set_word_pos(wordPosBytes);
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

    private native BigInteger get_word_pos();

    private native void set_word_pos(byte word_pos_bytes[]);

    private static native ChaCha20Rng seed_from_int(int seed);

    private static native ChaCha20Rng seed_from_long(long seed);

    private static native ChaCha20Rng seed_from_bytes(byte seed[]);

    private native void finalize_jni();

    private static final int SEED_SIZE_BYTES = 32;
    private static final int WORD_POS_SIZE_BYTES = 16;

}
