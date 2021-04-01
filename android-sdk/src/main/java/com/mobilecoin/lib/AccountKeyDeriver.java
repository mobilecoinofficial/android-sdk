// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.BadBip39EntropyException;

class AccountKeyDeriver extends Native {
    private final static String TAG = AccountKeyDeriver.class.getName();

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
