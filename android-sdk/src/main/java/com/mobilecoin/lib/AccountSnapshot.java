// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static com.mobilecoin.lib.MobileCoinClient.INPUT_FEE;
import static com.mobilecoin.lib.MobileCoinClient.OUTPUT_FEE;

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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fog_ledger.Ledger;

/**
 * This class represents the Account's state at the specified block index
 */
public final class AccountSnapshot {
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

    /**
     * @return account activity in the current snapshot
     */
    @NonNull
    public AccountActivity getAccountActivity() {
        return new AccountActivity(txOuts, blockIndex.add(UnsignedLong.ONE));
    }

    /**
     * Computes the account's balance as it was at the snapshot's block index
     */
    @NonNull
    @Deprecated
    public Balance getBalance() {
        return getBalance(TokenId.MOB);
    }

    /**
     * Computes the account's balance as it was at the snapshot's block index
     */
    @NonNull
    public Balance getBalance(TokenId tokenId) {
        Logger.i(TAG, "Getting balance");
        BigInteger value = BigInteger.ZERO;
        for (OwnedTxOut txOut : txOuts) {
            if (!txOut.isSpent(blockIndex)) {
                if (txOut.getAmount().getTokenId().equals(tokenId)) {
                    value = value.add(txOut.getAmount().getValue());
                }
            }
        }
        return new Balance(
                value,
                blockIndex
        );
    }

    /**
     * Computes the account's balances
     */
    public Map<TokenId, Balance> getBalances() {
        HashMap<TokenId, Balance> balances = new HashMap<TokenId, Balance>();
        for(OwnedTxOut otxo : txOuts) {
            //TODO: on API level 24, we can use getOrDefault to simplify the logic here
            Balance balance = balances.get(otxo.getAmount().getTokenId());
            if(null == balance) {
                balance = new Balance(BigInteger.ZERO, blockIndex);
            }
            if(!otxo.isSpent(blockIndex)) {
                balance = new Balance(
                        otxo.getAmount().getValue().add(balance.getValue()),
                        blockIndex
                );
            }
            balances.put(otxo.getAmount().getTokenId(), balance);
        }
        return balances;
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
                    Amount receiptAmountValue = receipt.getAmountData(accountKey);
                    Amount txoValue = txOut.getAmount();
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
                    mobileCoinClient.getUntrustedClient().fetchTxOuts(outputPublicKeys);
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
     *
     * @param minimumTxFee minimum transaction fee, see
     * {@link MobileCoinClient#getOrFetchMinimumTxFee}
     */
    @Deprecated
    @NonNull
    public BigInteger getTransferableAmount(@NonNull BigInteger minimumTxFee) {
        return getTransferableAmount(new Amount(minimumTxFee, TokenId.MOB)).getValue();
    }

    /**
     * Calculate the total transferable amount excluding all the required fees for such transfer
     *
     * @param minimumTxFee minimum transaction fee, see
     * {@link MobileCoinClient#getOrFetchMinimumTxFee}
     */
    @NonNull
    public Amount getTransferableAmount(@NonNull Amount minimumTxFee) {
        Logger.i(TAG, "Calculating transferable amount");
        HashSet<OwnedTxOut> unspent = txOuts.stream().filter(p -> !p.isSpent(blockIndex))
                .filter(utxo -> utxo.getAmount().getTokenId().equals(minimumTxFee.getTokenId()))
                .collect(Collectors.toCollection(HashSet::new));
        try {
            BigInteger value =  UTXOSelector.getTransferableAmount(
                    unspent,
                    minimumTxFee.getValue(),
                    INPUT_FEE,OUTPUT_FEE
            );
            return new Amount(value, minimumTxFee.getTokenId());
        } catch (InsufficientFundsException ignored) {
            return new Amount(BigInteger.ZERO, minimumTxFee.getTokenId());
        }
    }

    /**
     * The minimum fee required to send a transaction with the specified value in picoMOB. The
     * account balance consists of multiple coins, if there are no big enough coins to successfully
     * send the transaction {@link FragmentedAccountException} will be thrown. The account needs to
     * be defragmented in order to send the specified amount. See {@link MobileCoinAccountClient#defragmentAccount}
     *
     * @param amountPicoMOB       an amount value in picoMob
     * @param minimumTxFee minimum transaction fee, see
     *                     {@link MobileCoinClient#getOrFetchMinimumTxFee}
     */
    @Deprecated
    @NonNull
    public BigInteger estimateTotalFee(@NonNull BigInteger amountPicoMOB,
                                       @NonNull BigInteger minimumTxFee
    ) throws InsufficientFundsException {
        return estimateTotalFee(
                new Amount(amountPicoMOB, TokenId.MOB),
                new Amount(minimumTxFee, TokenId.MOB)
        ).getValue();
    }

    /**
     * The minimum fee required to send a transaction with the specified Amount. The account balance
     * consists of multiple coins, if there are no big enough coins to successfully send the
     * transaction {@link FragmentedAccountException} will be thrown. The account needs to be
     * defragmented in order to send the specified amount. See
     * {@link MobileCoinAccountClient#defragmentAccount}
     *
     * @param amount       the Amount to send
     * @param minimumTxFee minimum transaction fee, see
     *                     {@link MobileCoinClient#getOrFetchMinimumTxFee}
     */
    @NonNull
    public Amount estimateTotalFee(@NonNull Amount amount,
                                   @NonNull Amount minimumTxFee
    ) throws InsufficientFundsException {
        Logger.i(TAG, "EstimateTotalFee call");
        if(!amount.getTokenId().equals(minimumTxFee.getTokenId())) {
            throw(new IllegalArgumentException("Mixed token type transactions not supported"));
        }
        HashSet<OwnedTxOut> unspent = txOuts.stream().filter(p -> !p.isSpent(blockIndex))
                .filter(otxo -> otxo.getAmount().getTokenId().equals(amount.getTokenId()))
                .collect(Collectors.toCollection(HashSet::new));
        BigInteger totalFee = UTXOSelector.calculateFee(
                unspent,
                amount.getValue(),
                minimumTxFee.getValue(),
                INPUT_FEE,
                OUTPUT_FEE,
                2);
        Logger.d(TAG, "Estimated total fee", null, "totalFee:", totalFee);
        return new Amount(totalFee, amount.getTokenId());
    }

    /**
     * @param recipient     {@link PublicAddress} of the recipient
     * @param amountPicoMOB transaction amount in picoMOB
     * @param feePicoMOB    transaction fee (see {@link MobileCoinClient#estimateTotalFee})
     * @return {@link PendingTransaction} which encapsulates the {@link Transaction} and {@link
     * Receipt} objects
     */
    @Deprecated
    @NonNull
    public PendingTransaction prepareTransaction(
            @NonNull final PublicAddress recipient,
            @NonNull final BigInteger amountPicoMOB,
            @NonNull final BigInteger feePicoMOB
    ) throws InsufficientFundsException, FragmentedAccountException, FeeRejectedException,
            InvalidFogResponse, AttestationException, NetworkException,
            TransactionBuilderException, FogReportException {
        return prepareTransaction(
                recipient,
                new Amount(amountPicoMOB, TokenId.MOB),
                new Amount(feePicoMOB, TokenId.MOB),
                TxOutMemoBuilder.createDefaultRTHMemoBuilder()
        );
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
            @NonNull final Amount amount,
            @NonNull final Amount fee,
            @NonNull final TxOutMemoBuilder txOutMemoBuilder
    ) throws InsufficientFundsException, FragmentedAccountException, FeeRejectedException,
            InvalidFogResponse, AttestationException, NetworkException,
            TransactionBuilderException, FogReportException {
        return this.prepareTransaction(
                recipient,
                amount,
                fee,
                txOutMemoBuilder,
                ChaCha20Rng.withRandomSeed()
        );
    }

    /**
     * @param recipient {@link PublicAddress} of the recipient
     * @param amount    transaction amount
     * @param fee       transaction fee (see {@link MobileCoinClient#estimateTotalFee})
     * @param rng       Random Number Generator to pass to {@link TransactionBuilder}
     * @return {@link PendingTransaction} which encapsulates the {@link Transaction} and {@link
     * Receipt} objects
     */
    @NonNull
    public PendingTransaction prepareTransaction(
            @NonNull final PublicAddress recipient,
            @NonNull final Amount amount,
            @NonNull final Amount fee,
            @NonNull final TxOutMemoBuilder txOutMemoBuilder,
            @NonNull final ChaCha20Rng rng
    ) throws InsufficientFundsException, FragmentedAccountException, FeeRejectedException,
            InvalidFogResponse, AttestationException, NetworkException,
            TransactionBuilderException, FogReportException {
        Logger.i(TAG, "PrepareTransaction call", null,
                "recipient:", recipient,
                "amount:", amount,
                "fee:", fee);
        if(!amount.getTokenId().equals(fee.getTokenId())) {
            throw new IllegalArgumentException("Mixed token type transactions not supported");
        }
        Set<OwnedTxOut> unspent = txOuts.stream().filter(p -> !p.isSpent(getBlockIndex()))
                .filter(utxo -> utxo.getAmount().getTokenId().equals(amount.getTokenId()))
                .collect(Collectors.toCollection(HashSet::new));
        Amount finalAmount = amount.add(fee);
        Amount totalAvailable = unspent.stream()
                .map(OwnedTxOut::getAmount)
                .reduce(
                        new Amount(BigInteger.ZERO, amount.getTokenId()),
                        Amount::add
                );
        if (totalAvailable.compareTo(finalAmount) < 0) {
            throw new InsufficientFundsException();
        }
        // the custom fee is provided, no need to calculate a new fee
        UTXOSelector.Selection<OwnedTxOut> selection = UTXOSelector.selectInputsForAmount(unspent,
                finalAmount.getValue(),
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.ZERO,
                0
        );
        return mobileCoinClient.prepareTransaction(
                recipient,
                amount,
                selection.txOuts,
                fee,
                txOutMemoBuilder,
                rng
        );
    }
}
