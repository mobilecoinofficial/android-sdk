// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.log.Logger;

/**
 * Verifier is used for attestation connection verification.
 */

public final class Verifier {

    private final TrustedIdentities trustedIdentities;

    @Deprecated
    public Verifier() throws AttestationException {
        trustedIdentities = new TrustedIdentities();
    }

    @Deprecated
    public @NonNull
    Verifier withMrSigner(@NonNull byte[] mrSigner, short productId, short securityVersion,
                          @Nullable String[] configAdvisories,
                          @Nullable String[] hardeningAdvisories) throws AttestationException {
        trustedIdentities.addMrSignerIdentity(
                mrSigner,
                productId,
                securityVersion,
                configAdvisories,
                hardeningAdvisories
        );
        return this;
    }

    @Deprecated
    public @NonNull
    Verifier withMrEnclave(@NonNull byte[] mrEnclave, @Nullable String[] configAdvisories,
                           @Nullable String[] hardeningAdvisories) throws AttestationException {
        trustedIdentities.addMrEnclaveIdentity(
                mrEnclave,
                configAdvisories,
                hardeningAdvisories
        );
        return this;
    }

    @Deprecated
    TrustedIdentities getTrustedIdentities() {
        return trustedIdentities;
    }
}
