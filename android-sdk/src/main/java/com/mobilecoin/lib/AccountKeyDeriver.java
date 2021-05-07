// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.BadBip39EntropyException;

/**
 * The {@link AccountKeyDeriver} is used to derive {@link AccountKey}s
 */
final class AccountKeyDeriver extends Native {
    private final static String TAG = AccountKeyDeriver.class.getName();

    /**
     * Derive {@link AccountKey} from a mnemonic phrase and an account index.
     * Each account index corresponds to a unique deterministically derived {@link AccountKey}
     *
     * @param mnemonicPhrase is a bip39-compatible mnemonic phrase
     * @param accountIndex   index of the account to derive
     */
    static AccountKey deriveAccountKeyFromMnemonic(
            @NonNull String mnemonicPhrase,
            int accountIndex
    ) throws BadBip39EntropyException {
        try {
            return AccountKey.fomJNI(accountKey_from_mnemonic(mnemonicPhrase, accountIndex));
        } catch (Exception exception) {
            BadBip39EntropyException badBip39EntropyException =
                    new BadBip39EntropyException("Unable to derive ed25519 private key", exception);
            Util.logException(TAG, badBip39EntropyException);
            throw badBip39EntropyException;
        }
    }

    private native static long accountKey_from_mnemonic(String mnemonic_phrase, int account_index);
}
