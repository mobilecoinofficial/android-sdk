// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FeeRejectedException;
import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.FragmentedAccountException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidReceiptException;
import com.mobilecoin.lib.exceptions.InvalidTransactionException;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;
import com.mobilecoin.lib.log.LogAdapter;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.uri.ConsensusUri;
import com.mobilecoin.lib.uri.FogUri;
import com.mobilecoin.lib.util.Result;
import com.mobilecoin.lib.util.Task;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import consensus_common.ConsensusCommon;
import fog_ledger.Ledger;

/**
 * High level client for MobileCoin blockchain
 */
public class MobileCoinClient {
    public enum FeeLevel {
        MINIMUM
    }

    static final BigInteger TX_FEE = BigInteger.valueOf(10000000000L);
    static final BigInteger INPUT_FEE = BigInteger.valueOf(0L);
    static final BigInteger OUTPUT_FEE = BigInteger.valueOf(0L);
    private static final String TAG = MobileCoinClient.class.toString();
    private static final int STATUS_CHECK_DELAY_MS = 1000;
    private static final int STATUS_MAX_RETRIES = 300;
    private static final int DEFAULT_RING_SIZE = 11;
    private static final long DEFAULT_NEW_TX_BLOCK_ATTEMPTS = 50;
    private final AccountKey accountKey;
    private final TxOutStore txOutStore;
    private final ClientConfig clientConfig;
    private final StorageAdapter cacheStorage;
    private final FogReportsManager fogReportsManager;
    final FogBlockClient blockClient;
    final FogUntrustedClient untrustedClient;
    final AttestedViewClient viewClient;
    final AttestedLedgerClient ledgerClient;
    final AttestedConsensusClient consensusClient;

    /**
     * Construct new {@link MobileCoinClient} instance
     *
     * @param accountKey   user's accountKey
     * @param fogUri       a complete URI for the fog service
     * @param consensusUri a complete URI for the consensus service
     */
    public MobileCoinClient(
            @NonNull AccountKey accountKey,
            @NonNull Uri fogUri,
            @NonNull Uri consensusUri
    ) throws InvalidUriException {
        this(accountKey, fogUri, consensusUri, ClientConfig.defaultConfig());
    }

    /**
     * Construct new {@link MobileCoinClient} instance
     * If the service URIs don't specify the ports explicitly, port 443 will be used by default.
     *
     * @param accountKey   user's accountKey
     * @param fogUri       a complete URI for the fog service
     * @param consensusUri a complete URI for the consensus service
     * @param clientConfig fog and blockchain services networking and attestation configuration
     */
    public MobileCoinClient(
            @NonNull AccountKey accountKey,
            @NonNull Uri fogUri,
            @NonNull Uri consensusUri,
            @NonNull ClientConfig clientConfig
    ) throws InvalidUriException {
        Logger.i(TAG, "Creating MobileCoinClient");
        this.accountKey = accountKey;
        this.clientConfig = clientConfig;
        this.cacheStorage = clientConfig.storageAdapter;
        FogUri normalizedFogUri = new FogUri(fogUri);
        this.viewClient = new AttestedViewClient(normalizedFogUri, clientConfig.fogView);
        this.ledgerClient = new AttestedLedgerClient(normalizedFogUri, clientConfig.fogLedger);
        this.consensusClient = new AttestedConsensusClient(new ConsensusUri(consensusUri),
                clientConfig.consensus);
        this.blockClient = new FogBlockClient(normalizedFogUri, clientConfig.fogLedger);
        this.untrustedClient = new FogUntrustedClient(normalizedFogUri, clientConfig.fogLedger);
        this.txOutStore = new TxOutStore(accountKey);
        this.fogReportsManager = new FogReportsManager();
        // add client provided log adapter
        LogAdapter logAdapter = clientConfig.logAdapter;
        if (null != logAdapter) {
            Logger.addAdapter(logAdapter);
        }
    }

    /**
     * Fetch the latest account snapshot.
     */
    @NonNull
    public AccountSnapshot getAccountSnapshot() throws NetworkException,
            InvalidFogResponse, AttestationException {
        return Objects.requireNonNull(getAccountSnapshot(UnsignedLong.MAX_VALUE));
    }

    /**
     * Create an account snapshot for the provided block index.
     *
     * @param blockIndex is the index of the last block to include TxOuts from
     * @return {@link AccountSnapshot} or {@code null} if the {@code blockIndex} is higher than
     * what fog currently at and not equals to {@link UnsignedLong#MAX_VALUE}
     */
    @Nullable
    public AccountSnapshot getAccountSnapshot(UnsignedLong blockIndex) throws NetworkException,
            InvalidFogResponse, AttestationException {
        Logger.i(TAG, "GetAccountSnapshot call");
        TxOutStore txOutStore = getTxOutStore();
        UnsignedLong storeIndex = txOutStore.getCurrentBlockIndex();
        if (storeIndex.compareTo(blockIndex) < 0) {
            txOutStore.refresh(
                    viewClient,
                    ledgerClient,
                    blockClient
            );
            // refresh store index
            storeIndex = txOutStore.getCurrentBlockIndex();
        }
        // if the requested blockIndex is higher than what was retrieved from Fog but not MAX_VALUE
        // return null as the request cannot be fulfilled at this moment
        if (blockIndex.compareTo(UnsignedLong.MAX_VALUE) < 0
                && blockIndex.compareTo(storeIndex) > 0) {
            return null;
        }
        Set<OwnedTxOut> txOuts = txOutStore.getSyncedTxOuts().stream()
                .filter(txOut -> txOut.getReceivedBlockIndex().compareTo(blockIndex) <= 0)
                .collect(Collectors.toSet());

        final UnsignedLong finalBlockIndex = (storeIndex.compareTo(blockIndex) > 0)
                ? blockIndex
                : storeIndex;
        return new AccountSnapshot(this, txOuts, finalBlockIndex);
    }

    /**
     * Retrieve accountKey's balance
     */
    @NonNull
    public Balance getBalance() throws InvalidFogResponse, NetworkException, AttestationException {
        Logger.i(TAG, "GetBalance call");
        return getAccountSnapshot().getBalance();
    }

    /**
     * Calculate the total transferable amount excluding all the required fees for such transfer
     */
    @NonNull
    public BigInteger getTransferableAmount() throws NetworkException, InvalidFogResponse,
            AttestationException {
        Logger.i(TAG, "GetTransferableAmount call");
        return getAccountSnapshot().getTransferableAmount();
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
        Set<OwnedTxOut> unspent = getUnspentTxOuts();
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
        return prepareTransaction(
                recipient,
                amount,
                selection.txOuts,
                fee
        );
    }

    @NonNull
    PendingTransaction prepareTransaction(
            @NonNull final PublicAddress recipient,
            @NonNull final BigInteger amount,
            @NonNull final List<OwnedTxOut> txOuts,
            @NonNull final BigInteger fee
    ) throws InvalidFogResponse, AttestationException, NetworkException,
            TransactionBuilderException, FogReportException {
        Logger.i(TAG, "PrepareTransaction with TxOuts call", null,
                "recipient:", recipient,
                "amount:", amount,
                "fee:", fee);
        final RistrettoPrivate viewKey = accountKey.getViewKey();
        UnsignedLong blockIndex = txOutStore.getCurrentBlockIndex();
        UnsignedLong tombstoneBlockIndex = blockIndex
                .add(UnsignedLong.fromLongBits(DEFAULT_NEW_TX_BLOCK_ATTEMPTS));
        HashSet<FogUri> reportUris = new HashSet<>();
        try {
            if (recipient.hasFogInfo()) {
                reportUris.add(new FogUri(recipient.getFogReportUri()));
            }
            reportUris.add(new FogUri(getAccountKey().getFogReportUri()));
        } catch (InvalidUriException exception) {
            FogReportException reportException = new FogReportException("Invalid Fog Report " +
                    "Uri in the public address");
            Util.logException(TAG, reportException);
            throw reportException;
        }
        // fetch reports and rings in parallel
        long startTime = System.currentTimeMillis();
        Task<FogReportResponses, Exception> fetchReportsTask =
                new Task<FogReportResponses, Exception>() {
                    @Override
                    public FogReportResponses execute() throws Exception {
                        return fogReportsManager.fetchReports(reportUris,
                                tombstoneBlockIndex, clientConfig.report);
                    }
                };

        Task<List<Ring>, Exception> fetchRingsTask = new Task<List<Ring>, Exception>() {
            @Override
            public List<Ring> execute() throws Exception {
                return getRingsForUTXOs(
                        txOuts,
                        getTxOutStore().getLedgerTotalTxCount()
                );
            }
        };

        // allocate two worker threads for the two tasks
        ExecutorService fixedExecutorService =
                Executors.newFixedThreadPool(2);

        Future<Result<FogReportResponses, Exception>> fogReportResponsesFuture =
                fixedExecutorService.submit(fetchReportsTask);

        Future<Result<List<Ring>, Exception>> ringsListFuture =
                fixedExecutorService.submit(fetchRingsTask);

        // signal the executor to shutdown when the tasks are complete
        // this is not a blocking call
        fixedExecutorService.shutdown();

        List<Ring> rings;
        FogReportResponses fogReportResponses;
        try {
            Result<List<Ring>, Exception> ringsResult = ringsListFuture.get();
            if (ringsResult.isErr()) {
                // isError indicated that the error is non-null
                throw Objects.requireNonNull(ringsResult.getError());
            } else if (ringsResult.isOk()) {
                rings = Objects.requireNonNull(ringsResult.getValue());
            } else {
                throw new InvalidFogResponse("Unable to retrieve Rings");
            }
            Result<FogReportResponses, Exception> reportsResult = fogReportResponsesFuture.get();
            if (reportsResult.isErr()) {
                // isError indicated that the error is non-null
                throw Objects.requireNonNull(reportsResult.getError());
            } else if (reportsResult.isOk()) {
                fogReportResponses = Objects.requireNonNull(reportsResult.getValue());
            } else {
                throw new InvalidFogResponse("Unable to retrieve Fog Reports");
            }
        } catch (FogReportException | InvalidFogResponse | AttestationException | NetworkException exception) {
            Util.logException(TAG, exception);
            throw exception;
        } catch (InterruptedException | ExecutionException exception) {
            NetworkException networkException =
                    new NetworkException(504, "Timeout fetching fog reports", exception);
            Util.logException(TAG, networkException);
            throw networkException;
        } catch (Exception exception) {
            Logger.wtf(TAG, "Bug: Unexpected exception", exception);
            throw new IllegalStateException(exception);
        }
        long endTime = System.currentTimeMillis();
        Logger.d(TAG, "Report + Rings fetch time: " + (endTime - startTime) + "ms");
        FogResolver fogResolver = new FogResolver(fogReportResponses,
                clientConfig.report.getVerifier());
        TransactionBuilder txBuilder = new TransactionBuilder(fogResolver);
        BigInteger totalAmount = BigInteger.valueOf(0);
        for (Ring ring : rings) {
            OwnedTxOut utxo = ring.utxo;
            totalAmount = totalAmount.add(utxo.getValue());

            RistrettoPrivate onetimePrivateKey = Util.recoverOnetimePrivateKey(utxo.getPublicKey(),
                    viewKey,
                    accountKey.getSubAddressSpendKey()
            );

            txBuilder.addInput(ring.getNativeTxOuts(),
                    ring.getNativeTxOutMembershipProofs(),
                    ring.realIndex,
                    onetimePrivateKey,
                    viewKey
            );
        }
        byte[] confirmationNumberOut = new byte[Receipt.CONFIRMATION_NUMBER_LENGTH];
        TxOut pendingTxo = txBuilder.addOutput(amount,
                recipient,
                confirmationNumberOut
        );

        BigInteger finalAmount = amount.add(fee);

        if (totalAmount.compareTo(finalAmount) > 0) { // if total amount > finalAmount
            BigInteger change = totalAmount.subtract(finalAmount);
            txBuilder.addOutput(change,
                    accountKey.getPublicAddress(),
                    null
            );
        }
        txBuilder.setTombstoneBlockIndex(tombstoneBlockIndex);
        txBuilder.setFee(fee.longValue());

        Transaction transaction = txBuilder.build();
        Amount pendingAmount = pendingTxo.getAmount();
        Receipt receipt = new Receipt(pendingTxo.getPubKey(),
                confirmationNumberOut,
                pendingAmount,
                tombstoneBlockIndex
        );
        return new PendingTransaction(
                transaction,
                receipt
        );
    }

    /**
     * Submit transaction to the consensus service
     *
     * @param transaction a valid transaction object to submit (see
     *                    {@link MobileCoinClient#prepareTransaction}}
     */
    public void submitTransaction(@NonNull Transaction transaction)
            throws InvalidTransactionException, NetworkException, AttestationException {
        Logger.i(TAG, "SubmitTransaction call", null,
                "transaction:", transaction);
        ConsensusCommon.ProposeTxResponse txResponse =
                consensusClient.proposeTx(transaction.toProtoBufObject());
        int code = txResponse.getResult().getNumber();
        if (0 != code) {
            String message = txResponse.getResult().toString();
            InvalidTransactionException invalidTransactionException =
                    new InvalidTransactionException(message);
            Util.logException(TAG, invalidTransactionException);
            throw invalidTransactionException;
        }
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
    public Receipt.Status getReceiptStatus(@NonNull Receipt receipt)
            throws InvalidFogResponse, NetworkException, AttestationException,
            InvalidReceiptException {
        Logger.i(TAG, "GetReceiptStatus call");
        return getAccountSnapshot().getReceiptStatus(receipt);
    }

    /**
     * Check the status of the transaction. Sender's key is required to decode verification data,
     * hence only the sender of the transaction can verify it's status. Recipients should use {@link
     * MobileCoinClient#getReceiptStatus}
     *
     * @param transaction obtained from {@link MobileCoinClient#prepareTransaction}
     * @return {@link Transaction.Status}
     */
    @NonNull
    public Transaction.Status getTransactionStatus(@NonNull Transaction transaction)
            throws InvalidFogResponse, AttestationException,
            NetworkException {
        Logger.i(TAG, "GetTransactionStatus call");
        Ledger.CheckKeyImagesResponse keyImagesResponse =
                ledgerClient.checkKeyImages(transaction.getKeyImages());
        getTxOutStore().updateTxOutsSpentState(keyImagesResponse);
        UnsignedLong ledgerBlockIndex =
                UnsignedLong.fromLongBits(keyImagesResponse.getNumBlocks()).sub(UnsignedLong.ONE);
        List<Ledger.KeyImageResult> keyImageResults = keyImagesResponse.getResultsList();
        boolean txHasUnspentKeyImages = false;
        for (Ledger.KeyImageResult keyImageResult : keyImageResults) {
            if (keyImageResult.getKeyImageResultCode() != Ledger.KeyImageResultCode.Spent_VALUE) {
                txHasUnspentKeyImages = true;
                break;
            }
        }
        if (!txHasUnspentKeyImages) {
            Set<RistrettoPublic> outputPublicKeys = transaction.getOutputPublicKeys();
            Ledger.TxOutResponse response =
                    untrustedClient.fetchTxOuts(outputPublicKeys);
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
            if (allTxOutsFound) {
                return Transaction.Status.ACCEPTED.atBlock(outputBlockIndex);
            }
            return Transaction.Status.FAILED.atBlock(ledgerBlockIndex);
        }
        return Transaction.Status.UNKNOWN.atBlock(ledgerBlockIndex);
    }

    /**
     * The minimum fee required to send a transaction with the specified amount. The account balance
     * consists of multiple coins, if there are no big enough coins to successfully send the
     * transaction {@link FragmentedAccountException} will be thrown. The account needs to be
     * defragmented in order to send the specified amount. See
     * {@link MobileCoinClient#defragmentAccount}
     *
     * @param amount an amount value in picoMob
     */
    @NonNull
    public BigInteger estimateTotalFee(@NonNull BigInteger amount, @NonNull FeeLevel feeLevel)
            throws InsufficientFundsException, NetworkException, InvalidFogResponse,
            AttestationException {
        Logger.i(TAG, "EstimateTotalFee call");
        return UTXOSelector.calculateFee(
                getUnspentTxOuts(),
                amount,
                TX_FEE,
                INPUT_FEE,
                OUTPUT_FEE,
                2);
    }

    @NonNull
    public BigInteger estimateTotalFee(@NonNull BigInteger amount)
            throws InsufficientFundsException, NetworkException, AttestationException,
            InvalidFogResponse {
        return estimateTotalFee(amount, FeeLevel.MINIMUM);
    }

    /**
     * The account balance consists of multiple coins, if there are no big enough coins to
     * successfully send transaction, the account needs to be defragmented. If the account is too
     * fragmented, there may be a need to defragment the account more than once. However, wallet
     * fragmentation is a rare occurrence since there is an internal mechanism to defragment the
     * account during other operations.
     *
     * @param delegate monitors and controls the defragmentation process
     */
    public void defragmentAccount(
            @NonNull BigInteger amountToSend,
            @NonNull DefragmentationDelegate delegate
    ) throws InvalidFogResponse, AttestationException, NetworkException, InsufficientFundsException,
            TransactionBuilderException, InvalidTransactionException,
            FogReportException, TimeoutException {
        delegate.onStart();
        UTXOSelector.Selection<OwnedTxOut> inputSelectionForAmount = null;
        do {
            Set<OwnedTxOut> unspent = getUnspentTxOuts();
            try {
                inputSelectionForAmount = UTXOSelector.selectInputsForAmount(
                        unspent,
                        amountToSend,
                        TX_FEE,
                        INPUT_FEE,
                        OUTPUT_FEE, 1);
            } catch (FragmentedAccountException exception) {
                UTXOSelector.Selection<OwnedTxOut> selection = UTXOSelector.selectInputsForMerging(
                        unspent,
                        TX_FEE,
                        INPUT_FEE,
                        OUTPUT_FEE
                );
                BigInteger totalValue = BigInteger.ZERO;
                for (OwnedTxOut utxo : selection.txOuts) {
                    totalValue = totalValue.add(utxo.getValue());
                }
                PendingTransaction pendingTransaction = prepareTransaction(
                        accountKey.getPublicAddress(),
                        totalValue.subtract(selection.fee),
                        selection.txOuts,
                        selection.fee
                );
                if (!delegate.onStepReady(pendingTransaction, selection.fee)) {
                    delegate.onCancel();
                    return;
                }
                // make sure the previous Tx is posted
                Receipt.Status status;
                int queryTries = 0;
                try {
                    while ((status = getReceiptStatus(pendingTransaction.getReceipt()))
                            == Receipt.Status.UNKNOWN) {
                        if (queryTries++ == STATUS_MAX_RETRIES) {
                            Logger.w(TAG, "Exceeded waiting time for the transaction to post");
                            throw new TimeoutException();
                        }
                        try {
                            Thread.sleep(STATUS_CHECK_DELAY_MS);
                        } catch (InterruptedException interruptedException) {
                            Logger.w(TAG, "Sleep interruption during defragmentation");
                        }
                    }
                } catch (InvalidReceiptException invalidReceiptException) {
                    IllegalStateException illegalStateException =
                            new IllegalStateException(invalidReceiptException);
                    Logger.e(TAG, "BUG: unreachable code", illegalStateException);
                    throw illegalStateException;
                }
                if (status == Receipt.Status.FAILED) {
                    throw new InvalidTransactionException("Defrag step transaction has failed");
                }
            }
        } while (inputSelectionForAmount == null);
        delegate.onComplete();
    }

    /**
     * Returns whether the defragmentation is required on the active account
     * in order to send the specified amount
     */
    public boolean requiresDefragmentation(@NonNull BigInteger amountToSend)
            throws NetworkException, InvalidFogResponse, AttestationException,
            InsufficientFundsException {
        try {
            UTXOSelector.selectInputsForAmount(
                    getUnspentTxOuts(),
                    amountToSend,
                    TX_FEE,
                    INPUT_FEE,
                    OUTPUT_FEE, 1);
        } catch (FragmentedAccountException exception) {
            return true;
        }
        return false;
    }

    @NonNull
    TxOutStore getTxOutStore() {
        return txOutStore;
    }

    /**
     * Retrieve the list of account's unspent TxOuts
     */
    @NonNull
    Set<OwnedTxOut> getUnspentTxOuts() throws InvalidFogResponse, NetworkException,
            AttestationException {
        getTxOutStore().refresh(
                viewClient,
                ledgerClient,
                blockClient
        );
        return getTxOutStore().getUnspentTxOuts();
    }

    /**
     * Retrieve the account activity
     */
    @NonNull
    public AccountActivity getAccountActivity() throws NetworkException, InvalidFogResponse,
            AttestationException {
        txOutStore.refresh(viewClient, ledgerClient, blockClient);
        Set<OwnedTxOut> txOuts = txOutStore.getSyncedTxOuts();
        return new AccountActivity(txOuts,
                getTxOutStore().getCurrentBlockIndex().add(UnsignedLong.ONE));
    }

    @NonNull
    List<Ring> getRingsForUTXOs(
            @NonNull List<OwnedTxOut> utxos,
            @NonNull UnsignedLong numTxOutsInLedger
    ) throws InvalidFogResponse, NetworkException, AttestationException {
        // Sanity check to ensure all UTXOs have unique indices
        HashSet<UnsignedLong> indices = new HashSet<>();
        for (OwnedTxOut utxo : utxos) {
            if (!indices.add(utxo.getTxOutGlobalIndex())) {
                throw new IllegalStateException("utxos contains non-unique indices");
            }
        }

        // Figure out how many total outputs we need to get.
        int count = utxos.size() * DEFAULT_RING_SIZE;
        if (count > numTxOutsInLedger.intValue()) {
            throw new InvalidFogResponse("Ledger does not contain enough outputs");
        }

        HashSet<UnsignedLong> realIndices = new HashSet<>(indices);
        // Continue selecting random indices until we got our desired amount.
        Random rnd = new Random();
        while (indices.size() != count) {
            UnsignedLong index = UnsignedLong.valueOf(Math.abs(rnd.nextLong()))
                    .remainder(numTxOutsInLedger);
            indices.add(index);
        }

        // Query the ledger server.
        Ledger.GetOutputsResponse outputsResponse = ledgerClient.getOutputs(
                new ArrayList<>(indices),
                0
        );
        List<Ledger.OutputResult> outs = outputsResponse.getResultsList();

        if (outs.size() != count) {
            throw new InvalidFogResponse("getOutputs returned incorrect number of outputs");
        }

        // Split the results into real outputs and ring outputs.
        HashMap<UnsignedLong, Ledger.OutputResult> realOutputs = new HashMap<>();
        List<Ledger.OutputResult> ringPool = new ArrayList<>();

        for (Ledger.OutputResult out : outs) {
            if (realIndices.contains(UnsignedLong.fromLongBits(out.getIndex()))) {
                realOutputs.put(
                        UnsignedLong.fromLongBits(out.getIndex()),
                        out
                );
            } else {
                ringPool.add(out);
            }
        }

        // Sanity
        if (realIndices.size() != utxos.size()) {
            throw new IllegalStateException("BUG");
        }

        // Construct the list of rings.
        List<Ring> rings = new ArrayList<>();
        for (OwnedTxOut utxo : utxos) {
            short realIndex = (short) rnd.nextInt(DEFAULT_RING_SIZE);
            List<MobileCoinAPI.TxOut> txOuts = new ArrayList<>();
            List<MobileCoinAPI.TxOutMembershipProof> proofs = new ArrayList<>();

            for (int i = 0; i < DEFAULT_RING_SIZE; ++i) {
                Ledger.OutputResult out;
                if (i == realIndex) {
                    out = realOutputs.get(utxo.getTxOutGlobalIndex());
                } else {
                    out = ringPool.remove(0);
                }
                assert (out != null);
                txOuts.add(out.getOutput());
                proofs.add(out.getProof());
            }
            try {
                rings.add(new Ring(
                        txOuts,
                        proofs,
                        realIndex,
                        utxo
                ));
            } catch (Exception ex) {
                throw new InvalidFogResponse("Unable to decode rings");
            }
        }

        // Sanity
        if (!ringPool.isEmpty()) {
            throw new IllegalStateException("BUG: Not all rings consumed");
        }

        // Return
        return rings;
    }

    /**
     * AccountKey associated with this client instance.
     */
    @NonNull
    public final AccountKey getAccountKey() {
        return accountKey;
    }

    /**
     * Set the basic HTTP authorization username and password for future requests
     */
    public void setAuthorization(
            @NonNull String username,
            @NonNull String password
    ) {
        viewClient.setAuthorization(
                username,
                password
        );
        ledgerClient.setAuthorization(
                username,
                password
        );
        consensusClient.setAuthorization(
                username,
                password
        );
        blockClient.setAuthorization(
                username,
                password
        );
        untrustedClient.setAuthorization(
                username,
                password
        );
    }

    /**
     * Attempt to gracefully shutdown internal networking and threading services This is a blocking
     * call which in rare cases may take up to 10 seconds to complete.
     */
    public synchronized void shutdown() {
        if (null != viewClient) {
            viewClient.shutdown();
        }
        if (null != ledgerClient) {
            ledgerClient.shutdown();
        }
        if (null != consensusClient) {
            consensusClient.shutdown();
        }
        if (null != blockClient) {
            blockClient.shutdown();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }
}
