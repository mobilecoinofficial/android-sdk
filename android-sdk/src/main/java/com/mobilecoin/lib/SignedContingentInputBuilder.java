package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.SignedContingentInputBuilderException;

import java.math.BigInteger;
import java.util.List;

class SignedContingentInputBuilder extends Native {

    @NonNull private final ChaCha20Rng rng;

    SignedContingentInputBuilder(
            @NonNull final FogResolver fogResolver,
            @NonNull final TxOutMemoBuilder txOutMemoBuilder,
            final int blockVersion,
            @NonNull final TxOut ring[],
            @NonNull final TxOutMembershipProof membershipProofs[],
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
        } catch (Exception e) {
            throw new SignedContingentInputBuilderException("Failed to create SignedContingentInputBuilder", e);
        }
        this.rng = ChaCha20Rng.fromSeed(rng.nextBytes(ChaCha20Rng.SEED_SIZE_BYTES));
    }

    SignedContingentInputBuilder(
            @NonNull final FogResolver fogResolver,
            @NonNull final TxOutMemoBuilder txOutMemoBuilder,
            final int blockVersion,
            @NonNull final TxOut ring[],
            @NonNull final TxOutMembershipProof membershipProofs[],
            final short realIndex,
            @NonNull final RistrettoPrivate onetimePrivateKey,
            @NonNull final RistrettoPrivate viewPrivateKey
    ) throws SignedContingentInputBuilderException {
        this(
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
            throw new SignedContingentInputBuilderException("Failed to add required output", e);
        }
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
            throw new SignedContingentInputBuilderException("Failed to build SignedContingentInput", e);
        }
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
            @NonNull TxOut ring[],
            @NonNull TxOutMembershipProof membershipProofs[],
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

}
