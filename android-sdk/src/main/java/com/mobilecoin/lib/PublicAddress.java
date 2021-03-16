// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents account's public address to receive coins
 */
public class PublicAddress extends Native {
    private static final String TAG = PublicAddress.class.getName();
    private String fogReportId;
    private byte[] fogAuthoritySig;
    private RistrettoPublic viewKey;
    private RistrettoPublic spendKey;
    private Uri fogUri;

    /**
     * Constructs new {@link PublicAddress} instance
     *
     * @param viewKey  view public key
     * @param spendKey spend public key
     * @param fogUri   user's fog service uri
     * @throws IllegalArgumentException if provided parameters are invalid
     */
    public PublicAddress(
            @NonNull RistrettoPublic viewKey,
            @NonNull RistrettoPublic spendKey,
            @NonNull Uri fogUri,
            @NonNull byte[] fogAuthoritySig,
            @NonNull String fogReportId
    ) {
        Logger.i(TAG, "Creating Public Address from view/spend public keys");
        this.viewKey = viewKey;
        this.spendKey = spendKey;
        this.fogUri = fogUri;
        this.fogAuthoritySig = fogAuthoritySig;
        this.fogReportId = fogReportId;
        try {
            init_jni(
                    viewKey,
                    spendKey,
                    fogUri.toString(),
                    fogAuthoritySig,
                    fogReportId
            );
            Logger.i(TAG, "Created Public Address from view/spend public keys");
        } catch (NullPointerException ex) {
            throw ex;
        } catch (Exception ex) {
            Logger.e(TAG, "Unable to create PublicAddress instance", ex);
            throw new IllegalArgumentException(ex.getLocalizedMessage());
        }
    }

    private PublicAddress(long existingRustObj) {
        rustObj = existingRustObj;
    }

    /**
     * Construct a {@link PublicAddress} object from the serialized bytes.
     *
     * @param serializedBytes a binary representation of the {@link PublicAddress} object (see
     *                        {@link PublicAddress#toByteArray()})
     * @throws SerializationException if serialized bytes parameter is invalid
     */
    @NonNull
    public static PublicAddress fromBytes(@NonNull byte[] serializedBytes) throws SerializationException {
        try {
            MobileCoinAPI.PublicAddress protoBufObject =
                    MobileCoinAPI.PublicAddress.parseFrom(serializedBytes);
            return PublicAddress.fromProtoBufObject(protoBufObject);
        } catch (InvalidProtocolBufferException exception) {
            throw new SerializationException(
                    "Unable to construct an object from the provided data",
                    exception
            );
        }
    }

    @NonNull
    static PublicAddress fromProtoBufObject(@NonNull MobileCoinAPI.PublicAddress address)
            throws SerializationException {
        String fogUrl = address.getFogReportUrl();
        Uri fogUri = Uri.parse(fogUrl);
        RistrettoPublic viewKey = RistrettoPublic.fromProtoBufObject(address.getViewPublicKey());
        RistrettoPublic spendKey = RistrettoPublic.fromProtoBufObject(address.getSpendPublicKey());
        return new PublicAddress(viewKey,
                spendKey,
                fogUri,
                address.getFogAuthoritySig().toByteArray(),
                address.getFogReportId()
        );
    }

    /**
     * Constructs a public address from an existing rust object. This is used by the JNI
     * implementation.
     */
    @NonNull
    static PublicAddress fromJNI(long rustObj) {
        return new PublicAddress(rustObj);
    }

    /**
     * Returns a binary representation of this object
     */
    @NonNull
    public byte[] toByteArray() {
        return toProtoBufObject().toByteArray();
    }

    @NonNull
    MobileCoinAPI.PublicAddress toProtoBufObject() {
        MobileCoinAPI.PublicAddress.Builder addressBuilder =
                MobileCoinAPI.PublicAddress.newBuilder();
        addressBuilder.setFogAuthoritySig(ByteString
                .copyFrom(getFogAuthoritySig()));
        addressBuilder.setFogReportId(getFogReportId());
        addressBuilder.setFogReportUrl(getFogUri().toString());
        addressBuilder.setViewPublicKey(getViewKey().toProtoBufObject());
        addressBuilder.setSpendPublicKey(getSpendKey().toProtoBufObject());
        return addressBuilder.build();
    }

    /**
     * @return view public key
     */
    @NonNull
    public synchronized RistrettoPublic getViewKey() {
        if (null == viewKey) {
            long rustObj = get_view_key();
            viewKey = RistrettoPublic.fromJNI(rustObj);
        }
        return viewKey;
    }

    @NonNull
    public synchronized byte[] getFogAuthoritySig() {
        if (null == fogAuthoritySig) {
            fogAuthoritySig = get_fog_authority_sig();
        }
        return fogAuthoritySig;
    }

    @NonNull
    public synchronized String getFogReportId() {
        if (null == fogReportId) {
            fogReportId = get_report_id();
        }
        return fogReportId;
    }

    /**
     * @return spend public key
     */
    @NonNull
    public synchronized RistrettoPublic getSpendKey() {
        if (null == spendKey) {
            long rustObj = get_spend_key();
            spendKey = RistrettoPublic.fromJNI(rustObj);
        }
        return spendKey;
    }

    /**
     * @return user's fog service Uri
     */
    @NonNull
    public synchronized Uri getFogUri() {
        if (null == fogUri) {
            String fogUriString = get_fog_uri();
            fogUri = Uri.parse(fogUriString);
        }
        return fogUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicAddress that = (PublicAddress) o;
        return fogReportId.equals(that.fogReportId) &&
                Arrays.equals(fogAuthoritySig, that.fogAuthoritySig) &&
                viewKey.equals(that.viewKey) &&
                spendKey.equals(that.spendKey) &&
                fogUri.equals(that.fogUri);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(fogReportId, viewKey, spendKey, fogUri);
        result = 31 * result + Arrays.hashCode(fogAuthoritySig);
        return result;
    }

    private native void init_jni(
            @NonNull RistrettoPublic viewKey,
            @NonNull RistrettoPublic spendKey,
            @NonNull String fogUriString,
            @NonNull byte[] fogAuthoritySpki,
            @NonNull String fogReportId
    );

    private native long get_view_key();

    private native long get_spend_key();

    @NonNull
    private native String get_fog_uri();

    @NonNull
    private native byte[] get_fog_authority_sig();

    @NonNull
    private native String get_report_id();
}
