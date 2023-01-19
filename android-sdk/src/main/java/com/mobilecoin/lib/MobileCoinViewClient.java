package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FogSyncException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.LogAdapter;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.uri.ConsensusUri;
import com.mobilecoin.lib.network.uri.FogUri;
import com.mobilecoin.lib.network.uri.MobileCoinUri;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MobileCoinViewClient {

    static final BigInteger INPUT_FEE = BigInteger.valueOf(0L);
    static final BigInteger OUTPUT_FEE = BigInteger.valueOf(0L);
    private static final String TAG = MobileCoinClient.class.toString();
    private static final int STATUS_CHECK_DELAY_MS = 1000;
    private static final int STATUS_MAX_RETRIES = 300;
    private static final int DEFAULT_RING_SIZE = 11;
    private static final long DEFAULT_NEW_TX_BLOCK_ATTEMPTS = 50;
    private final ViewAccountKey accountKey;
    private final ViewableTxOutStore txOutStore;
    private final ClientConfig clientConfig;
    private final StorageAdapter cacheStorage;
    final FogReportsManager fogReportsManager;
    final FogBlockClient fogBlockClient;
    final FogUntrustedClient untrustedClient;
    final AttestedViewClient viewClient;
    final AttestedLedgerClient ledgerClient;
    final AttestedConsensusClient consensusClient;
    final BlockchainClient blockchainClient;

    public MobileCoinViewClient(
            @NonNull final ViewAccountKey accountKey,
            @NonNull final Uri fogUri,
            @NonNull final Uri consensusUri,
            @NonNull final TransportProtocol transportProtocol
    ) throws InvalidUriException {
        this.accountKey = accountKey;
        this.clientConfig = ClientConfig.defaultConfig();
        this.cacheStorage = clientConfig.storageAdapter;
        FogUri normalizedFogUri = new FogUri(fogUri);
        List<MobileCoinUri> normalizedConsensusUris = createNormalizedConsensusUris(Collections.singletonList(consensusUri));
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

    private ViewableTxOutStore createTxOutStore(@NonNull final ViewAccountKey accountKey) {
        return new ViewableTxOutStore(accountKey);
    }

    private List<MobileCoinUri> createNormalizedConsensusUris(@NonNull final List<Uri> consensusUris)
            throws InvalidUriException {
        List<MobileCoinUri> normalizedConsensusUris = new ArrayList<>();
        for (Uri consensusUri : consensusUris) {
            normalizedConsensusUris.add(new ConsensusUri(consensusUri));
        }

        return normalizedConsensusUris;
    }

    @NonNull
    public Balance getBalance(TokenId tokenId) throws AttestationException, InvalidFogResponse, NetworkException, FogSyncException {
        Logger.i(TAG, "GetBalance call");
        txOutStore.refresh(viewClient, fogBlockClient);
        return getBalances().get(tokenId);
    }

    @NonNull
    public Map<TokenId, Balance> getBalances() throws AttestationException, InvalidFogResponse, NetworkException, FogSyncException {
        txOutStore.refresh(viewClient, fogBlockClient);
        HashMap<TokenId, Balance> balances = new HashMap<TokenId, Balance>();
        final UnsignedLong blockIndex = txOutStore.getCurrentBlockIndex();
        for(ViewableTxOut otxo : txOutStore.getUnspentTxOuts()) {
            Balance balance = balances.get(otxo.getAmount().getTokenId());
            if(null == balance) {
                balance = new Balance(BigInteger.ZERO, blockIndex);
            }
            balance = new Balance(
                    otxo.getAmount().getValue().add(balance.getValue()),
                    blockIndex
            );
            balances.put(otxo.getAmount().getTokenId(), balance);
        }
        return balances;
    }

}
