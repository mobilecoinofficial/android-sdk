// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.SerializationException;

import org.junit.Assert;
import org.junit.Test;

public class AccountTest {
    private final TestFogConfig fogConfig = Environment.getTestFogConfig();

    private static final byte[] viewPublicKeyBytes = {
            (byte) 120, (byte) 144, (byte) 28, (byte) 17, (byte) 131, (byte) 78, (byte) 132,
            (byte) 45, (byte) 246, (byte) 190, (byte) 152, (byte) 193, (byte) 39, (byte) 149,
            (byte) 53, (byte) 19, (byte) 225, (byte) 22, (byte) 9, (byte) 93, (byte) 65, (byte) 34,
            (byte) 204, (byte) 39, (byte) 216, (byte) 7, (byte) 180, (byte) 81, (byte) 28,
            (byte) 184, (byte) 30, (byte) 17
    };
    private static final byte[] spendPublicKeyBytes = {
            (byte) 246, (byte) 47, (byte) 241, (byte) 195, (byte) 58, (byte) 34, (byte) 133,
            (byte) 177, (byte) 174, (byte) 230, (byte) 18, (byte) 237, (byte) 135, (byte) 40,
            (byte) 31, (byte) 44, (byte) 104, (byte) 187, (byte) 43, (byte) 91, (byte) 15,
            (byte) 20, (byte) 57, (byte) 161, (byte) 62, (byte) 74, (byte) 183, (byte) 40,
            (byte) 249, (byte) 142, (byte) 251, (byte) 106
    };
    private static final byte[] viewPrivateKeyBytes = {
            (byte) 176, (byte) 20, (byte) 109, (byte) 232, (byte) 205, (byte) 143, (byte) 91,
            (byte) 121, (byte) 98, (byte) 249, (byte) 231, (byte) 74, (byte) 94, (byte) 240,
            (byte) 243, (byte) 229, (byte) 138, (byte) 149, (byte) 80, (byte) 201, (byte) 82,
            (byte) 122, (byte) 193, (byte) 68, (byte) 243, (byte) 135, (byte) 41, (byte) 240,
            (byte) 253, (byte) 63, (byte) 237, (byte) 14
    };
    private static final byte[] spendPrivateKeyBytes = {
            (byte) 180, (byte) 191, (byte) 1, (byte) 167, (byte) 126, (byte) 212, (byte) 224,
            (byte) 101, (byte) 233, (byte) 8, (byte) 45, (byte) 75, (byte) 218, (byte) 103,
            (byte) 173, (byte) 211, (byte) 12, (byte) 136, (byte) 224, (byte) 33, (byte) 220,
            (byte) 248, 31, (byte) 200, 78, 106, (byte) 156, (byte) 162, (byte) 203, 104,
            (byte) 225, 7
    };
    private static final byte[] serializedAccount = {
            (byte) 0x0a, (byte) 0x22, (byte) 0x0a, (byte) 0x20, (byte) 0xb0, (byte) 0x14,
            (byte) 0x6d, (byte) 0xe8, (byte) 0xcd, (byte) 0x8f, (byte) 0x5b, (byte) 0x79,
            (byte) 0x62, (byte) 0xf9, (byte) 0xe7, (byte) 0x4a, (byte) 0x5e, (byte) 0xf0,
            (byte) 0xf3, (byte) 0xe5, (byte) 0x8a, (byte) 0x95, (byte) 0x50, (byte) 0xc9,
            (byte) 0x52, (byte) 0x7a, (byte) 0xc1, (byte) 0x44, (byte) 0xf3, (byte) 0x87,
            (byte) 0x29, (byte) 0xf0, (byte) 0xfd, (byte) 0x3f, (byte) 0xed, (byte) 0x0e,
            (byte) 0x12, (byte) 0x22, (byte) 0x0a, (byte) 0x20, (byte) 0xb4, (byte) 0xbf,
            (byte) 0x01, (byte) 0xa7, (byte) 0x7e, (byte) 0xd4, (byte) 0xe0, (byte) 0x65,
            (byte) 0xe9, (byte) 0x08, (byte) 0x2d, (byte) 0x4b, (byte) 0xda, (byte) 0x67,
            (byte) 0xad, (byte) 0xd3, (byte) 0x0c, (byte) 0x88, (byte) 0xe0, (byte) 0x21,
            (byte) 0xdc, (byte) 0xf8, (byte) 0x1f, (byte) 0xc8, (byte) 0x4e, (byte) 0x6a,
            (byte) 0x9c, (byte) 0xa2, (byte) 0xcb, (byte) 0x68, (byte) 0xe1, (byte) 0x07,
            (byte) 0x1a, (byte) 0x2d, (byte) 0x66, (byte) 0x6f, (byte) 0x67, (byte) 0x3a,
            (byte) 0x2f, (byte) 0x2f, (byte) 0x66, (byte) 0x6f, (byte) 0x67, (byte) 0x2d,
            (byte) 0x72, (byte) 0x65, (byte) 0x70, (byte) 0x6f, (byte) 0x72, (byte) 0x74,
            (byte) 0x2e, (byte) 0x6d, (byte) 0x6f, (byte) 0x62, (byte) 0x69, (byte) 0x6c,
            (byte) 0x65, (byte) 0x64, (byte) 0x65, (byte) 0x76, (byte) 0x2e, (byte) 0x6d,
            (byte) 0x6f, (byte) 0x62, (byte) 0x69, (byte) 0x6c, (byte) 0x65, (byte) 0x63,
            (byte) 0x6f, (byte) 0x69, (byte) 0x6e, (byte) 0x2e, (byte) 0x63, (byte) 0x6f,
            (byte) 0x6d, (byte) 0x3a, (byte) 0x34, (byte) 0x34, (byte) 0x33
    };

    @Test
    public void test_serialize() throws SerializationException, InvalidUriException {
        AccountKey accountKey1 = AccountKey.createNew(fogConfig.getFogUri(),
                fogConfig.getFogReportId(),
                fogConfig.getFogAuthoritySpki()
        );
        byte[] serialized = accountKey1.toByteArray();
        AccountKey accountKey2 = AccountKey.fromBytes(serialized);
        Assert.assertEquals("Serialized and restored accounts must be equal",
                accountKey1,
                accountKey2
        );
    }

    @Test
    public void test_deserialize() throws SerializationException, InvalidUriException {
        AccountKey accountKey = AccountKey.fromBytes(serializedAccount);

        RistrettoPrivate restoredViewKey = RistrettoPrivate.fromBytes(viewPrivateKeyBytes);
        RistrettoPrivate restoredSpendKey = RistrettoPrivate.fromBytes(spendPrivateKeyBytes);

        Assert.assertEquals(
                restoredViewKey,
                accountKey.getViewKey()
        );
        Assert.assertEquals(
                restoredSpendKey,
                accountKey.getSpendKey()
        );

        RistrettoPublic restoredViewPublicKey = RistrettoPublic.fromBytes(viewPublicKeyBytes);
        RistrettoPublic restoredSpendPublicKey = RistrettoPublic.fromBytes(spendPublicKeyBytes);

        Assert.assertEquals(
                restoredViewPublicKey,
                accountKey.getSubAddressViewKey().getPublicKey()
        );
        Assert.assertEquals(
                restoredSpendPublicKey,
                accountKey.getSubAddressSpendKey().getPublicKey()
        );
    }
/* // TODO:
    @Test
    public void test_account_integrity() throws SerializationException, BadEntropyException,
            InvalidUriException, BadMnemonicException {
        byte[] rootEntropy = new byte[32];
        Arrays.fill(
                rootEntropy,
                (byte) 0
        );
        String mnemonic = Mnemonics.bip39EntropyToMnemonic(rootEntropy);
        AccountKey accountKey = AccountKey.fromMnemonicPhrase(
                mnemonic,
                0,
                fogConfig.getFogUri(),
                fogConfig.getFogReportId(),
                fogConfig.getFogAuthoritySpki()
        );

        RistrettoPrivate restoredViewKey = RistrettoPrivate.fromBytes(viewPrivateKeyBytes);
        RistrettoPrivate restoredSpendKey = RistrettoPrivate.fromBytes(spendPrivateKeyBytes);
        Assert.assertEquals(
                restoredViewKey,
                accountKey.getViewKey()
        );
        Assert.assertEquals(
                restoredSpendKey,
                accountKey.getSpendKey()
        );

        RistrettoPublic restoredViewPublicKey = RistrettoPublic.fromBytes(viewPublicKeyBytes);
        RistrettoPublic restoredSpendPublicKey = RistrettoPublic.fromBytes(spendPublicKeyBytes);

        Assert.assertEquals(
                restoredViewPublicKey,
                accountKey.getSubAddressViewKey().getPublicKey()
        );
        Assert.assertEquals(
                restoredSpendPublicKey,
                accountKey.getSubAddressSpendKey().getPublicKey()
        );
    }
 */
}
