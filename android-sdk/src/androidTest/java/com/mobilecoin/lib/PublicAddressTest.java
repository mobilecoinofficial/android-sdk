// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import android.net.Uri;
import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.SerializationException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PublicAddressTest {
    private final RistrettoPublic key1 = RistrettoPrivate.generateNewKey().getPublicKey();
    private final RistrettoPublic key2 = RistrettoPrivate.generateNewKey().getPublicKey();
    // make sure we have different pub key objects and only underlying key bytes are equal
    private final RistrettoPublic key1_copy = RistrettoPublic.fromBytes(key1.getKeyBytes());
    private final RistrettoPublic key2_copy = RistrettoPublic.fromBytes(key2.getKeyBytes());

    private final TestFogConfig fogConfig = Environment.getTestFogConfig();

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
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
    }

    @Test
    public void test_create_success() {
        new PublicAddress(
                key1,
                key2,
                fogConfig.getFogUri(),
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
    }

    @Test
    public void test_hashcode() {
        Uri fogUri = Uri.parse("fog://some-test-uri");
        Uri differentFogUri = Uri.parse("fog://some-other-test-uri");
        Uri fogUriWithPort = Uri.parse("fog://some-test-uri:443");

        PublicAddress first = new PublicAddress(key1,
                key2,
                fogUri,
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        PublicAddress second = new PublicAddress(key1_copy,
                key2_copy,
                fogUri,
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        assertEquals(
                "Equal objects must have equal hashes",
                first.hashCode(),
                second.hashCode()
        );

        second = new PublicAddress(key2,
                key1,
                fogConfig.getFogUri(),
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        Assert.assertNotEquals(
                "Different objects must have different hashes",
                first.hashCode(),
                second.hashCode()
        );

        second = new PublicAddress(key1,
                key2,
                differentFogUri,
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        Assert.assertNotEquals(
                "Different objects must have different hashes",
                first.hashCode(),
                second.hashCode()
        );

        second = new PublicAddress(key1,
                key2,
                fogUriWithPort,
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        Assert.assertNotEquals(
                "PublicAddresses with different uri ports must have different hashes",
                first.hashCode(),
                second.hashCode()
        );
    }

    @Test
    public void test_compare() {
        Uri fogUri = Uri.parse("fog://some-test-uri");
        Uri differentFogUri = Uri.parse("fog://some-other-test-uri");
        Uri fogUriWithPort = Uri.parse("fog://some-test-uri:443");

        PublicAddress first = new PublicAddress(key1,
                key2,
                fogUri,
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        try {
            if (first.equals(null)) {
                Assert.fail("Valid object cannot be equal to null");
            }
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
        PublicAddress second = new PublicAddress(key1_copy,
                key2_copy,
                fogUri,
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        assertEquals(
                "Public addresses with copied keys must be equal",
                first,
                second
        );

        // swap keys to make objects not equal
        second = new PublicAddress(key2,
                key1,
                fogUri,
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        Assert.assertNotEquals(
                "Different Public addresses must not be equal",
                first,
                second
        );

        second = new PublicAddress(key1,
                key2,
                fogUriWithPort,
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        Assert.assertNotEquals(
                "Public addresses with different ports must not be equal",
                first,
                second
        );

        second = new PublicAddress(key1,
                key2,
                differentFogUri,
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        Assert.assertNotEquals(
                "PublicAddresses with different report uris must not be equal",
                first,
                second
        );

        second = new PublicAddress(key1,
                key2,
                fogUriWithPort,
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );

        if (!first.equivalent(second)) {
            Assert.fail("Public addresses with and without port must be equivalent");
        }
    }

    @Test
    public void test_fields() {
        PublicAddress address = new PublicAddress(key1,
                key2,
                fogConfig.getFogUri(),
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        if (!address.getViewKey().equals(key1) || !address.getSpendKey().equals(key2) ||
                !address.getFogReportUri().equals(fogConfig.getFogUri())) {
            Assert.fail("Getters must return valid keys");
        }
    }

    @Test
    public void test_serialize() throws SerializationException {
        PublicAddress address1 = new PublicAddress(key1,
                key2,
                fogConfig.getFogUri(),
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        byte[] serialized = address1.toByteArray();
        PublicAddress address2 = PublicAddress.fromBytes(serialized);
        assertEquals(
                "Serialized and restored objects must be equal",
                address1,
                address2
        );
    }

    @Test
    public void test_serialize_integrity() throws SerializationException {
        Uri fogUri = Uri.parse("fog://some-test-uri");
        Uri fogUriWithPort = Uri.parse("fog://some-test-uri:443");

        Uri[] fogUrisToTest = new Uri[]{fogUri, fogUriWithPort};

        for (Uri uri : fogUrisToTest) {
            PublicAddress address = new PublicAddress(key1,
                    key2,
                    uri,
                    fogConfig.getFogAuthoritySpki(),
                    fogConfig.getFogReportId()
            );
            byte[] serialized = address.toByteArray();
            PublicAddress restored = PublicAddress.fromBytes(serialized);
            Assert.assertArrayEquals(
                    "Serialized roundtrip bytes must be equal",
                    serialized,
                    restored.toByteArray()
            );
        }
    }

    @Test
    public void test_parcelable() {
        PublicAddress parcelInput = new PublicAddress(key1,
                key2,
                fogConfig.getFogUri(),
                fogConfig.getFogAuthoritySpki(),
                fogConfig.getFogReportId()
        );
        Parcel parcel = Parcel.obtain();
        parcelInput.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        PublicAddress parcelOutput = PublicAddress.CREATOR.createFromParcel(parcel);
        assertEquals(parcelInput, parcelOutput);
    }

}
