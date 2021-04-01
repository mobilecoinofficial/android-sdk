// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.util.Log;

import com.google.common.base.Preconditions;
import com.mobilecoin.lib.exceptions.BadEntropyException;
import com.mobilecoin.lib.exceptions.BadMnemonicException;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.SerializationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccountTest {

    private static final List<AccountTestData> accountTestDataList = new ArrayList<>();

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
    private static final String TEST_ACCOUNT_DATA_PATH = "raw/test_account_data.jsonl";

    private final TestFogConfig fogConfig = Environment.getTestFogConfig();

    @BeforeClass
    public static void setUpTestData() {
        ClassLoader classLoader = AccountTest.class.getClassLoader();
        InputStream testAccountData = classLoader.getResourceAsStream(TEST_ACCOUNT_DATA_PATH);
        Preconditions.checkNotNull(testAccountData, "The testAccountData object is null. " +
                "Ensure path is correct");
        String line;
        // If this logic fails, then the test runner will report that "No tests were found."
        // Wrapping with a try block allows us to print the error and diagnose the root cause.
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(testAccountData))) {
            while ((line = reader.readLine()) != null) {
                JSONObject mainObject = new JSONObject(line);
                accountTestDataList.add(AccountTestData.fromJsonObject(mainObject));
            }
        } catch (IOException | JSONException e) {
            Log.e("ACCOUNT_TEST", e.getMessage());
        }
    }


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

    @Test
    public void fromMnemonicPhrase_createsCorrectPrivateKeys() throws Exception {
        Assert.assertFalse(accountTestDataList.isEmpty());
        for (AccountTestData accountTestData : accountTestDataList) {
            String calculatedMnemonicPhrase =
                    Mnemonics.bip39EntropyToMnemonic(accountTestData.bip39Entropy);
            Assert.assertEquals(calculatedMnemonicPhrase, accountTestData.mnemonic);

            TestFogConfig testFogConfig = Environment.getTestFogConfig();
            AccountKey accountKey = AccountKey.fromMnemonicPhrase(calculatedMnemonicPhrase,
                    accountTestData.accountIndex, testFogConfig.getFogUri(),
                    testFogConfig.getFogReportId(), testFogConfig.getFogAuthoritySpki());

            byte[] calculatedViewPrivateKeyBytes = accountKey.getViewKey().getKeyBytes();
            Assert.assertArrayEquals(accountTestData.viewPrivateKey, calculatedViewPrivateKeyBytes);

            byte[] calculatedSpendPrivateKeyBytes = accountKey.getSpendKey().getKeyBytes();
            Assert.assertArrayEquals(accountTestData.spendPrivateKey,
                    calculatedSpendPrivateKeyBytes);
        }
    }

    @Test
    public void fromBip39Entropy_createsCorrectPrivateKeys() throws Exception {
        Assert.assertFalse(accountTestDataList.isEmpty());
        for (AccountTestData accountTestData : accountTestDataList) {
            byte[] calculatedBip39Entropy =
                    Mnemonics.bip39EntropyFromMnemonic(accountTestData.mnemonic);
            Assert.assertArrayEquals(calculatedBip39Entropy, accountTestData.bip39Entropy);

            TestFogConfig testFogConfig = Environment.getTestFogConfig();
            AccountKey accountKey = AccountKey.fromBip39Entropy(calculatedBip39Entropy,
                    accountTestData.accountIndex, testFogConfig.getFogUri(),
                    testFogConfig.getFogReportId(), testFogConfig.getFogAuthoritySpki());

            byte[] calculatedViewPrivateKeyBytes = accountKey.getViewKey().getKeyBytes();
            Assert.assertArrayEquals(accountTestData.viewPrivateKey, calculatedViewPrivateKeyBytes);

            byte[] calculatedSpendPrivateKeyBytes = accountKey.getSpendKey().getKeyBytes();
            Assert.assertArrayEquals(accountTestData.spendPrivateKey,
                    calculatedSpendPrivateKeyBytes);
        }
    }

    /**
     * Contains data needed for Account tests.
     */
    static class AccountTestData {
        byte[] bip39Entropy;
        String mnemonic;
        int accountIndex;
        byte[] viewPrivateKey;
        byte[] spendPrivateKey;

        static AccountTestData fromJsonObject(JSONObject mainObject) throws JSONException {
            byte[] bip39Entropy
                    = createByteArray(mainObject.getJSONArray("entropy"));
            String mnemonic = mainObject.getString("mnemonic");
            int accountIndex = mainObject.getInt("account_index");
            byte[] viewPrivateKey = createByteArray(mainObject.getJSONArray("view_private_key"
            ));
            byte[] spendPrivateKey = createByteArray(mainObject.getJSONArray(
                    "spend_private_key"));

            return new AccountTestData(bip39Entropy, mnemonic, accountIndex,
                    viewPrivateKey, spendPrivateKey);
        }

        private static byte[] createByteArray(JSONArray jsonArray) throws JSONException {
            byte[] byteArray = new byte[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                byteArray[i] = (byte) jsonArray.getInt(i);
            }

            return byteArray;
        }

        @Override
        public String toString() {
            return String.format("AccountTestData [bip39Entropy=%s, mnemonic=%s, accountIndex=%s," +
                            " viewPrivateKey=%s, spendPrivateKey=%s]",
                    Arrays.toString(bip39Entropy),
                    mnemonic, accountIndex, Arrays.toString(viewPrivateKey),
                    Arrays.toString(spendPrivateKey));
        }

        private AccountTestData(byte[] bip39Entropy, String mnemonic, int accountIndex,
                                byte[] viewPrivateKey, byte[] spendPrivateKey) {
            this.bip39Entropy = bip39Entropy;
            this.mnemonic = mnemonic;
            this.accountIndex = accountIndex;
            this.viewPrivateKey = viewPrivateKey;
            this.spendPrivateKey = spendPrivateKey;
        }
    }
}