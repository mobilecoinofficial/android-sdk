// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.SerializationException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static com.mobilecoin.lib.Environment.fogAuthoritySpki;
import static com.mobilecoin.lib.Environment.fogReportId;

@RunWith(AndroidJUnit4.class)
public class PublicAddressTest {
    private final RistrettoPublic key1 = RistrettoPrivate.generateNewKey().getPublicKey();
    private final RistrettoPublic key2 = RistrettoPrivate.generateNewKey().getPublicKey();
    // make sure we have different pub key objects and only underlying key bytes are equal
    private final RistrettoPublic key1_copy = RistrettoPublic.fromBytes(key1.getKeyBytes());
    private final RistrettoPublic key2_copy = RistrettoPublic.fromBytes(key2.getKeyBytes());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public PublicAddressTest() throws SerializationException {
    }

    @Test
    public void test_all_null_arguments() {
        thrown.expect(NullPointerException.class);
        new PublicAddress(
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    public void test_spend_and_fog_null_arguments() {
        thrown.expect(NullPointerException.class);
        new PublicAddress(
                key1,
                null,
                null,
                null,
                null
        );
    }

    @Test
    public void test_view_and_fog_null_arguments() {
        thrown.expect(NullPointerException.class);
        new PublicAddress(
                null,
                key2,
                null,
                null,
                null
        );
    }

    @Test
    public void test_fog_null_argument() {
        thrown.expect(NullPointerException.class);
        new PublicAddress(
                key1,
                key2,
                null,
                fogAuthoritySpki,
                fogReportId
        );
    }

    @Test
    public void test_create_success() {
        new PublicAddress(
                key1,
                key2,
                Environment.FOG_URI,
                fogAuthoritySpki,
                fogReportId
        );
    }

    @Test
    public void test_hashcode() {
        PublicAddress first = new PublicAddress(key1,
                key2,
                Environment.FOG_URI,
                fogAuthoritySpki,
                fogReportId
        );
        PublicAddress second = new PublicAddress(key1_copy,
                key2_copy,
                Environment.FOG_URI,
                fogAuthoritySpki,
                fogReportId
        );
        if (first.hashCode() != second.hashCode()) {
            Assert.fail("Equal objects must have equal hashes");
        }
        second = new PublicAddress(key2,
                key1,
                Environment.FOG_URI,
                fogAuthoritySpki,
                fogReportId
        );
        if (first.hashCode() == second.hashCode()) {
            Assert.fail("Different objects must have different hashes");
        }
        second = new PublicAddress(key1,
                key2,
                Uri.parse("https://test.some.server"),
                fogAuthoritySpki,
                fogReportId
        );
        if (first.hashCode() == second.hashCode()) {
            Assert.fail("Different objects must have different hashes");
        }
    }

    @Test
    public void test_compare() {
        PublicAddress first = new PublicAddress(key1,
                key2,
                Environment.FOG_URI,
                fogAuthoritySpki,
                fogReportId
        );
        PublicAddress second = new PublicAddress(key1_copy,
                key2_copy,
                Environment.FOG_URI,
                fogAuthoritySpki,
                fogReportId
        );
        if (!first.equals(second)) {
            Assert.fail("Public addresses with identical underlying key bytes must be equal");
        }
        // swap keys to make objects not equal
        second = new PublicAddress(key2,
                key1,
                Environment.FOG_URI,
                fogAuthoritySpki,
                fogReportId
        );
        if (first.equals(second)) {
            Assert.fail("Different Public addresses must not be equal");
        }
        second = new PublicAddress(key1,
                key2,
                Uri.parse("https://test.fog.server.com"),
                fogAuthoritySpki,
                fogReportId
        );
        if (first.equals(second)) {
            Assert.fail("Different Public addresses must not be equal");
        }
        try {
            if (first.equals(null)) {
                Assert.fail("Valid object cannot be equal to null");
            }
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void test_fields() {
        PublicAddress address = new PublicAddress(key1,
                key2,
                Environment.FOG_URI,
                fogAuthoritySpki,
                fogReportId
        );
        if (!address.getViewKey().equals(key1) || !address.getSpendKey().equals(key2) ||
                !address.getFogUri().equals(Environment.FOG_URI)) {
            Assert.fail("Getters must return valid keys");
        }
    }

    @Test
    public void test_serialize() throws SerializationException {
        PublicAddress address1 = new PublicAddress(key1,
                key2,
                Environment.FOG_URI,
                fogAuthoritySpki,
                fogReportId
        );
        byte[] serialized = address1.toByteArray();
        PublicAddress address2 = PublicAddress.fromBytes(serialized);
        Assert.assertEquals(
                "Serialized and restored objects must be equal",
                address1,
                address2
        );
    }
}
