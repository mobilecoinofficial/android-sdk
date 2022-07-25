// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FeeRejectedException;
import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.FogSyncException;
import com.mobilecoin.lib.exceptions.FragmentedAccountException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidReceiptException;
import com.mobilecoin.lib.exceptions.InvalidTransactionException;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.exceptions.StorageNotFoundException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;
import com.mobilecoin.lib.log.LogAdapter;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.uri.ConsensusUri;
import com.mobilecoin.lib.network.uri.FogUri;
import com.mobilecoin.lib.network.uri.MobileCoinUri;
import com.mobilecoin.lib.util.Result;
import com.mobilecoin.lib.util.Task;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
 * <pre>
 * The {@link MobileCoinClient} class is a high-level Fog-enabled client to access MobileCoin
 * blockchain.
 *
 * Fog-enabled {@link AccountKey} is required to use {@code MobileCoinClient}.
 * </pre>
 */
public final class MobileCoinClient implements MobileCoinAccountClient, MobileCoinTransactionClient,
    MobileCoinNetworkManager {

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
    final FogBlockClient fogBlockClient;
    final FogUntrustedClient untrustedClient;
    final AttestedViewClient viewClient;
    final AttestedLedgerClient ledgerClient;
    final AttestedConsensusClient consensusClient;
    final BlockchainClient blockchainClient;

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
            @NonNull Uri consensusUri,
            @NonNull TransportProtocol transportProtocol
    ) throws InvalidUriException {
        this(accountKey, fogUri, consensusUri, ClientConfig.defaultConfig(), transportProtocol);
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
            @NonNull ClientConfig clientConfig,
            @NonNull TransportProtocol transportProtocol
    ) throws InvalidUriException {
        this(accountKey, fogUri, Collections.singletonList(consensusUri), clientConfig, transportProtocol);
    }

    /**
     * Construct new {@link MobileCoinClient} instance
     * If the service URIs don't specify the ports explicitly, port 443 will be used by default.
     *
     * @param accountKey   user's accountKey
     * @param fogUri       a complete URI for the fog service
     * @param consensusUris a list of complete URIs for the consensus service
     * @param clientConfig fog and blockchain services networking and attestation configuration
     */
    public MobileCoinClient(
        @NonNull AccountKey accountKey,
        @NonNull Uri fogUri,
        @NonNull List<Uri> consensusUris,
        @NonNull ClientConfig clientConfig,
        @Nullable TransportProtocol transportProtocol
    ) throws InvalidUriException {
        Logger.i(TAG, "Creating MobileCoinClient");
        this.accountKey = accountKey;
        this.clientConfig = clientConfig;
        this.cacheStorage = clientConfig.storageAdapter;
        FogUri normalizedFogUri = new FogUri(fogUri);
        List<MobileCoinUri> normalizedConsensusUris = createNormalizedConsensusUris(consensusUris);
        this.blockchainClient = new BlockchainClient(
            RandomLoadBalancer.create(normalizedConsensusUris),
            clientConfig.consensus,
            clientConfig.minimumFeeCacheTTLms,
            transportProtocol
        );
        this.viewClient = new AttestedViewClient(RandomLoadBalancer.create(normalizedFogUri),
            clientConfig.fogView, transportProtocol);
        this.ledgerClient = new AttestedLedgerClient(RandomLoadBalancer.create(normalizedFogUri),
            clientConfig.fogLedger, transportProtocol);
        this.consensusClient = new AttestedConsensusClient(
            RandomLoadBalancer.create(normalizedConsensusUris),
            clientConfig.consensus, transportProtocol);
        this.fogBlockClient = new FogBlockClient(RandomLoadBalancer.create(normalizedFogUri),
            clientConfig.fogLedger, transportProtocol);
        this.untrustedClient = new FogUntrustedClient(RandomLoadBalancer.create(normalizedFogUri),
            clientConfig.fogLedger, transportProtocol);
        this.txOutStore = createTxOutStore(accountKey);
        this.fogReportsManager = new FogReportsManager(transportProtocol);
        // add client provided log adapter
        LogAdapter logAdapter = clientConfig.logAdapter;
        if (null != logAdapter) {
            Logger.addAdapter(logAdapter);
        }
    }

    @VisibleForTesting
    MobileCoinClient(
            AccountKey accountKey,
            TxOutStore txOutStore,
            ClientConfig clientConfig,
            StorageAdapter cacheStorage,
            FogReportsManager fogReportsManager,
            FogBlockClient fogBlockClient,
            FogUntrustedClient untrustedClient,
            AttestedViewClient viewClient,AttestedLedgerClient ledgerClient,
            AttestedConsensusClient consensusClient,
            BlockchainClient blockchainClient
    ) {
        this.accountKey = accountKey;
        this.txOutStore = txOutStore;
        this.clientConfig = clientConfig;
        this.cacheStorage = cacheStorage;
        this.fogReportsManager = fogReportsManager;
        this.fogBlockClient = fogBlockClient;
        this.untrustedClient = untrustedClient;
        this.viewClient = viewClient;
        this.ledgerClient = ledgerClient;
        this.consensusClient = consensusClient;
        this.blockchainClient = blockchainClient;
    }

    @NonNull
    FogUntrustedClient getUntrustedClient() {
        return untrustedClient;
    }

    private List<MobileCoinUri> createNormalizedConsensusUris(List<Uri> consensusUris)
        throws InvalidUriException {
        List<MobileCoinUri> normalizedConsensusUris = new ArrayList<>();
        for (Uri consensusUri : consensusUris) {
          normalizedConsensusUris.add(new ConsensusUri(consensusUri));
        }

        return normalizedConsensusUris;
    }

    private TxOutStore createTxOutStore(AccountKey accountKey) {
        String txOutStoreStorageKey = TxOutStore.createStorageKey(accountKey);
        if(cacheStorage != null && cacheStorage.has(txOutStoreStorageKey)) {
            byte[] serializedTxOutStore = cacheStorage.get(txOutStoreStorageKey);
            return deserializeTxOutStore(serializedTxOutStore);
        }

        return new TxOutStore(accountKey);
    }

    private TxOutStore deserializeTxOutStore(byte[] serializedTxOutStore) {
        try {
            return TxOutStore.fromBytes(serializedTxOutStore);
        } catch (SerializationException e) {
            Logger.i(TAG, "Failed to deserialize the serialized TxOutStore:" + e.getMessage());
            return new TxOutStore(accountKey);
        }
    }

    @Override
    public void cacheUserData()
        throws StorageNotFoundException, SerializationException {
        if (cacheStorage == null) {
            throw new StorageNotFoundException(
                "Data cannot be persisted because no cache storage is available.");
        }

        String txOutStoreStorageKey = TxOutStore.createStorageKey(accountKey);
        byte[] serializedTxOutStore = txOutStore.toByteArray();

        cacheStorage.set(txOutStoreStorageKey, serializedTxOutStore);
    }

    @Override
    @NonNull
    public AccountSnapshot getAccountSnapshot() throws NetworkException,
            InvalidFogResponse, AttestationException, FogSyncException {
        return Objects.requireNonNull(getAccountSnapshot(UnsignedLong.MAX_VALUE));
    }

    @Override
    @Nullable
    public AccountSnapshot getAccountSnapshot(UnsignedLong blockIndex) throws NetworkException,
            InvalidFogResponse, AttestationException, FogSyncException {
        Logger.i(TAG, "GetAccountSnapshot call");
        TxOutStore txOutStore = getTxOutStore();
        UnsignedLong storeIndex = txOutStore.getCurrentBlockIndex();
        if (storeIndex.compareTo(blockIndex) < 0) {
            try {
                txOutStore.refresh(
                        viewClient,
                        ledgerClient,
                        fogBlockClient
                );
            } catch(FogSyncException e) {
                if(blockIndex.compareTo(storeIndex) >= 0) {
                    throw e;
                }
            }
            // refresh store index
            storeIndex = txOutStore.getCurrentBlockIndex();
        }
        // if the requested blockIndex is higher than what was retrieved from Fog but not MAX_VALUE
        // return null as the request cannot be fulfilled at this moment
        if (blockIndex.compareTo(UnsignedLong.MAX_VALUE) < 0
                && blockIndex.compareTo(storeIndex) > 0) {
            return null;
        }
        final UnsignedLong finalBlockIndex = (storeIndex.compareTo(blockIndex) > 0)
                ? blockIndex
                : storeIndex;
        Set<OwnedTxOut> txOuts = txOutStore.getSyncedTxOuts().stream()
                .filter(txOut -> txOut.getReceivedBlockIndex().compareTo(finalBlockIndex) <= 0)
                .map(OwnedTxOut::new)
                .collect(Collectors.toSet());

        return new AccountSnapshot(this, txOuts, finalBlockIndex);
    }

    @Deprecated
    @Override
    @NonNull
    public Balance getBalance() throws AttestationException, InvalidFogResponse, NetworkException {
        try {
            return getBalance(TokenId.MOB);
        } catch (FogSyncException e) {
            throw new NetworkException(NetworkResult.INTERNAL, e);
        }
    }

    @Override
    @NonNull
    public Balance getBalance(TokenId tokenId) throws AttestationException, InvalidFogResponse, NetworkException, FogSyncException {
        Logger.i(TAG, "GetBalance call");
        return getAccountSnapshot().getBalance(tokenId);
    }

    @Override
    @NonNull
    public Map<TokenId, Balance> getBalances() throws AttestationException, InvalidFogResponse, NetworkException, FogSyncException {
        return getAccountSnapshot().getBalances();
    }

    @Deprecated
    @Override
    @NonNull
    public BigInteger getTransferableAmount() throws NetworkException, InvalidFogResponse,
            AttestationException {
        try {
            return getTransferableAmount(TokenId.MOB).getValue();
        } catch (FogSyncException e) {
            throw new NetworkException(NetworkResult.INTERNAL, e);
        }
    }

    @Override
    @NonNull
    public Amount getTransferableAmount(@NonNull TokenId tokenId) throws NetworkException, InvalidFogResponse,
            AttestationException, FogSyncException {
        Logger.i(TAG, "GetTransferableAmount call");
        return getAccountSnapshot().getTransferableAmount(getOrFetchMinimumTxFee(tokenId));
    }

    @Deprecated
    @Override
    @NonNull
    public PendingTransaction prepareTransaction(
            @NonNull final PublicAddress recipient,
            @NonNull final BigInteger amountPicoMOB,
            @NonNull final BigInteger feePicoMOB
    ) throws InsufficientFundsException, FragmentedAccountException, FeeRejectedException,
            InvalidFogResponse, AttestationException, NetworkException,
            TransactionBuilderException, FogReportException {
        try {
            return prepareTransaction(
                    recipient,
                    new Amount(amountPicoMOB, TokenId.MOB),
                    new Amount(feePicoMOB, TokenId.MOB),
                    TxOutMemoBuilder.createDefaultRTHMemoBuilder()
            );
        } catch(FogSyncException e) {
            throw new NetworkException(NetworkResult.INTERNAL, e);
        }
    }

    @Override
    @NonNull
    public PendingTransaction prepareTransaction(
            @NonNull final PublicAddress recipient,
            @NonNull final Amount amount,
            @NonNull final Amount fee,
            @NonNull final TxOutMemoBuilder txOutMemoBuilder
    ) throws InsufficientFundsException, FragmentedAccountException, FeeRejectedException,
            InvalidFogResponse, AttestationException, NetworkException,
            TransactionBuilderException, FogReportException, FogSyncException {
        return this.prepareTransaction(
                recipient,
                amount,
                fee,
                txOutMemoBuilder,
                ChaCha20Rng.withRandomSeed()
        );
    }

    @Override
    @NonNull
    public PendingTransaction prepareTransaction(
        @NonNull final PublicAddress recipient,
        @NonNull final Amount amount,
        @NonNull final Amount fee,
        @NonNull final TxOutMemoBuilder txOutMemoBuilder,
        @NonNull final ChaCha20Rng rng
    ) throws InsufficientFundsException, FragmentedAccountException, FeeRejectedException,
            InvalidFogResponse, AttestationException, NetworkException,
            TransactionBuilderException, FogReportException, FogSyncException {
        Logger.i(TAG, "PrepareTransaction call", null,
                "recipient:", recipient,
                "amount:", amount,
                "fee:", fee);
        if(!amount.getTokenId().equals(fee.getTokenId())) {
            throw new IllegalArgumentException("Mixed token type transactions not supported");
        }
        Set<OwnedTxOut> unspent = getUnspentTxOuts(amount.getTokenId());
        Amount finalAmount = amount.add(fee);
        Amount totalAvailable = unspent.stream()
                .map(OwnedTxOut::getAmount)
                .reduce(new Amount(BigInteger.ZERO, amount.getTokenId()), Amount::add);
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
        return prepareTransaction(
                recipient,
                amount,
                selection.txOuts,
                fee,
                txOutMemoBuilder,
                rng
        );
    }

    @NonNull
    PendingTransaction prepareTransaction(
        @NonNull final PublicAddress recipient,
        @NonNull final Amount amount,
        @NonNull final List<OwnedTxOut> txOuts,
        @NonNull final Amount fee,
        @NonNull final TxOutMemoBuilder txOutMemoBuilder,
        @NonNull final ChaCha20Rng rng
    ) throws InvalidFogResponse, AttestationException, NetworkException,
            TransactionBuilderException, FogReportException {
        Logger.i(TAG, "PrepareTransaction with TxOuts call", null,
                "recipient:", recipient,
                "amount:", amount,
                "fee:", fee);
        if(!amount.getTokenId().equals(fee.getTokenId())) {
            throw new IllegalArgumentException("Mixed token type transactions not supported");
        }
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
                        getTxOutStore().getLedgerTotalTxCount(),
                        rng
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
                    new NetworkException(NetworkResult.DEADLINE_EXCEEDED
                            .withDescription("Timeout fetching fog reports")
                            .withCause(exception));
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

        TransactionBuilder txBuilder = new TransactionBuilder(
                fogResolver,
                txOutMemoBuilder,
                blockchainClient.getOrFetchNetworkBlockVersion(),
                amount.getTokenId(),
                fee,
                rng
        );
        txBuilder.setFee(fee);
        txBuilder.setTombstoneBlockIndex(tombstoneBlockIndex);

        BigInteger totalAmount = BigInteger.valueOf(0);
        for (Ring ring : rings) {
            OwnedTxOut utxo = ring.utxo;
            totalAmount = totalAmount.add(utxo.getAmount().getValue());

            RistrettoPrivate onetimePrivateKey = Util.recoverOnetimePrivateKey(
                    utxo.getPublicKey(),
                    utxo.getTargetKey(),
                    accountKey
            );

            txBuilder.addInput(ring.getNativeTxOuts(),
                    ring.getNativeTxOutMembershipProofs(),
                    ring.realIndex,
                    onetimePrivateKey,
                    accountKey.getViewKey()
            );
        }
        byte[] confirmationNumberOut = new byte[Receipt.CONFIRMATION_NUMBER_LENGTH];
        final TxOutContext payloadTxOutContext = txBuilder.addOutput(
                amount.getValue(),
                recipient,
                confirmationNumberOut
        );
        TxOut pendingTxo = payloadTxOutContext.getTxOut();

        BigInteger finalAmount = amount.add(fee).getValue();

        BigInteger change = totalAmount.subtract(finalAmount);
        TxOutContext changeTxOutContext;
        if(blockchainClient.getOrFetchNetworkBlockVersion() < 1) {
            changeTxOutContext = txBuilder.addOutput(change, accountKey.getPublicAddress(), null);
        }
        else {
            changeTxOutContext = txBuilder.addChangeOutput(change, accountKey, null);
        }

        Transaction transaction = txBuilder.build();
        MaskedAmount pendingMaskedAmount = pendingTxo.getMaskedAmount();
        Receipt receipt = new Receipt(pendingTxo.getPublicKey(),
                confirmationNumberOut,
                pendingMaskedAmount,
                tombstoneBlockIndex
        );
        return new PendingTransaction(
                transaction,
                receipt,
                payloadTxOutContext,
                changeTxOutContext
        );
    }

    @Override
    public long submitTransaction(@NonNull Transaction transaction)
            throws InvalidTransactionException, NetworkException, AttestationException {
        Logger.i(TAG, "SubmitTransaction call", null,
                "transaction:", transaction);
        ConsensusCommon.ProposeTxResponse txResponse =
                consensusClient.proposeTx(transaction.toProtoBufObject());
        final long blockIndex = txResponse.getBlockCount() > 0 ? txResponse.getBlockCount() - 1L : 0;
        this.txOutStore.setConsensusBlockIndex(UnsignedLong.fromLongBits(blockIndex));
        ConsensusCommon.ProposeTxResult txResult = txResponse.getResult();
        int code = txResult.getNumber();
        if (0 != code) {
            blockchainClient.resetCache();
            InvalidTransactionException invalidTransactionException =
                    new InvalidTransactionException(txResult, UnsignedLong.fromLongBits(blockIndex));
            Util.logException(TAG, invalidTransactionException);
            throw invalidTransactionException;
        }
        return blockIndex;
    }

    @Override
    @NonNull
    public Receipt.Status getReceiptStatus(@NonNull Receipt receipt)
            throws InvalidFogResponse, NetworkException, AttestationException,
            InvalidReceiptException, FogSyncException {
        Logger.i(TAG, "GetReceiptStatus call");
        return getAccountSnapshot().getReceiptStatus(receipt);
    }

    @Override
    @NonNull
    public Transaction.Status getTransactionStatus(@NonNull Transaction transaction)
            throws InvalidFogResponse, AttestationException,
            NetworkException, FogSyncException {
        Logger.i(TAG, "GetTransactionStatus call");
        return getAccountSnapshot().getTransactionStatus(transaction);
    }

    @Deprecated
    @Override
    @NonNull
    public BigInteger estimateTotalFee(@NonNull BigInteger amountPicoMOB)
            throws InsufficientFundsException, NetworkException, InvalidFogResponse,
            AttestationException {
        try {
            return estimateTotalFee(new Amount(
                    amountPicoMOB,
                    TokenId.MOB
            )).getValue();
        } catch(FogSyncException e) {
            throw new NetworkException(NetworkResult.INTERNAL, e);
        }
    }

    @Override
    @NonNull
    public Amount estimateTotalFee(@NonNull Amount amount)
            throws InsufficientFundsException, NetworkException, InvalidFogResponse,
            AttestationException, FogSyncException {
        Logger.i(TAG, "EstimateTotalFee call");
        return new Amount(
                UTXOSelector.calculateFee(
                    getUnspentTxOuts(amount.getTokenId()),
                    amount.getValue(),
                    getOrFetchMinimumTxFee(amount.getTokenId()).getValue(),
                    INPUT_FEE,
                    OUTPUT_FEE,
                    2
                ),
                amount.getTokenId()
        );
    }

    @Deprecated
    @Override
    public void defragmentAccount(
            @NonNull BigInteger amountPicoMOB,
            @NonNull DefragmentationDelegate delegate
    ) throws InvalidFogResponse, AttestationException, NetworkException, InsufficientFundsException,
            TransactionBuilderException, InvalidTransactionException,
            FogReportException, TimeoutException {
        try {
            defragmentAccount(
                    new Amount(amountPicoMOB, TokenId.MOB),
                    delegate,
                    false
            );
        } catch(FogSyncException e) {
            throw new NetworkException(NetworkResult.INTERNAL, e);
        }
    }

    @Override
    public void defragmentAccount(
            @NonNull final Amount amountToSend,
            @NonNull final DefragmentationDelegate delegate,
            final boolean shouldWriteRTHMemos
    ) throws InvalidFogResponse, AttestationException, NetworkException, InsufficientFundsException,
            TransactionBuilderException, InvalidTransactionException,
            FogReportException, TimeoutException, FogSyncException {
        this.defragmentAccount(
                amountToSend,
                delegate,
                shouldWriteRTHMemos,
                ChaCha20Rng.withRandomSeed()
        );
    }

    @Override
    public void defragmentAccount(
        @NonNull final Amount amountToSend,
        @NonNull final DefragmentationDelegate delegate,
        final boolean shouldWriteRTHMemos,
        @NonNull final ChaCha20Rng rng
    ) throws InvalidFogResponse, AttestationException, NetworkException, InsufficientFundsException,
            TransactionBuilderException, InvalidTransactionException,
            FogReportException, TimeoutException, FogSyncException {
        delegate.onStart();
        UTXOSelector.Selection<OwnedTxOut> inputSelectionForAmount = null;
        TxOutMemoBuilder txOutMemoBuilder = shouldWriteRTHMemos ? TxOutMemoBuilder
            .createSenderAndDestinationRTHMemoBuilder(accountKey)
            : TxOutMemoBuilder.createDefaultRTHMemoBuilder();
        do {
            Set<OwnedTxOut> unspent = getUnspentTxOuts(amountToSend.getTokenId());
            final Amount txFee = getOrFetchMinimumTxFee(amountToSend.getTokenId());
            try {
                inputSelectionForAmount = UTXOSelector.selectInputsForAmount(
                        unspent,
                        amountToSend.getValue(),
                        txFee.getValue(),
                        INPUT_FEE,
                        OUTPUT_FEE,
                        1
                );
            } catch (FragmentedAccountException exception) {
                UTXOSelector.Selection<OwnedTxOut> selection = UTXOSelector.selectInputsForMerging(
                        unspent,
                        txFee.getValue(),
                        INPUT_FEE,
                        OUTPUT_FEE
                );
                Amount totalValue = new Amount(BigInteger.ZERO, amountToSend.getTokenId());
                for (OwnedTxOut utxo : selection.txOuts) {
                    totalValue = totalValue.add(utxo.getAmount());
                }
                Amount selectionFee = new Amount(selection.fee, totalValue.getTokenId());
                PendingTransaction pendingTransaction = prepareTransaction(
                        accountKey.getPublicAddress(),
                        totalValue.subtract(selectionFee),
                        selection.txOuts,
                        selectionFee,
                        txOutMemoBuilder,
                        rng
                );
                if (!delegate.onStepReady(pendingTransaction, selection.fee)) {
                    delegate.onCancel();
                    return;
                }
                // make sure the previous Tx is posted
                Transaction.Status status;
                int queryTries = 0;
                while ((status = getTransactionStatus(pendingTransaction.getTransaction()))
                        == Transaction.Status.UNKNOWN) {
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
                if (status == Transaction.Status.FAILED) {
                    //Status only set to FAILED on TombstoneBlockExceeded. See getTransactionStatus(Transaction transaction)
                    throw new InvalidTransactionException(
                            ConsensusCommon.ProposeTxResult.TombstoneBlockExceeded,
                            status.getBlockIndex());
                }
            }
        } while (inputSelectionForAmount == null);
        delegate.onComplete();
    }

    @Deprecated
    @Override
    public boolean requiresDefragmentation(@NonNull BigInteger amountPicoMOB)
            throws NetworkException, InvalidFogResponse, AttestationException,
            InsufficientFundsException {
        return requiresDefragmentation(new Amount(
                amountPicoMOB,
                TokenId.MOB
        ));
    }

    @Override
    public boolean requiresDefragmentation(@NonNull Amount amountToSend)
            throws NetworkException, InvalidFogResponse, AttestationException,
            InsufficientFundsException {
        try {
            UTXOSelector.selectInputsForAmount(
                    getUnspentTxOuts(amountToSend.getTokenId()),
                    amountToSend.getValue(),
                    getOrFetchMinimumTxFee(amountToSend.getTokenId()).getValue(),
                    INPUT_FEE,
                    OUTPUT_FEE, 1);
        } catch (FragmentedAccountException | FogSyncException exception) {
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
    Set<OwnedTxOut> getAllUnspentTxOuts() throws InvalidFogResponse, NetworkException,
            AttestationException, FogSyncException {
        getTxOutStore().refresh(
                viewClient,
                ledgerClient,
                fogBlockClient
        );
        return getTxOutStore().getUnspentTxOuts();
    }

    @NonNull
    Set<OwnedTxOut> getUnspentTxOuts(@NonNull TokenId tokenId) throws InvalidFogResponse,
            NetworkException, AttestationException, FogSyncException {
        return getAllUnspentTxOuts().stream()
                .filter(otxo -> tokenId.equals(otxo.getAmount().getTokenId()))
                .collect(Collectors.toSet());
    }

    @Deprecated
    @Override
    @NonNull
    public BigInteger getOrFetchMinimumTxFee() throws NetworkException {
        return getOrFetchMinimumTxFee(TokenId.MOB).getValue();
    }

    @Override
    @NonNull
    public Amount getOrFetchMinimumTxFee(@NonNull TokenId tokenId) throws NetworkException {
        return blockchainClient.getOrFetchMinimumFee(tokenId);
    }

    @Override
    @NonNull
    public AccountActivity getAccountActivity() throws NetworkException, InvalidFogResponse,
            AttestationException, FogSyncException {
        txOutStore.refresh(viewClient, ledgerClient, fogBlockClient);
        Set<OwnedTxOut> txOuts = txOutStore.getSyncedTxOuts()
                .stream().map(OwnedTxOut::new).collect(Collectors.toSet());
        return new AccountActivity(txOuts,
                getTxOutStore().getCurrentBlockIndex().add(UnsignedLong.ONE));
    }

    @NonNull
    List<Ring> getRingsForUTXOs(
            @NonNull List<OwnedTxOut> utxos,
            @NonNull UnsignedLong numTxOutsInLedger,
            @NonNull Rng rng
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
        while (indices.size() != count) {
            UnsignedLong index = UnsignedLong.valueOf(Math.abs(rng.nextLong()))
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
            short realIndex = (short)(Math.abs(rng.nextInt()) % DEFAULT_RING_SIZE);
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

    @Override
    @NonNull
    public final AccountKey getAccountKey() {
        return accountKey;
    }

    @Override
    public void setFogBasicAuthorization(
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
        fogBlockClient.setAuthorization(
                username,
                password
        );
        getUntrustedClient().setAuthorization(
                username,
                password
        );
    }

    @Override
    public void setConsensusBasicAuthorization(@NonNull String username, @NonNull String password) {
        consensusClient.setAuthorization(
                username,
                password
        );
        blockchainClient.setAuthorization(
                username,
                password
        );
    }

    @Override
    public void setTransportProtocol(@NonNull TransportProtocol protocol) {
        viewClient.setTransportProtocol(protocol);
        ledgerClient.setTransportProtocol(protocol);
        consensusClient.setTransportProtocol(protocol);
        blockchainClient.setTransportProtocol(protocol);
        fogBlockClient.setTransportProtocol(protocol);
        getUntrustedClient().setTransportProtocol(protocol);
        fogReportsManager.setTransportProtocol(protocol);
    }

    @Override
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
        if (null != fogBlockClient) {
            fogBlockClient.shutdown();
        }
        if (null != blockchainClient) {
            blockchainClient.shutdown();
        }
        getUntrustedClient().shutdown();
    }

    @Override
    protected void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }
}
