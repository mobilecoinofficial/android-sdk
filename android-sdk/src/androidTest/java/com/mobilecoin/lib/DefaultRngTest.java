package com.mobilecoin.lib;

import static org.junit.Assert.assertFalse;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.log.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
public class DefaultRngTest {

    @Test
    public void testNextInt() {
        DefaultRng rng = DefaultRng.createInstance();
        HashMap<Integer, Integer> generated = new HashMap<Integer, Integer>();
        for(int i = 0; i < 1000; i++) {
            int next = rng.nextInt();
            assertFalse(generated.containsKey(next));
            generated.put(next, next);
        }
    }

    @Test
    public void testNextLong() {
        DefaultRng rng = DefaultRng.createInstance();
        HashMap<Long, Long> generated = new HashMap<Long, Long>();
        for(int i = 0; i < 1000000; i++) {
            long next = rng.nextLong();
            assertFalse(generated.containsKey(next));
            generated.put(next, next);
        }
    }

    @Test
    public void testNextBytes() {
        DefaultRng rng = DefaultRng.createInstance();
        HashMap<Byte, Byte> generated = new HashMap<Byte, Byte>();
        for(int i = 0; i < 2; i++) {
            byte next = rng.nextBytes(1)[0];
            assertFalse(generated.containsKey(next));
            generated.put(next, next);
        }
        for(int i = 0; i < 6; i++) {
            assertFalse(Arrays.equals(rng.nextBytes((int)Math.pow(10, i)), rng.nextBytes((int)Math.pow(10, i))));
        }
    }

}
