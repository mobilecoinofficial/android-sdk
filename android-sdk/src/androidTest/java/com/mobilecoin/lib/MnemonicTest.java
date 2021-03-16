// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.BadEntropyException;
import com.mobilecoin.lib.exceptions.BadMnemonicException;
import com.mobilecoin.lib.util.Hex;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MnemonicTest {
    @Test(expected = BadEntropyException.class)
    public void wrong_entropy_length_zero() throws BadEntropyException {
        Mnemonics.entropyToMnemonic(new byte[0]);
    }

    @Test(expected = BadEntropyException.class)
    public void entropy_length_too_short() throws BadEntropyException {
        Mnemonics.entropyToMnemonic(Hex.toByteArray("c430fef6262a9e21d4587dc22f54c1"));
    }

    @Test(expected = BadEntropyException.class)
    public void entropy_length_too_long() throws BadEntropyException {
        Mnemonics.entropyToMnemonic(Hex.toByteArray(
                "6a36844131c70de644643935ac6610445d04adca6670f6bb6ee46935c873e21411"));
    }

    @Test
    public void create_mnemonic_12_words() throws BadEntropyException {
        String mnemonic = Mnemonics.entropyToMnemonic(Hex.toByteArray(
                "c430fef6262a9e21d4587dc22f54c1b7"));
        assertEquals("service margin rural era prepare axis fabric autumn season kingdom corn " +
                "hover", mnemonic);
    }

    @Test
    public void create_mnemonic_24_words() throws BadEntropyException {
        String mnemonic = Mnemonics.entropyToMnemonic(Hex.toByteArray(
                "6a36844131c70de644643935ac6610445d04adca6670f6bb6ee46935c873e214"));
        assertEquals("health reflect aware glory ignore veteran " +
                "bag mango cup glimpse lottery master " +
                "space finger civil sock wall swarm " +
                "ribbon sponsor frame delay marriage oyster", mnemonic);
    }

    @Test
    public void mnemonic_to_entropy_12_words() throws BadMnemonicException {
        byte[] entropy = Mnemonics.entropyFromMnemonic("service margin rural era prepare axis " +
                "fabric autumn season kingdom corn hover");
        assertEquals("c430fef6262a9e21d4587dc22f54c1b7", Hex.toString(entropy));
    }

    @Test
    public void mnemonic_to_entropy_24_words() throws BadMnemonicException {
        byte[] entropy = Mnemonics.entropyFromMnemonic("health reflect aware glory ignore veteran" +
                " " +
                "bag mango cup glimpse lottery master " +
                "space finger civil sock wall swarm " +
                "ribbon sponsor frame delay marriage oyster");
        assertEquals("6a36844131c70de644643935ac6610445d04adca6670f6bb6ee46935c873e214",
                Hex.toString(entropy));
    }

    @Test(expected = BadMnemonicException.class)
    public void mnemonic_to_entropy_bad_mnemonic_unknown_word() throws BadMnemonicException {
        byte[] entropy = Mnemonics.entropyFromMnemonic("service margin rural era prepare axis " +
                "fabric autumn season kingdom corn hoverx");
        assertArrayEquals(Hex.toByteArray("c430fef6262a9e21d4587dc22f54c1b7"), entropy);
    }

    @Test(expected = BadMnemonicException.class)
    public void mnemonic_to_entropy_bad_mnemonic_checksum_failure() throws BadMnemonicException {
        byte[] entropy = Mnemonics.entropyFromMnemonic("service margin rural era prepare axis " +
                "fabric autumn season kingdom corn corn");
        assertArrayEquals(Hex.toByteArray("c430fef6262a9e21d4587dc22f54c1b7"), entropy);
    }

    @Test
    public void derive_account_0() throws BadMnemonicException {
        byte[] bytes = AccountKey.deriveAccountRootEntropy("service margin rural era prepare axis" +
                " fabric autumn season kingdom corn hover", 0);
        assertEquals("2888ece4e970821669b9996c731a8ce557f4eae4d275d94be03569f4f72f60a6",
                Hex.toString(bytes));
    }

    @Test
    public void derive_account_1() throws BadMnemonicException {
        byte[] bytes = AccountKey.deriveAccountRootEntropy("service margin rural era prepare axis" +
                " fabric autumn season kingdom corn hover", 1);
        assertEquals("b90464080c70fe41b8e509b008e493ed2edf4046d4b9d48165deb8d8f424f62d",
                Hex.toString(bytes));
    }

    @Test
    public void words_by_prefix() throws BadMnemonicException {
        String[] words = Mnemonics.wordsByPrefix("z");
        assertArrayEquals(new String[]{"zebra", "zero", "zone", "zoo"}, words);
    }

    @Test
    public void all_words() throws BadMnemonicException {
        String[] words = Mnemonics.wordsByPrefix("");
        assertEquals(2048, words.length);
    }
}
