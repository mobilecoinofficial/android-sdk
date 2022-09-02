package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import java.util.List;

public class SignedContingentInputBuilder extends Native {

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

}
