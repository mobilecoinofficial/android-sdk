// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.BadBip39SeedException;
import com.mobilecoin.lib.log.Logger;

class Slip10 extends Native {
    private final static String TAG = Slip10.class.getName();

    /**
     * Derives only the private key for ED25519 in the manner defined in [SLIP-0010]
     * (https://github.com/satoshilabs/slips/blob/master/slip-0010.md).
     *
     * @param bip39seed output of {@link Mnemonics#getBip39Seed(String)}
     * @param path      an array of indexes that define the path. E.g. for m/1'/2'/3', pass 1, 2,
     *                  3. As with Ed25519 non-hardened child indexes
     *                  are not supported, this function treats all indexes as hardened.
     * @return Private key at path.
     */
    static byte[] deriveEd25519PrivateKey(byte[] bip39seed, int... path) throws BadBip39SeedException {
        Logger.i(TAG, "Deriving private key from bip39 seed");
        try {
            return derive_ed25519_private_key(bip39seed, path);
        } catch (Exception exception) {
            BadBip39SeedException badBip39SeedException =
                    new BadBip39SeedException("Unable to derive ed25519 private key", exception);
            Util.logException(TAG, badBip39SeedException);
            throw badBip39SeedException;
        }
    }

    private native static byte[] derive_ed25519_private_key(byte[] bip39seed, int... path);
}
