package com.mobilecoin.lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
public class RngTest {

    @Test
    public void testDefaultRng() {
        Rng rng = DefaultRng.createInstance();
        performRngTests(rng);
    }

    @Test
    public void testChaCha20Rng() {
        SecureRandom seedRng = new SecureRandom();
        byte seedBytes[] = new byte[32];
        seedRng.nextBytes(seedBytes);
        SeedableRng rng1 = ChaCha20Rng.fromSeed(seedBytes);
        SeedableRng rng2 = ChaCha20Rng.fromSeed(seedBytes);
        performSeedableRngTests(rng1, rng2, seedBytes);
    }

    public void performRngTests(@NonNull Rng rng) {
        testNextInt(rng);
        testNextLong(rng);
        testNextBytes(rng);
    }

    public void performSeedableRngTests(@NonNull SeedableRng rng1, @NonNull SeedableRng rng2, @NonNull byte seedBytes[]) {
        assertNotSame("Test requires that two different SeedableRng instances are used.",
                rng1, rng2);
        performRngTests(rng1);
        testGetSeed(rng1, seedBytes);
        assertArrayEquals("SeedableRng seeds must be identical for this test.",
                rng1.getSeed(), rng2.getSeed());
        testReproducibleResultsForSeed(rng1, rng2);
    }

    public void testNextInt(@NonNull final Rng rng) {
        HashMap<Integer, Integer> generated = new HashMap<Integer, Integer>();
        for(int i = 0; i < 10000; i++) {
            int next = rng.nextInt();
            assertNull(generated.put(next, next));
        }
    }

    public void testNextLong(@NonNull final Rng rng) {
        HashMap<Long, Long> generated = new HashMap<Long, Long>();
        for(int i = 0; i < 1000000; i++) {
            long next = rng.nextLong();
            assertNull(generated.put(next, next));
        }
    }

    public void testNextBytes(@NonNull final Rng rng) {
        HashMap<Byte, Byte> generated = new HashMap<Byte, Byte>();
        for(int i = 0; i < 5; i++) {
            byte next = rng.nextBytes(1)[0];
            assertNull(generated.put(next, next));
        }
        generated.clear();
        byte bytes[] = rng.nextBytes(5);
        for(int i = 0; i < 5; i++) {
            assertNull(generated.put(bytes[i], bytes[i]));
        }
        for(int i = 0; i < 6; i++) {
            assertFalse(Arrays.equals(rng.nextBytes((int)Math.pow(10, i)), rng.nextBytes((int)Math.pow(10, i))));
        }
    }

    public void testGetSeed(@NonNull SeedableRng rng, @NonNull byte seedBytes[]) {
        assertArrayEquals(seedBytes, rng.getSeed());
    }

    public void testReproducibleResultsForSeed(
            @NonNull SeedableRng rng1,
            @NonNull SeedableRng rng2
    ) {
        /*for(int i = 0; i < 1000000; i++) {
            assertEquals("Failed during round " + i,
                    rng1.nextInt(), rng2.nextInt());
        }*/
        /*for(int i = 0; i < 1000000; i++) {
            assertEquals("Failed during round " + i,
                    rng1.nextLong(), rng2.nextLong());
        }*/
        for(int i = 0; i < 1000000; i++) {
            assertArrayEquals("Failed during round " + i,
                    rng1.nextBytes(32), rng2.nextBytes(32));
        }
    }

}
