// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.SerializationException;

import org.junit.Assert;
import org.junit.Test;

import java.security.SecureRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.os.Parcel;

public class RistrettoPrivateTest {

    @Test
    public void generate_new_key() {
        try {
            RistrettoPrivate.generateNewKey();
        } catch (Exception e) {
            Assert.fail("Key generation failed with Exception: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void check_equals() {
        RistrettoPrivate key1 = RistrettoPrivate.generateNewKey();
        RistrettoPrivate key2 = RistrettoPrivate.generateNewKey();
        if (key1.equals(key2)) {
            Assert.fail("Two random keys cannot be equal");
        }
        // type mismatch must return false
        if (key1.equals(new Object())) {
            Assert.fail("Different objects must not be equal");
        }
    }

    @Test
    public void check_equal_seeds() throws SerializationException {
        byte[] seed = new byte[32];
        new SecureRandom().nextBytes(seed);
        RistrettoPrivate key1 = RistrettoPrivate.generateNewKey(seed);
        RistrettoPrivate key2 = RistrettoPrivate.generateNewKey(seed);
        if (!key1.equals(key2)) {
            Assert.fail("Keys generated from the same seed must be different");
        }
        // generate new seed and a new key
        new SecureRandom().nextBytes(seed);
        key2 = RistrettoPrivate.generateNewKey(seed);
        if (key1.equals(key2)) {
            Assert.fail("Keys generated from different seeds must not be equal");
        }
    }
    @Test
    public void check_finalizers() {
        try {
            RistrettoPrivate.generateNewKey();
            System.gc();
        } catch (Exception ignored) {
            Assert.fail("Finalizer must complete successfully");
        }
    }
    @Test
    public void check_raw_key_bytes() throws SerializationException {
        RistrettoPrivate key1 = RistrettoPrivate.generateNewKey();
        RistrettoPrivate key2 = RistrettoPrivate.generateNewKey();
        int hash1 = key1.hashCode();
        int hash2 = key2.hashCode();
        assertNotEquals(
                "Random keys must not be equal",
                hash1,
                hash2
        );
        // swapping underlying key bytes to check if get/set raw bytes work
        byte[] key1_bytes = key1.getKeyBytes().clone();
        byte[] key2_bytes = key2.getKeyBytes().clone();
        key1 = RistrettoPrivate.fromBytes(key2_bytes);
        key2 = RistrettoPrivate.fromBytes(key1_bytes);
        assertNotEquals(
                "Swapped keys must not be equal",
                key1.hashCode(),
                key2.hashCode()
        );
        assertEquals(
                "Key1 hash must correspond to now swapped key2",
                hash1,
                key2.hashCode()
        );
        assertEquals(
                "Key2 hash must correspond to now swapped key1",
                hash2,
                key1.hashCode()
        );
    }

    @Test
    public void fromBytes() throws SerializationException {
        RistrettoPrivate key1 = RistrettoPrivate.generateNewKey();
        RistrettoPrivate key2 = RistrettoPrivate.fromBytes(key1.getKeyBytes());
        assertEquals(
                "Cloned key must have the same has codes",
                key1.hashCode(),
                key2.hashCode()
        );
        if (!key1.equals(key2)) {
            Assert.fail("Cloned keys must be equal");
        }
    }

    @Test
    public void test_parcelable() throws SerializationException {
        RistrettoPrivate parcelInput = RistrettoPrivate.fromBytes(new byte[32]);
        Parcel parcel = Parcel.obtain();
        parcelInput.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        RistrettoPrivate parcelOutput = RistrettoPrivate.CREATOR.createFromParcel(parcel);
        assertEquals(parcelInput, parcelOutput);
    }

}
