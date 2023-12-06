// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static junit.framework.Assert.fail;

import com.mobilecoin.lib.exceptions.SerializationException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PublicKeyTest {
    private final RistrettoPrivate privateKey = RistrettoPrivate.generateNewKey();
    private final byte[] pubKeyBytes = privateKey.getPublicKey().getKeyBytes();
    @Test
    public void test_create_new() throws SerializationException {
        RistrettoPublic.fromBytes(pubKeyBytes);
    }

    @Test
    public void test_create_from_garbage_bytes() {
       try {
           byte[] garbage = new byte[RistrettoPublic.PUBLIC_KEY_SIZE];
           for (int i = 0; i < RistrettoPublic.PUBLIC_KEY_SIZE; ++i) {
               garbage[i] = (byte) i;
           }
           RistrettoPublic.fromBytes(garbage);
       } catch(SerializationException e) {
           return;// pass
       }
       fail("Expected SerializationException");
    }

    @Test
    public void test_create_from_null() throws Exception {
        try {
            RistrettoPublic.fromBytes(null);
        } catch(NullPointerException e) {
            return;// pass
        }
        fail("Expected NullPointerException");
    }

    @Test
    public void test_create_from_illegal_size() {
        try {
            RistrettoPublic.fromBytes(new byte[1]);
        } catch(SerializationException e) {
            return;// pass
        }
        fail("Expected SerializationException");
    }

    @Test
    public void test_hashcode() throws SerializationException {
        RistrettoPublic key1 = RistrettoPublic.fromBytes(pubKeyBytes);
        RistrettoPublic key2 = RistrettoPublic.fromBytes(pubKeyBytes);

        if (key1.hashCode() != key2.hashCode()) {
            Assert.fail("Equal objects must have equal hashes");
        }

        RistrettoPrivate randomKey = RistrettoPrivate.generateNewKey();
        RistrettoPublic key3 = randomKey.getPublicKey();
        if (key1.hashCode() == key3.hashCode()) {
            Assert.fail("Different objects must have different hashes");
        }
    }

    @Test
    public void test_serialize() throws SerializationException {
        byte[] serializedPubKey = RistrettoPublic.fromBytes(pubKeyBytes).getKeyBytes();
        Assert.assertArrayEquals(
                serializedPubKey,
                pubKeyBytes
        );
    }

    @Test
    public void test_compare() throws SerializationException {
        RistrettoPublic key1 = RistrettoPublic.fromBytes(pubKeyBytes);
        RistrettoPublic key2 = RistrettoPublic.fromBytes(pubKeyBytes);

        if (!key1.equals(key2)) {
            Assert.fail("Keys with identical underlying bytes must be equal");
        }

        RistrettoPrivate randomKey = RistrettoPrivate.generateNewKey();
        RistrettoPublic key3 = randomKey.getPublicKey();
        if (key1.equals(key3)) {
            Assert.fail("Different Public keys must not be equal");
        }
        try {
            if (key1.equals(null)) {
                Assert.fail("Valid object cannot be equal to null");
            }
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }
}
