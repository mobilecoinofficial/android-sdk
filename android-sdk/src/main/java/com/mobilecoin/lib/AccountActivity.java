// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.log.Logger;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
     * @return all account MOB TxOuts at the particular block count, see
     * {@link AccountActivity#getBlockCount}
     * @deprecated Deprecated as of 1.2.0. Please use {@link AccountActivity#getAllTokenTxOuts}.
     * @see TokenId
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
     * @since 1.2.0
     */
    @NonNull
    public Set<OwnedTxOut> getAllTokenTxOuts() {
        Logger.i(TAG, "GetAllTxOuts", null,
                "txOuts count:", txOuts.size());
        return txOuts;
    }

    /**
     * Recovers all transactions for the provided set of contacts.
     * Contacts are to be provided as a {@link Set} of {@link AddressHashProvider}s.
     * This method will iterate over the {@link AccountActivity}'s {@link Set} of {@link OwnedTxOut}s
     * and parse the {@link TxOutMemo}s. For every {@link OwnedTxOut} that has a memo matching the
     * {@link AddressHash} of a contact, a {@link HistoricalTransaction} will be created and added
     * to a {@link List}. The {@link List} is then returned.
     *
     * @param contacts a {@link Set} of {@link AddressHashProvider}s
     * @return a {@link List} of {@link HistoricalTransaction}s
     * @see AddressHashProvider
     * @see HistoricalTransaction
     * @since 1.2.2
     */
    @NonNull
    public List<HistoricalTransaction> recoverTransactions(@NonNull Set<AddressHashProvider> contacts) {
        return null;//TODO: this
    }

    /**
     * Recovers all transactions for the provided contact.
     * The contact is to be provided as an {@link AddressHashProvider}.
     * This method will iterate over the {@link AccountActivity}'s {@link Set} of {@link OwnedTxOut}s
     * and parse the {@link TxOutMemo}s. For every {@link OwnedTxOut} that has a memo matching the
     * {@link AddressHash} of the contact, a {@link HistoricalTransaction} will be created and added
     * to a {@link List}. The {@link List} is then returned.
     *
     * @param contact an {@link AddressHashProvider}
     * @return a {@link List} of {@link HistoricalTransaction}s
     * @see AddressHashProvider
     * @see HistoricalTransaction
     * @since 1.2.2
     */
    @NonNull
    public List<HistoricalTransaction> recoverContactTransactions(@NonNull AddressHashProvider contact) {
        return null;//TODO: this
    }

}
