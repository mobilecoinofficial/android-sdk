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
 * <br /><br />
 * An {@link AccountActivity} instance will remain valid at its created block index but will not be
 * updated with subsequent activity afterwards. Note that the unspent {@link OwnedTxOut}s already
 * present in this {@link AccountActivity} may be updated with subsequent calls to methods that
 * update the {@link MobileCoinClient} (e.g. {@link MobileCoinClient#getBalances()},
 * {@link MobileCoinClient#prepareTransaction(PublicAddress, Amount, Amount, TxOutMemoBuilder)})
 * if they become spent. This means that an {@link AccountActivity} may not remain strictly equal to
 * itself throughout its entire lifetime. It will, however, remain effectively equal by nature of
 * the fact that computations such as balance or transferable amount will always be accurate for the
 * block index at which it was created.
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
