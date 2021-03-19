// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.AmountDecoderException;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FeeRejectedException;
import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.FragmentedAccountException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidReceiptException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;
import com.mobilecoin.lib.log.Logger;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fog_ledger.Ledger;

/**
 * This class represents the Account's state at the specified block index
 */
public class AccountSnapshot {
    private final static String TAG = AccountSnapshot.class.getName();
    private final UnsignedLong blockIndex;
    private final Set<OwnedTxOut> txOuts;
    private final MobileCoinClient mobileCoinClient;

    AccountSnapshot(@NonNull MobileCoinClient mobileCoinClient, @NonNull Set<OwnedTxOut> txOuts,
                    @NonNull UnsignedLong blockIndex) {
        this.txOuts = txOuts;
        this.blockIndex = blockIndex;
        this.mobileCoinClient = mobileCoinClient;
    }
    /**
     * Snapshot's block index
     */
    @NonNull
    public UnsignedLong getBlockIndex() {
        return blockIndex;
    }

    @NonNull
    public AccountActivity getAccountActivity() {
        return new AccountActivity(txOuts, blockIndex.add(UnsignedLong.ONE));
    }

    /**
     * Retrieve the account's balance as it was at the snapshot's block index
     */
    @NonNull
    public Balance getBalance() {
        Logger.i(TAG, "Getting balance");
        BigInteger coins = BigInteger.ZERO;
        for (OwnedTxOut txOut : txOuts) {
            if (!txOut.isSpent(blockIndex)) {
                coins = coins.add(txOut.getValue());
            }
        }
        return new Balance(
                coins,
                blockIndex
        );
    }

    /**
     * Check the status of the transaction receipt. Recipient's key is required to decode
     * verification data, hence only the recipient of the transaction can verify receipts. Sender
     * should use {@link MobileCoinClient#getTransactionStatus}
     *
     * @param receipt provided by the transaction sender to the recipient
     * @return {@link Receipt.Status}
     */
    @NonNull
    public Receipt.Status getReceiptStatus(@NonNull Receipt receipt) throws InvalidReceiptException {
        Logger.i(TAG, "Checking receipt status");
        RistrettoPublic txOutPubKey = receipt.getPublicKey();
        for (OwnedTxOut txOut : txOuts) {
            if (txOut.getPublicKey().equals(txOutPubKey)) {
                try {
                    AccountKey accountKey = mobileCoinClient.getAccountKey();
                    BigInteger receiptAmountValue = receipt.getAmount(accountKey);
                    BigInteger txoValue = txOut.getValue();
                    if (!txoValue.equals(receiptAmountValue)) {
                        InvalidReceiptException exception = new InvalidReceiptException("Receipt " +
                                "amount mismatch");
                        Util.logException(TAG, exception);
                        throw exception;
                    }
                } catch (AmountDecoderException exception) {
                    InvalidReceiptException receiptException = new InvalidReceiptException(
                            "Malformed Receipt", exception);
                    Util.logException(TAG, receiptException);
                    throw receiptException;
                }
                return Receipt.Status.RECEIVED.atBlock(txOut.getReceivedBlockIndex());
            }
        }
        Receipt.Status status = Receipt.Status.UNKNOWN.atBlock(blockIndex);
        if (blockIndex.compareTo(receipt.getTombstoneBlockIndex()) >= 0) {
            status = Receipt.Status.FAILED.atBlock(blockIndex);
        }
        return status;
    }

    /**
     * Check the status of the transaction. Sender's key is required to decode verification data,
     * hence only the sender of the transaction can verify it's status. Recipients should use {@link
     * AccountSnapshot#getReceiptStatus} )}
     *
     * @param transaction obtained from {@link MobileCoinClient#prepareTransaction}
     * @return {@link Transaction.Status}
     */
    @NonNull
    public Transaction.Status getTransactionStatus(@NonNull Transaction transaction)
            throws NetworkException {
        Logger.i(TAG, "Checking transaction status");
        HashMap<Integer, Boolean> keyMapping = new HashMap<>();
        for (KeyImage keyImage : transaction.getKeyImages()) {
            keyMapping.put(
                    keyImage.hashCode(),
                    true
            );
        }
        for (OwnedTxOut txOut : txOuts) {
            if (txOut.isSpent(blockIndex)) {
                KeyImage keyImage = txOut.getKeyImage();
                Integer hash = keyImage.hashCode();
                keyMapping.remove(hash);
            }
        }
        if (keyMapping.isEmpty()) {
            Set<RistrettoPublic> outputPublicKeys = transaction.getOutputPublicKeys();
            Ledger.TxOutResponse response =
                    mobileCoinClient.untrustedClient.fetchTxOuts(outputPublicKeys);
            List<Ledger.TxOutResult> results = response.getResultsList();

            boolean allTxOutsFound = true;
            UnsignedLong outputBlockIndex = UnsignedLong.ZERO;

            for (Ledger.TxOutResult txOutResult : results) {
                if (txOutResult.getResultCode() != Ledger.TxOutResultCode.Found) {
                    allTxOutsFound = false;
                    break;
                } else {
                    UnsignedLong txOutBlockIndex =
                            UnsignedLong.fromLongBits(txOutResult.getBlockIndex());
                    if (outputBlockIndex.compareTo(txOutBlockIndex) < 0) {
                        outputBlockIndex = txOutBlockIndex;
                    }
                }
            }
            if (allTxOutsFound && outputBlockIndex.compareTo(blockIndex) <= 0) {
                return Transaction.Status.ACCEPTED.atBlock(outputBlockIndex);
            }
        }
        if (blockIndex.compareTo(
                UnsignedLong.fromLongBits(transaction.getTombstoneBlockIndex())) >= 0) {
            return Transaction.Status.FAILED.atBlock(blockIndex);
        }
        return Transaction.Status.UNKNOWN.atBlock(blockIndex);
    }

    /**
     * Calculate the total transferable amount excluding all the required fees for such transfer
     */
    @NonNull
    public BigInteger getTransferableAmount() {
        Logger.i(TAG, "Calculating transferable amount");
        HashSet<OwnedTxOut> unspent = txOuts.stream().filter(p -> !p.isSpent(blockIndex))
                .collect(Collectors.toCollection(HashSet::new));
        try {
            return UTXOSelector.getTransferableAmount(unspent,
                    MobileCoinClient.TX_FEE,
                    MobileCoinClient.INPUT_FEE,
                    MobileCoinClient.OUTPUT_FEE);
        } catch (InsufficientFundsException ignored) {
            return BigInteger.ZERO;
        }
    }

    /**
     * @param recipient {@link PublicAddress} of the recipient
     * @param amount    transaction amount
     * @param fee       transaction fee (see {@link MobileCoinClient#estimateTotalFee})
     * @return {@link PendingTransaction} which encapsulates the {@link Transaction} and {@link
     * Receipt} objects
     */
    @NonNull
    public PendingTransaction prepareTransaction(
            @NonNull final PublicAddress recipient,
            @NonNull final BigInteger amount,
            @NonNull final BigInteger fee
    ) throws InsufficientFundsException, FragmentedAccountException, FeeRejectedException,
            InvalidFogResponse, AttestationException, NetworkException,
            TransactionBuilderException, FogReportException {
        Logger.i(TAG, "PrepareTransaction call", null,
                "recipient:", recipient,
                "amount:", amount,
                "fee:", fee);
        Set<OwnedTxOut> unspent = txOuts.stream().filter(p -> !p.isSpent(getBlockIndex()))
                .collect(Collectors.toCollection(HashSet::new));
        BigInteger finalAmount = amount.add(fee);
        BigInteger totalAvailable = unspent.stream()
                .map(OwnedTxOut::getValue)
                .reduce(BigInteger.ZERO, BigInteger::add);
        if (totalAvailable.compareTo(finalAmount) < 0) {
            throw new InsufficientFundsException();
        }
        // the custom fee is provided, no need to calculate a new fee
        UTXOSelector.Selection<OwnedTxOut> selection = UTXOSelector.selectInputsForAmount(unspent,
                finalAmount,
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.ZERO,
                0
        );
        return mobileCoinClient.prepareTransaction(
                recipient,
                amount,
                selection.txOuts,
                fee
        );
    }
}
