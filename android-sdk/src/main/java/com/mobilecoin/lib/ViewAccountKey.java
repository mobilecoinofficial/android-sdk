package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.uri.FogUri;

public final class ViewAccountKey extends Native {
    private static final String TAG = ViewAccountKey.class.getName();


    private final RistrettoPrivate defaultSubaddressViewKey;
    private final RistrettoPrivate viewPrivateKey;
    private final RistrettoPublic spendPublicKey;
    private final Uri fogReportUri;
    private final String fogReportId;
    private final byte[] fogAuthoritySpki;

    ViewAccountKey(@NonNull final AccountKey spendAccountKey) throws InvalidUriException {
        this(
                spendAccountKey.getViewKey(),
                spendAccountKey.getDefaultSubAddressViewKey(),
                spendAccountKey.getSpendKey().getPublicKey(),
                spendAccountKey.getFogReportUri(),
                spendAccountKey.getFogAuthoritySpki(),
                spendAccountKey.getFogReportId()
        );
    }
    ViewAccountKey(
            @NonNull final RistrettoPrivate viewPrivateKey,
            @NonNull final RistrettoPrivate defaultSubaddressViewKey,
            @NonNull final RistrettoPublic spendPublicKey,
            @NonNull final Uri fogReportUri,
            @NonNull final byte[] fogAuthoritySpki,
            @NonNull final String fogRepordId
    ) throws InvalidUriException {
        new FogUri(fogReportUri);
        try {
            init_jni(
                    viewPrivateKey,
                    defaultSubaddressViewKey,
                    spendPublicKey,
                    fogReportUri.toString(),
                    fogAuthoritySpki,
                    fogRepordId
            );
            this.viewPrivateKey = viewPrivateKey;
            this.defaultSubaddressViewKey = defaultSubaddressViewKey;
            this.spendPublicKey = spendPublicKey;
            this.fogReportUri = fogReportUri;
            this.fogReportId = fogRepordId;
            this.fogAuthoritySpki = fogAuthoritySpki;
        } catch(Exception e) {
            IllegalArgumentException illegalArgumentException =
                    new IllegalArgumentException("Failed to create an ViewAccountKey", e);
            Util.logException(TAG, illegalArgumentException);
            throw illegalArgumentException;
        }
    }

    @NonNull
    RistrettoPrivate getDefaultSubaddressViewKey() {
        return defaultSubaddressViewKey;
    }

    @NonNull
    RistrettoPrivate getViewPrivateKey() {
        return viewPrivateKey;
    }

    @NonNull
    RistrettoPublic getSpendPublicKey() {
        return spendPublicKey;
    }

    @NonNull
    public Uri getFogReportUri() {
        return fogReportUri;
    }

    @NonNull
    public String getFogReportId() {
        return fogReportId;
    }

    @NonNull
    public byte[] getFogAuthoritySpki() {
        return fogAuthoritySpki;
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    private native void init_jni(
            @NonNull final RistrettoPrivate viewPrivateKey,
            @NonNull final RistrettoPrivate defaultSubaddressViewKey,
            @NonNull final RistrettoPublic spendPublicKey,
            @NonNull final String fogReportUri,
            @NonNull final byte[] fogAuthoritySpki,
            @NonNull final String fogRepordId
    );

    private native void finalize_jni();

}
