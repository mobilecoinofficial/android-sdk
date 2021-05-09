// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.log.Logger;

import java.util.Set;

/**
 * <pre>
 * The {@code AccountActivity} class provides a low-level info about the account activity.
 *
 * The activity is comprised from the individual TxOuts that the account has received and spent.
 * Each TxOut contains the information about the block indexes and the timestamps when it was
 * received and spent (if it was spent).
 * </pre>
 */
public final class AccountActivity {
    private final static String TAG = AccountActivity.class.getName();
    private final UnsignedLong blockCount;
    private final Set<OwnedTxOut> txOuts;

    AccountActivity(@NonNull Set<OwnedTxOut> txOuts, @NonNull UnsignedLong blockCount) {
        this.txOuts = txOuts;
        this.blockCount = blockCount;
        Logger.i(TAG, "Created AccountActivity", null,
                "txOuts size:", txOuts.size(),
                "blockCount:", blockCount);
    }

    /**
     * @return block count when the AccountActivity was current
     */
    @NonNull
    public UnsignedLong getBlockCount() {
        Logger.i(TAG, "Get block count", null,
                "blocks:", blockCount.toString());
        return blockCount;
    }

    /**
     * @return all account TxOuts at the particular block count, see
     * {@link AccountActivity#getBlockCount}
     */
    @NonNull
    public Set<OwnedTxOut> getAllTxOuts() {
        Logger.i(TAG, "GetAllTxOuts", null,
                "txOuts count:", txOuts.size());
        return txOuts;
    }
}
