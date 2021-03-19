// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.util.Hex;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents account's public address to receive coins
 */
public class PublicAddress extends Native {
    private static final String TAG = PublicAddress.class.getName();
    private final RistrettoPublic viewKey;
    private final RistrettoPublic spendKey;
    private final Uri fogUri;
    private final String fogReportId;
    private final byte[] fogAuthoritySig;
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
            init_jni_with_fog(
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

    /**
     * Constructs new {@link PublicAddress} instance
     *
     * @param viewKey  view public key
     * @param spendKey spend public key
     * @throws IllegalArgumentException if provided parameters are invalid
     */
    public PublicAddress(
            @NonNull RistrettoPublic viewKey,
            @NonNull RistrettoPublic spendKey
    ) {
        Logger.i(TAG, "Creating Public Address from view/spend public keys");
        this.viewKey = viewKey;
        this.spendKey = spendKey;
        fogUri = null;
        fogReportId = null;
        fogAuthoritySig = null;
        try {
            init_jni(
                    viewKey,
                    spendKey
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
        viewKey = RistrettoPublic.fromJNI(get_view_key());
        spendKey = RistrettoPublic.fromJNI(get_spend_key());
        String fogUriString = get_fog_uri();
        if (fogUriString != null) {
            fogUri = Uri.parse(fogUriString);
        } else {
            fogUri = null;
        }
        String fogReportId = get_report_id();
        this.fogReportId = (fogReportId == null)
                ? ""
                : fogReportId;
        fogAuthoritySig = get_fog_authority_sig();
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
        Uri fogUri = (fogUrl != null)
                ? Uri.parse(fogUrl)
                : null;
        byte[] fogAuthoritySig = address.getFogAuthoritySig().toByteArray();
        String fogReportId = address.getFogReportId();
        // check non-null requirement for optional fields
        boolean hasFog = (fogUri != null && fogAuthoritySig != null && fogReportId != null);
        RistrettoPublic viewKey = RistrettoPublic.fromProtoBufObject(address.getViewPublicKey());
        RistrettoPublic spendKey = RistrettoPublic.fromProtoBufObject(address.getSpendPublicKey());
        if (hasFog) {
            return new PublicAddress(
                    viewKey,
                    spendKey,
                    fogUri,
                    fogAuthoritySig,
                    fogReportId
            );
        } else {
            return new PublicAddress(viewKey, spendKey);
        }
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
        if (getFogAuthoritySig() != null) {
            addressBuilder.setFogAuthoritySig(ByteString
                    .copyFrom(getFogAuthoritySig()));
        }
        if (getFogReportId() != null) {
            addressBuilder.setFogReportId(getFogReportId());
        }
        if (getFogReportUri() != null) {
            addressBuilder.setFogReportUrl(getFogReportUri().toString());
        }
        addressBuilder.setViewPublicKey(getViewKey().toProtoBufObject());
        addressBuilder.setSpendPublicKey(getSpendKey().toProtoBufObject());
        return addressBuilder.build();
    }

    /**
     * @return view public key
     */
    @NonNull
    public RistrettoPublic getViewKey() {
        return viewKey;
    }

    /**
     * @return spend public key
     */
    @NonNull
    public RistrettoPublic getSpendKey() {
        return spendKey;
    }

    /**
     * @return fog authority signature
     */
    @Nullable
    public byte[] getFogAuthoritySig() {
        return fogAuthoritySig;
    }

    /**
     * @return fog report id
     */
    @Nullable
    public String getFogReportId() {
        return fogReportId;
    }

    /**
     * @return fog report uri
     */
    @Nullable
    public Uri getFogReportUri() {
        return fogUri;
    }

    /**
     * @return true if this public address has fog info, false otherwise
     */
    public boolean hasFogInfo() {
        return (getFogReportUri() != null &&
                getFogAuthoritySig() != null &&
                getFogReportId() != null
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicAddress that = (PublicAddress) o;
        return getViewKey().equals(that.getViewKey()) &&
                getSpendKey().equals(that.getSpendKey()) &&
                Arrays.equals(getFogAuthoritySig(), that.getFogAuthoritySig()) &&
                Objects.equals(getFogReportUri(), that.getFogReportUri()) &&
                Objects.equals(getFogReportId(), that.getFogReportId());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getFogReportId(), viewKey, spendKey, getFogReportUri());
        result = 31 * result + Arrays.hashCode(getFogAuthoritySig());
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        String fogAuthoritySignString = (fogAuthoritySig == null)
                ? "(null)"
                : Hex.toString(fogAuthoritySig);
        return "PublicAddress{" +
                "viewKey=" + viewKey.toString() +
                ", spendKey=" + spendKey.toString() +
                ", fogUri=" + fogUri +
                ", fogReportId=" + fogReportId +
                ", fogAuthoritySig=" + fogAuthoritySignString +
                '}';
    }

    private native void init_jni_with_fog(
            @NonNull RistrettoPublic viewKey,
            @NonNull RistrettoPublic spendKey,
            @NonNull String fogUriString,
            @NonNull byte[] fogAuthoritySpki,
            @NonNull String fogReportId
    );

    private native void init_jni(
            @NonNull RistrettoPublic viewKey,
            @NonNull RistrettoPublic spendKey
    );

    private native long get_view_key();

    private native long get_spend_key();

    @Nullable
    private native String get_fog_uri();

    @Nullable
    private native byte[] get_fog_authority_sig();

    @Nullable
    private native String get_report_id();
}
