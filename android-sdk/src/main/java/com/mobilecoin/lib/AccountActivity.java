// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.log.Logger;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * The {@link AccountActivity} class provides a low-level info about the account activity up to the
 * block index at which it was created.
 * <br /><br />
 * The activity consists of a {@link Set} of {@link OwnedTxOut}s that the account has received and spent.
 * Each {@link OwnedTxOut} contains the information about the block index and the timestamp at which it was
 * received and/or spent (if it was spent).
 * </p>
 * @see OwnedTxOut
 * @see MobileCoinClient#getAccountActivity()
 * @since 1.0.0
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
     * @return all account MOB TxOuts at the particular block count, see
     * {@link AccountActivity#getBlockCount}
     *
     * @deprecated Deprecated as of 1.2.0. Please use {@link AccountActivity#getAllTokenTxOuts()}.
     * @see AccountActivity#getAllTokenTxOuts()
     * @since 1.0.0
     */
    @Deprecated
    @NonNull
    public Set<OwnedTxOut> getAllTxOuts() {
        Logger.i(TAG, "GetAllTxOuts", null,
                "txOuts count:", txOuts.size());
        return txOuts.stream()
                .filter(otxo -> TokenId.MOB.equals(otxo.getAmount().getTokenId()))
                .collect(Collectors.toSet());
    }

    /**
     * @return all account TxOuts at the particular block count, see
     * {@link AccountActivity#getBlockCount}
     */
    @NonNull
    public Set<OwnedTxOut> getAllTokenTxOuts() {
        Logger.i(TAG, "GetAllTxOuts", null,
                "txOuts count:", txOuts.size());
        return txOuts;
    }

}
