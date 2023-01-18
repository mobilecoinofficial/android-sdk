package com.mobilecoin.lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;

@RunWith(AndroidJUnit4.class)
public class DefaultVersionedCryptoBoxTest {

    @Test
    public void testEncryptDecrypt() throws Exception {

        final AccountKey key = TestKeysManager.getNextAccountKey();
        final RistrettoPrivate privateKey = key.getDefaultSubAddressViewKey();
        final RistrettoPublic publicKey = key.getPublicAddress().getViewKey();
        final DefaultVersionedCryptoBox uut = new DefaultVersionedCryptoBox();

        final String plainText = "buy.mobilecoin.com";
        final byte[] plainTextBytes = plainText.getBytes(StandardCharsets.US_ASCII);
        final byte[] encrypted = uut.versionedCryptoBoxEncrypt(publicKey, plainTextBytes);

        final byte[] decryptedBytes = uut.versionedCryptoBoxDecrypt(privateKey, encrypted);
        final String decryptedString = new String(decryptedBytes, StandardCharsets.US_ASCII);

        assertArrayEquals(plainTextBytes, decryptedBytes);
        assertEquals(plainText, decryptedString);

    }

}
