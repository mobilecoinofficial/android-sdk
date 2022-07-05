// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;
import com.mobilecoin.lib.log.Logger;

import java.util.ArrayList;
import java.util.HashSet;
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
    private final AccountKey accountKey;

    AccountActivity(@NonNull Set<OwnedTxOut> txOuts, @NonNull UnsignedLong blockCount, @NonNull AccountKey accountKey) {
        this.txOuts = txOuts;
        this.blockCount = blockCount;
        this.accountKey = accountKey;
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
     * Contacts are to be provided as a {@link Set} of {@link PublicAddressProvider}s.
     * This method will iterate over the {@link AccountActivity}'s {@link Set} of {@link OwnedTxOut}s
     * and parse the {@link TxOutMemo}s. For every {@link OwnedTxOut} that has a memo matching the
     * {@link AddressHash} of a contact, a {@link HistoricalTransaction} will be created and added
     * to a {@link List}. The {@link List} is then returned.
     *
     * @param contacts a {@link Set} of {@link PublicAddressProvider}s
     * @return a {@link List} of {@link HistoricalTransaction}s
     * @see PublicAddressProvider
     * @see HistoricalTransaction
     * @since 1.2.2
     */
    @NonNull
    public List<HistoricalTransaction> recoverTransactions(@NonNull Set<PublicAddressProvider> contacts) {
        Set<OwnedTxOut> ownedTxOut = getAllTokenTxOuts();
        List<HistoricalTransaction> historicalTransactions = new ArrayList<>();
        for (OwnedTxOut txOut: ownedTxOut) {
            for (PublicAddressProvider contact: contacts) {
                TxOutMemo memo = txOut.getTxOutMemo();
                if (contact.getPublicAddress().calculateAddressHash() == memo.memoData.getAddressHash()) {
                    switch (memo.memoType) {
                        case SENDER:
                        case SENDER_WITH_PAYMENT_REQUEST:
                            if (validateSenderMemo(contact.getPublicAddress(), (SenderMemo) memo)) {
                                historicalTransactions.add(new HistoricalTransaction(txOut));
                            }
                            break;
                        case DESTINATION:
                            historicalTransactions.add(new HistoricalTransaction(txOut));
                            break;
                    }
                }
            }
        }
        return historicalTransactions;
    }

    private boolean validateSenderMemo(PublicAddress contact, SenderMemo memo) {
        try {
            memo.getSenderMemoData(
                    contact.getPublicAddress(),
                    accountKey.getDefaultSubAddressViewKey()
            );
            return true;
        } catch (InvalidTxOutMemoException e) {
            // Unable to validate contact public address with memo
        }
        return false;
    }

    /**
     * Recovers all transactions for the provided contact.
     * The contact is to be provided as an {@link PublicAddressProvider}.
     * This method will iterate over the {@link AccountActivity}'s {@link Set} of {@link OwnedTxOut}s
     * and parse the {@link TxOutMemo}s. For every {@link OwnedTxOut} that has a memo matching the
     * {@link AddressHash} of the contact, a {@link HistoricalTransaction} will be created and added
     * to a {@link List}. The {@link List} is then returned.
     *
     * @param contact an {@link PublicAddressProvider}
     * @return a {@link List} of {@link HistoricalTransaction}s
     * @see PublicAddressProvider
     * @see HistoricalTransaction
     * @since 1.2.2
     */
    @NonNull
    public List<HistoricalTransaction> recoverContactTransactions(@NonNull PublicAddressProvider contact) {
        Set<PublicAddressProvider> contacts = new HashSet<>();
        contacts.add(contact);
        return recoverTransactions(contacts);
    }

}
