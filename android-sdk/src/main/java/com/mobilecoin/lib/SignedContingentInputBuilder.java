package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.FogSyncException;
import com.mobilecoin.lib.exceptions.FragmentedAccountException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.SignedContingentInputBuilderException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.uri.FogUri;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

// TODO: doc
public class SignedContingentInputBuilder extends Native {

    @NonNull private final ChaCha20Rng rng;
    @NonNull private final PublicAddress signerPublicAddress;

    SignedContingentInputBuilder(
            @NonNull PublicAddress signerPublicAddress,
            @NonNull final FogResolver fogResolver,
            @NonNull final TxOutMemoBuilder txOutMemoBuilder,
            final int blockVersion,
            @NonNull final TxOut[] ring,
            @NonNull final TxOutMembershipProof[] membershipProofs,
            final short realIndex,
            @NonNull final RistrettoPrivate onetimePrivateKey,
            @NonNull final RistrettoPrivate viewPrivateKey,
            @NonNull final Rng rng
    ) throws SignedContingentInputBuilderException {
        try {
            init_jni(
                    fogResolver,
                    txOutMemoBuilder,
                    blockVersion,
                    ring,
                    membershipProofs,
                    realIndex,
                    onetimePrivateKey,
                    viewPrivateKey
            );
            this.signerPublicAddress = signerPublicAddress;
        } catch (Exception e) {
            throw new SignedContingentInputBuilderException("Failed to create SignedContingentInputBuilder", e);
        }
        this.rng = ChaCha20Rng.fromSeed(rng.nextBytes(ChaCha20Rng.SEED_SIZE_BYTES));
    }

    SignedContingentInputBuilder(
            @NonNull PublicAddress signerPublicAddress,
            @NonNull final FogResolver fogResolver,
            @NonNull final TxOutMemoBuilder txOutMemoBuilder,
            final int blockVersion,
            @NonNull final TxOut[] ring,
            @NonNull final TxOutMembershipProof[] membershipProofs,
            final short realIndex,
            @NonNull final RistrettoPrivate onetimePrivateKey,
            @NonNull final RistrettoPrivate viewPrivateKey
    ) throws SignedContingentInputBuilderException {
        this(
                signerPublicAddress,
                fogResolver,
                txOutMemoBuilder,
                blockVersion,
                ring,
                membershipProofs,
                realIndex,
                onetimePrivateKey,
                viewPrivateKey,
                DefaultRng.createInstance()
        );
    }

    @NonNull
    TxOut addRequiredOutput(
            @NonNull final Amount amount,
            @NonNull final PublicAddress recipient
    ) throws SignedContingentInputBuilderException {
        try {
            return TxOut.fromJNI(
                    add_required_output(
                            amount.getValue(),
                            amount.getTokenId().getId().longValue(),
                            recipient,
                            rng
                    )
            );
        } catch(Exception e) {
            Logger.e(TAG, e);
            throw new SignedContingentInputBuilderException("Failed to add required output", e);
        }
    }

    // TODO: doc
    public void addRequiredAmount(
            @NonNull final Amount amount
    ) throws SignedContingentInputBuilderException {
        addRequiredOutput(amount, signerPublicAddress);
    }

    // TODO: doc
    public void addRequiredAmount(
            @NonNull final Amount amount,
            @NonNull final PublicAddress recipient
    ) throws SignedContingentInputBuilderException {
        addRequiredOutput(amount, recipient);
    }

    @NonNull
    TxOut addRequiredChangeOutput(
            @NonNull final Amount amount,
            @NonNull final AccountKey accountKey
    ) throws SignedContingentInputBuilderException {
        try {
            return TxOut.fromJNI(
                    add_required_change_output(
                            amount.getValue(),
                            amount.getTokenId().getId().longValue(),
                            accountKey,
                            rng
                    )
            );
        } catch(Exception e) {
            Logger.e(TAG, e);
            throw new SignedContingentInputBuilderException("Failed to add required change output", e);
        }
    }

    void setTombstoneBlockIndex(@NonNull final UnsignedLong index) {
        set_tombstone_block(index.longValue());
    }

    @NonNull
    SignedContingentInput build() throws SignedContingentInputBuilderException {
        try {
            return SignedContingentInput.fromJNI(build_sci(rng));
        } catch(Exception e) {
            Logger.e(TAG, e);
            throw new SignedContingentInputBuilderException("Failed to build SignedContingentInput", e);
        }
    }

    // TODO: doc
    @NonNull
    public static SignedContingentInputBuilder newBuilder(
            @NonNull final Amount amountToSpend,
            @NonNull final MobileCoinClient client
    ) throws AttestationException, FogSyncException, InvalidFogResponse, NetworkException,
            InsufficientFundsException, FogReportException, FragmentedAccountException,
            SignedContingentInputBuilderException, TransactionBuilderException {
        return newBuilder(amountToSpend, client, client.getAccountSnapshot());
    }

    // TODO: doc
    @NonNull
    public static SignedContingentInputBuilder newBuilder(
            @NonNull final Amount amountToSpend,
            @NonNull final MobileCoinClient client,
            @NonNull final AccountSnapshot snapshot
    ) throws InsufficientFundsException, NetworkException, FogReportException, FragmentedAccountException,
            AttestationException, InvalidFogResponse, TransactionBuilderException,
            SignedContingentInputBuilderException {
        final int blockVersion = client.blockchainClient.getOrFetchNetworkBlockVersion();
        //if(blockVersion < 3) throw new UnsupportedOperationException("Unsupported until block version 3");// TODO: HERE!
        final TokenId tokenId = amountToSpend.getTokenId();
        final Balance availableBalance = snapshot.getBalance(tokenId);
        if(availableBalance.getValue().compareTo(amountToSpend.getValue()) < 0) {
            throw new InsufficientFundsException();
        }

        UnsignedLong blockIndex = snapshot.getBlockIndex();
        UnsignedLong tombstoneBlockIndex = blockIndex.add(UnsignedLong.fromLongBits(50L));
        HashSet<FogUri> reportUris = new HashSet<>();
        try {
            reportUris.add(new FogUri(client.getAccountKey().getFogReportUri()));
        } catch(InvalidUriException e) {
            FogReportException reportException = new FogReportException("Invalid Fog Report " +
                    "Uri in the public address");
            throw (reportException);
        }

        /*
        Put all OwnedTxOuts into a TreeSet so they will be sorted in ascending amount order
        We have to be a little careful here. The resulting set will not contain two different unspent
        OwnedTxOuts if they have the same Amount. That is because sorted sets will use compareTo
        to determine equality of elements. Since we can only select a single TxOut to spend, it doesn't
        matter here, allowing us to make this helpful simplification.
         */
        Set<OwnedTxOutTreeSetNode> unspent = snapshot.getAccountActivity().getAllTokenTxOuts(tokenId).stream()
                .filter(otxo -> tokenId.equals(otxo.getAmount().getTokenId()))
                .map(OwnedTxOutTreeSetNode::new)
                .collect(Collectors.toCollection(TreeSet::new));
        OwnedTxOut txOutToSpend = null;
        for(OwnedTxOutTreeSetNode otxoNode : unspent) {
            if(amountToSpend.compareTo(otxoNode.getAmount()) <= 0) {
                // Find first TxOut at least as big as we want to spend
                txOutToSpend = otxoNode.otxo;
                break;
            }
        }
        if(null == txOutToSpend) {
            throw new FragmentedAccountException("No single TxOut big enough to satisfy input conditions. Defragmentation required");
        }
        final List<OwnedTxOut> txos = new ArrayList<>();
        txos.add(txOutToSpend);
        final Ring ring = client.getRingsForUTXOs(
                txos,
                client.getTxOutStore().getLedgerTotalTxCount(),
                DefaultRng.createInstance()
        ).get(0);
        FogReportResponses reportsResponse = client.fogReportsManager.fetchReports(reportUris,
                tombstoneBlockIndex, client.clientConfig.report);
        RistrettoPrivate onetimePrivateKey = Util.recoverOnetimePrivateKey(
                txOutToSpend.getPublicKey(),
                txOutToSpend.getTargetKey(),
                client.getAccountKey()
        );
        SignedContingentInputBuilder sciBuilder = new SignedContingentInputBuilder(
                client.getAccountKey().getPublicAddress(),
                new FogResolver(reportsResponse, client.clientConfig.report.getVerifier()),
                TxOutMemoBuilder.createDefaultRTHMemoBuilder(),
                //blockVersion,
                3,//TODO: this!
                ring.getNativeTxOuts().toArray(new TxOut[0]),
                ring.getNativeTxOutMembershipProofs().toArray(new TxOutMembershipProof[0]),
                ring.realIndex,
                onetimePrivateKey,
                client.getAccountKey().getViewKey()
        );

        final Amount changeAmount = txOutToSpend.getAmount().subtract(amountToSpend);
        sciBuilder.addRequiredChangeOutput(
                changeAmount,
                client.getAccountKey()
        );

        return sciBuilder;
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    private native void init_jni(
            @NonNull FogResolver fog_resolver,
            @NonNull TxOutMemoBuilder txOutMemoBuilder,
            int blockVersion,
            @NonNull TxOut[] ring,
            @NonNull TxOutMembershipProof[] membershipProofs,
            short realIndex,
            @NonNull RistrettoPrivate onetimePrivateKey,
            @NonNull RistrettoPrivate viewPrivateKey
    );

    private native long add_required_output(
            @NonNull BigInteger value,
            long tokenId,
            @NonNull PublicAddress recipient,
            @NonNull ChaCha20Rng rng
    );

    private native long add_required_change_output(
            @NonNull BigInteger value,
            long tokenId,
            @NonNull AccountKey accountKey,
            @NonNull ChaCha20Rng rng
    );

    private native void set_tombstone_block(long value);

    private native long build_sci(@NonNull ChaCha20Rng rng);

    private native void finalize_jni();

    private static final String TAG = SignedContingentInputBuilder.class.getName();

    // This node class is used to give TreeSet a way to sort OwnedTxOuts by Amount
    private static class OwnedTxOutTreeSetNode implements Comparable<OwnedTxOutTreeSetNode> {
        final OwnedTxOut otxo;
        OwnedTxOutTreeSetNode(@NonNull OwnedTxOut otxo) {
            this.otxo = otxo;
        }
        @NonNull
        Amount getAmount() {
            return otxo.getAmount();
        }
        @Override
        public int compareTo(OwnedTxOutTreeSetNode other) {
            return this.otxo.getAmount().compareTo(other.otxo.getAmount());
        }
    }

}
