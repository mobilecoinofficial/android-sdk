package com.mobilecoin.lib;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
public class ChaCha20RngTest {

    @Test
    public void testNextInt() {
        SecureRandom seedRng = new SecureRandom();
        byte seedBytes[] = new byte[32];
        seedRng.nextBytes(seedBytes);
        ChaCha20Rng rng = ChaCha20Rng.fromSeed(seedBytes);
        HashMap<Integer, Integer> generated = new HashMap<Integer, Integer>();
        for(int i = 0; i < 10000; i++) {
            int next = rng.nextInt();
            assertNull(generated.put(next, next));
        }
    }

    @Test
    public void testNextLong() {
        SecureRandom seedRng = new SecureRandom();
        byte seedBytes[] = new byte[32];
        seedRng.nextBytes(seedBytes);
        ChaCha20Rng rng = ChaCha20Rng.fromSeed(seedBytes);
        HashMap<Long, Long> generated = new HashMap<Long, Long>();
        for(int i = 0; i < 1000000; i++) {
            long next = rng.nextLong();
            assertNull(generated.put(next, next));
        }
    }

    @Test
    public void testNextBytes() {
        SecureRandom seedRng = new SecureRandom();
        byte seedBytes[] = new byte[32];
        seedRng.nextBytes(seedBytes);
        ChaCha20Rng rng = ChaCha20Rng.fromSeed(seedBytes);
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

    @Test
    public void testGetSeed() {
        SecureRandom seedRng = new SecureRandom();
        byte seedBytes[] = new byte[32];
        seedRng.nextBytes(seedBytes);
        ChaCha20Rng rng = ChaCha20Rng.fromSeed(seedBytes);
        assertTrue(Arrays.equals(seedBytes, rng.getSeed()));
    }

}
