package com.mobilecoin.lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
public class RngTest {

    private final int INTS_TO_GENERATE = 10000;
    private final int INT_FAILURE_LIMIT = 5;
    private final int LONGS_TO_GENERATE = 1000000;
    private final int LONG_FAILURE_LIMIT = 5;
    private final int BYTES_TO_GENERATE = 5;
    private final int BYTE_FAILURE_LIMIT = 2;
    private final int BYTE_SEQUENCE_MAGNITUDE_LIMIT = 5;

    @Test
    public void testDefaultRng() {
        Rng rng = DefaultRng.createInstance();
        performRngTests(rng);
    }

    @Test
    public void testChaCha20Rng() {
        SecureRandom seedRng = new SecureRandom();
        byte[] seedBytes = new byte[ChaCha20Rng.SEED_SIZE_BYTES];
        seedRng.nextBytes(seedBytes);
        SeedableRng rng1 = ChaCha20Rng.fromSeed(seedBytes);
        SeedableRng rng2 = ChaCha20Rng.fromSeed(seedBytes);
        performSeedableRngTests(rng1, rng2, seedBytes);
    }

    public void performRngTests(@NonNull final Rng rng) {

        testNextInt(rng);

        testNextLong(rng);

        testNextBytes(rng);

    }

    public void performSeedableRngTests(@NonNull final SeedableRng rng1, @NonNull final SeedableRng rng2, @NonNull byte[] seedBytes) {
        assertNotSame("Test requires that two different SeedableRng instances are used.",
                rng1, rng2);

        performRngTests(rng1);

        testGetSeed(rng1, seedBytes);

        testWordPos(rng1, rng2);
        rng2.setWordPos(rng1.getWordPos());

        assertArrayEquals("SeedableRng seeds must be identical for this test.",
                rng1.getSeed(), rng2.getSeed());
        assertEquals("SeedableRng word pos must be identical for this test.",
                rng1.getWordPos(), rng2.getWordPos());
        testReproducibleResultsForSeed(rng1, rng2);
    }

    public void testNextInt(@NonNull final Rng rng) {
        HashMap<Integer, Integer> generated = new HashMap<Integer, Integer>();
        for(int i = 0; i < INTS_TO_GENERATE; i++) {
            int next = rng.nextInt();
            generated.put(next, next);
        }
        assertTrue(INTS_TO_GENERATE - generated.size() + " duplicate ints generated.",
                generated.size() >= (INTS_TO_GENERATE - INT_FAILURE_LIMIT));
    }

    public void testNextLong(@NonNull final Rng rng) {
        HashMap<Long, Long> generated = new HashMap<Long, Long>();
        for(int i = 0; i < LONGS_TO_GENERATE; i++) {
            long next = rng.nextLong();
            generated.put(next, next);
        }
        assertTrue(LONGS_TO_GENERATE - generated.size() + " duplicate longs generated.",
                generated.size() >= (LONGS_TO_GENERATE - LONG_FAILURE_LIMIT));
    }

    public void testNextBytes(@NonNull final Rng rng) {
        HashMap<Byte, Byte> generated = new HashMap<Byte, Byte>();
        for(int i = 0; i < BYTES_TO_GENERATE; i++) {
            byte next = rng.nextBytes(1)[0];
            generated.put(next, next);
        }
        assertTrue(BYTES_TO_GENERATE - generated.size() + " duplicate bytes generated.",
                generated.size() >= (BYTES_TO_GENERATE - BYTE_FAILURE_LIMIT));
        generated.clear();
        byte bytes[] = rng.nextBytes(BYTES_TO_GENERATE);
        for(int i = 0; i < BYTES_TO_GENERATE; i++) {
            generated.put(bytes[i], bytes[i]);
        }
        assertTrue(BYTES_TO_GENERATE - generated.size() + " duplicate bytes generated.",
                generated.size() >= (BYTES_TO_GENERATE - BYTE_FAILURE_LIMIT));
        for(int i = 0; i < BYTE_SEQUENCE_MAGNITUDE_LIMIT; i++) {
            final int byteArrayLength = (int)Math.pow(10, i);
            assertFalse("Duplicate " + byteArrayLength + " bytes generated",
                    Arrays.equals(rng.nextBytes(byteArrayLength), rng.nextBytes(byteArrayLength)));
        }
    }

    public void testGetSeed(@NonNull SeedableRng rng, @NonNull byte seedBytes[]) {
        assertArrayEquals(seedBytes, rng.getSeed());
    }

    public void testWordPos(@NonNull final SeedableRng rng1, @NonNull final SeedableRng rng2) {
        rng2.setWordPos(BigInteger.ZERO);
        assertEquals(BigInteger.ZERO, rng2.getWordPos());
        rng2.setWordPos(BigInteger.ONE);
        assertEquals(BigInteger.ONE, rng2.getWordPos());
        rng2.setWordPos(BigInteger.TEN);
        assertEquals(BigInteger.TEN, rng2.getWordPos());
        rng2.setWordPos(rng1.getWordPos());
        assertEquals(rng1.getWordPos(), rng2.getWordPos());
        rng1.nextInt();
        rng2.nextInt();
        assertEquals(rng1.getWordPos(), rng2.getWordPos());
        rng1.nextLong();
        rng2.nextLong();
        assertEquals(rng1.getWordPos(), rng2.getWordPos());
        rng1.nextBytes(32);
        rng2.nextBytes(32);
        assertEquals(rng1.getWordPos(), rng2.getWordPos());
        rng1.nextInt();
        assertNotEquals(rng1.getWordPos(), rng2.getWordPos());
    }

    public void testReproducibleResultsForSeed(
            @NonNull final SeedableRng rng1,
            @NonNull final SeedableRng rng2
    ) {
        for(int i = 0; i < 1000000; i++) {
            assertEquals("Failed during round " + i,
                    rng1.nextInt(), rng2.nextInt());
        }
        for(int i = 0; i < 1000000; i++) {
            assertEquals("Failed during round " + i,
                    rng1.nextLong(), rng2.nextLong());
        }
        for(int i = 0; i < 1000000; i++) {
            assertArrayEquals("Failed during round " + i,
                    rng1.nextBytes(32), rng2.nextBytes(32));
        }
    }

}
