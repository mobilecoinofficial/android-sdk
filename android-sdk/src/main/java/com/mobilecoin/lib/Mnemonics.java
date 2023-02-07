// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.VisibleForTesting;

import com.mobilecoin.lib.exceptions.BadEntropyException;
import com.mobilecoin.lib.exceptions.BadMnemonicException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.TransportProtocol;

import java.util.List;

public final class Mnemonics extends Native {
    private final static String TAG = Mnemonics.class.getName();

    private Mnemonics() {
    }

    /**
     * Uses a cryptographically secure RNG implementation to generate a new random mnemonic.
     *
     * This method should only be used the first time an account is created. After generating the mnemonic,
     * it must be stored securely by the end-user and should never be shared with anybody. If the
     * mnemonic is lost, there is no way to generate the same one again.
     *
     * Mnemonics returned by this method can be passed to {@link AccountKey#fromMnemonicPhrase(String, int, Uri, String, byte[])}
     * to create an {@link AccountKey}.
     *
     * @return a unique, securely generated, random mnemonic phrase
     *
     * @see AccountKey#fromMnemonicPhrase(String, int, Uri, String, byte[])
     * @see MobileCoinClient#MobileCoinClient(AccountKey, Uri, Uri, TransportProtocol)
     * @see MobileCoinClient#MobileCoinClient(AccountKey, Uri, Uri, ClientConfig, TransportProtocol)
     * @see MobileCoinClient#MobileCoinClient(AccountKey, Uri, List, ClientConfig, TransportProtocol)
     * @since 4.0.0.1
     */
    public static String createRandomMnemonic() {
        return entropy_to_mnemonic(DefaultRng.createInstance().nextBytes(32));
    }

    /**
     * Gives mnemonic for the supplied entropy.
     */
    public static String bip39EntropyToMnemonic(byte[] entropy) throws BadEntropyException {
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
     *
     * @see AccountKey#fromMnemonicPhrase(String, int, Uri, String, byte[])
     * @see Mnemonics#createRandomMnemonic()
     */
    public static byte[] bip39EntropyFromMnemonic(String mnemonic) throws BadMnemonicException {
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

    // native methods
    private static native String entropy_to_mnemonic(byte[] entropy);

    private static native byte[] entropy_from_mnemonic(String mnemonic);

    private static native String words_by_prefix(String prefix);
}
