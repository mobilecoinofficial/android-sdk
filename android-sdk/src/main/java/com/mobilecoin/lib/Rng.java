package com.mobilecoin.lib;

/**
 * This interface represents a Random Number Generator (RNG)
 * An RNG can be used to generate a sequence of pseudorandom numbers
 *
 * @see DefaultRng
 * @see SeedableRng
 * @since 1.2.3
 */
public interface Rng {

    /**
     * Generates the next 4 bytes, advancing the RNG and returning it as an integer
     *
     * @return the next integer from this RNG
     * @since 1.2.3
     */
    public int nextInt();

    /**
     * Generates the next 8 bytes, advancing the RNG and returning it as a long
     *
     * @return the next long from this RNG
     * @since 1.2.3
     */
    public long nextLong();

    /**
     * Generates the next N bytes, advancing the RNG and returning it as a byte array
     *
     * @param length the length of the array to generate
     * @return the next N bytes from this RNG
     * @since 1.2.3
     */
    public byte[] nextBytes(int length);

}
