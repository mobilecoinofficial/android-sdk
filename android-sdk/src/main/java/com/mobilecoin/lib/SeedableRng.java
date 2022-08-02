package com.mobilecoin.lib;

import java.math.BigInteger;

/**
 * This interface represents a seedable RNG.
 *
 * A {@code SeedableRng} is an {@link Rng} which is created by specifying a seed value.
 * An RNG seed is a number used to initialize an RNG. The pseudorandom output of an RNG is a
 * function of this seed and of how much data has already been generated (word pos).
 *
 * The {@code SeedableRng} interface defines methods to set the word pos and get the seed and word pos
 *
 * @see ChaCha20Rng
 * @since 1.2.3
 */
public interface SeedableRng extends Rng {

    /**
     * An RNG seed is a number used to initialize an RNG. The pseudorandom output of an RNG is a
     * function of this seed and of how much data has already been generated. The same RNG using the
     * same seed at the same position (word pos) will always generate the same data.
     *
     * @return the seed used to create this {@code SeedableRng}
     *
     * @see SeedableRng#getWordPos()
     * @see SeedableRng#setWordPos(BigInteger)
     * @since 1.2.3
     */
    public byte[] getSeed();

    /**
     * The word pos of an RNG is an indication of how much data the RNG has generated, or the number
     * of times the RNG has been advanced. It is the position in a sequence of all the numbers the
     * RNG can generate. Two of the same RNG with the same seed and at the same word pos will always
     * generate the same data. This method returns the current word pos of this RNG as a
     * {@link BigInteger}.
     *
     * @return the word pos of this RNG
     *
     * @see SeedableRng#getSeed()
     * @see SeedableRng#setWordPos(BigInteger)
     * @since 1.2.3
     */
    public BigInteger getWordPos();

    /**
     * The word pos of an RNG is an indication of how much data the RNG has generated, or the number
     * of times the RNG has been advanced. It is the position in a sequence of all the numbers the
     * RNG can generate. Two of the same RNG with the same seed and at the same word pos will always
     * generate the same data. This method sets the word pos of this RNG to the specified value
     * provided as a {@link BigInteger}.
     *
     * @param wordPos the word pos to set for this RNG
     *
     * @see SeedableRng#getWordPos()
     * @see SeedableRng#getSeed()
     * @since 1.2.3
     */
    public void setWordPos(BigInteger wordPos);

}
