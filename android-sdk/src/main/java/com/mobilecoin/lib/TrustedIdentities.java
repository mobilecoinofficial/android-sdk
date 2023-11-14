package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.log.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TrustedIdentities extends Native {

    public TrustedIdentities() throws AttestationException {
        try {
            init_jni();
        } catch(Exception e) {
            Logger.wtf(TAG, "Failed to create TrustedIdentities", e);
            throw new AttestationException(e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public TrustedIdentities addMrSignerIdentity(
            final byte[] mrSigner,
            final short productId,
            final short securityVersion,
            @Nullable String[] configAdvisories,
            @Nullable String[] hardeningAdvisories
    ) throws AttestationException {
        if(null == configAdvisories) configAdvisories = new String[0];
        if(null == hardeningAdvisories) hardeningAdvisories = new String[0];

        try {
            add_mr_signer_identity(
                    mrSigner,
                    productId,
                    securityVersion,
                    configAdvisories,
                    hardeningAdvisories
            );
        } catch(Exception e) {
            Logger.e(TAG, "Failed to add trusted mr signer identity", e);
            throw new AttestationException(e.getLocalizedMessage(), e);
        }

        return this;
    }

    @NonNull
    public TrustedIdentities addMrEnclaveIdentity(
            final byte[] mrEnclave,
            @Nullable String[] configAdvisories,
            @Nullable String[] hardeningAdvisories
    ) throws AttestationException{
        if(null == configAdvisories) configAdvisories = new String[0];
        if(null == hardeningAdvisories) hardeningAdvisories = new String[0];

        try {
            add_mr_enclave_identity(
                    mrEnclave,
                    configAdvisories,
                    hardeningAdvisories
            );
        } catch(Exception e) {
            Logger.e(TAG, "Failed to add trusted mr enclave identity", e);
            throw new AttestationException(e.getLocalizedMessage(), e);
        }

        return this;
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    private native void init_jni();

    private native void add_mr_signer_identity(
            final byte[] mr_signer,
            final short product_id,
            final short security_version,
            @NonNull final String[] config_advisories,
            @NonNull final String[] hardening_advisories
    );

    private native void add_mr_enclave_identity(
            final byte[] mr_enclave,
            @NonNull final String[] config_advisories,
            @NonNull final String[] hardening_advisories
    );

    private native void finalize_jni();

    private static final String TAG = TrustedIdentities.class.getName();

}
