// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.BadEntropyException;
import com.mobilecoin.lib.exceptions.BadMnemonicException;
import com.mobilecoin.lib.log.Logger;

public final class Mnemonics extends Native {
    private final static String TAG = Mnemonics.class.getName();

    private Mnemonics() {
    }

    /**
     * Gives mnemonic for the supplied entropy.
     */
    public static String entropyToMnemonic(byte[] entropy) throws BadEntropyException {
        Logger.i(TAG, "Converting entropy to mnemonic");
        try {
            return entropy_to_mnemonic(entropy);
        } catch (Exception exception) {
            BadEntropyException badEntropyException =
                    new BadEntropyException("Failed to get a mnemonic", exception);
            Util.logException(TAG, badEntropyException);
            throw badEntropyException;
        }
    }

    /**
     * Gives entropy from the supplied mnemonic.
     */
    public static byte[] entropyFromMnemonic(String mnemonic) throws BadMnemonicException {
        Logger.i(TAG, "Getting entropy from mnemonic");
        try {
            return entropy_from_mnemonic(mnemonic);
        } catch (Exception exception) {
            BadMnemonicException badMnemonicException =
                    new BadMnemonicException("Failed to get an entropy from mnemonic", exception);
            Util.logException(TAG, badMnemonicException);
            throw badMnemonicException;
        }
    }

    /**
     * List words from the BIP39 dictionary with the given prefix.
     * Empty prefix gives all 2048 words.
     */
    public static String[] wordsByPrefix(String prefix) throws BadMnemonicException {
        Logger.i(TAG, "Listing words by prefix");
        try {
            String delimitedString = words_by_prefix(prefix);
            return delimitedString.split(",");
        } catch (Exception exception) {
            BadMnemonicException badMnemonicException =
                    new BadMnemonicException("Unable to create words dictionary", exception);
            Util.logException(TAG, badMnemonicException);
            throw badMnemonicException;
        }
    }

    /**
     * Gives bip39Seed from the mnemonic.
     */
    public static byte[] getBip39Seed(String mnemonic) throws BadMnemonicException {
        Logger.i(TAG, "Getting Bip39 seed");
        try {
            return get_bip39_seed(mnemonic);
        } catch (Exception exception) {
            BadMnemonicException badMnemonicException =
                    new BadMnemonicException("Failed to get an entropy from mnemonic", exception);
            Util.logException(TAG, badMnemonicException);
            throw badMnemonicException;
        }
    }

    // native methods
    private static native String entropy_to_mnemonic(byte[] entropy) throws Exception;

    private static native byte[] entropy_from_mnemonic(String mnemonic) throws Exception;

    private static native String words_by_prefix(String prefix);

    private static native byte[] get_bip39_seed(String mnemonic) throws Exception;
}
