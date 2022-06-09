// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.uri.FogUri;
import com.mobilecoin.lib.util.Hex;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents account's public address to receive coins
 */
public final class PublicAddress extends Native implements Parcelable, AddressHashProvider {
    private static final String TAG = PublicAddress.class.getName();

    @NonNull
    private final RistrettoPublic viewKey;
    @NonNull
    private final RistrettoPublic spendKey;
    @Nullable
    private final Uri fogReportUri;
    @Nullable
    private final String fogReportId;
    @Nullable
    private final byte[] fogAuthoritySig;

    @Nullable
    private AddressHash addressHash;

    /**
     * Constructs new {@link PublicAddress} instance
     *
     * @param viewKey      view public key
     * @param spendKey     spend public key
     * @param fogReportUri user's fog service uri
     */
    public PublicAddress(
            @NonNull RistrettoPublic viewKey,
            @NonNull RistrettoPublic spendKey,
            @NonNull Uri fogReportUri,
            @NonNull byte[] fogAuthoritySig,
            @NonNull String fogReportId
    ) {
        this.viewKey = viewKey;
        this.spendKey = spendKey;
        this.fogReportUri = fogReportUri;
        this.fogAuthoritySig = fogAuthoritySig;
        this.fogReportId = fogReportId;
        try {
            new FogUri(fogReportUri);
            // throws if the URI is not valid
            init_jni_with_fog(
                    viewKey,
                    spendKey,
                    fogReportUri.toString(),
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
        fogReportUri = null;
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
            fogReportUri = Uri.parse(fogUriString);
        } else {
            fogReportUri = null;
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
        Uri fogUri = (fogUrl != null && !fogUrl.isEmpty())
                ? Uri.parse(fogUrl)
                : null;
        ByteString fogAuthoritySigString = address.getFogAuthoritySig();
        byte[] fogAuthoritySig =
                (fogAuthoritySigString != null && fogAuthoritySigString.size() > 0)
                        ? fogAuthoritySigString.toByteArray()
                        : null;
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

    /**
     * Calculates the {@link AddressHash} for the given instance.
     * @deprecated Deprecated as of 1.2.2. Please use {@link PublicAddress#getAddressHash()}.
     * */
    @Deprecated
    @NonNull
    public AddressHash calculateAddressHash() {//TODO: 1.3 make private
        if (addressHash == null) {
            byte[] addressHashData = calculate_address_hash_data();
            addressHash = AddressHash.createAddressHash(addressHashData);
        }

        return addressHash;
    }

    @NonNull
    @Override
    public AddressHash getAddressHash() {
        return this.calculateAddressHash();
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
        return fogReportUri;
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

    @Nullable
    public Uri getNormalizedFogReportUri() {
        if (fogReportUri == null) {
            return null;
        }
        try {
            return new FogUri(fogReportUri).getUri();
        } catch (InvalidUriException exception) {
            throw new IllegalStateException("Bug: URI was already verified");
        }
    }

    public boolean equivalent(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicAddress that = (PublicAddress) o;
        return viewKey.equals(that.viewKey) &&
                spendKey.equals(that.spendKey) &&
                Arrays.equals(fogAuthoritySig, that.fogAuthoritySig) &&
                Objects.equals(getNormalizedFogReportUri(), that.getNormalizedFogReportUri()) &&
                Objects.equals(fogReportId, that.fogReportId) &&
                Objects.equals(addressHash, that.addressHash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicAddress that = (PublicAddress) o;
        return viewKey.equals(that.viewKey) &&
                spendKey.equals(that.spendKey) &&
                Arrays.equals(fogAuthoritySig, that.fogAuthoritySig) &&
                Objects.equals(fogReportUri, that.fogReportUri) &&
                Objects.equals(fogReportId, that.fogReportId) &&
                Objects.equals(addressHash, that.addressHash);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(fogReportId, viewKey, spendKey, fogReportUri, addressHash);
        result = 31 * result + Arrays.hashCode(fogAuthoritySig);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        String fogAuthoritySignString = (fogAuthoritySig == null)
                ? "(null)"
                : Hex.toString(fogAuthoritySig);
        return "PublicAddress{" +
                "viewKey=" + viewKey +
                ", spendKey=" + spendKey +
                ", fogReportUri=" + fogReportUri +
                ", fogReportId=" + fogReportId +
                ", fogAuthoritySig=" + fogAuthoritySignString +
                ", addressHash=" + addressHash +
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

    /**
     * @return The flags needed to write and read this object to or from a parcel
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes this object to the provided parcel
     * @param parcel The parcel to write the object to
     * @param flags The flags describing the contents of this object
     */
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(viewKey, flags);
        parcel.writeParcelable(spendKey, flags);
        parcel.writeParcelable(fogReportUri, flags);
        parcel.writeString(fogReportId);
        parcel.writeByteArray(fogAuthoritySig);
    }

    /**
     * Creates a PublicAddress from the provided parcel
     * @param parcel The parcel that contains a PublicAddress
     */
    private PublicAddress(Parcel parcel) {
        viewKey = parcel.readParcelable(RistrettoPublic.class.getClassLoader());
        spendKey = parcel.readParcelable(RistrettoPublic.class.getClassLoader());
        fogReportUri = parcel.readParcelable(Uri.class.getClassLoader());
        fogReportId = parcel.readString();
        fogAuthoritySig = parcel.createByteArray();
    }

    public static final Creator<PublicAddress> CREATOR = new Creator<PublicAddress>() {
        /**
         * Create PublicAddress from the provided Parcel
         * @param parcel The parcel containing a PublicAddress
         * @return The PublicAddress contained in the provided Parcel
         */
        @Override
        public PublicAddress createFromParcel(Parcel parcel) {
            return new PublicAddress(parcel);
        }

        /**
         * Used by Creator to deserialize an array of PublicAddresses
         */
        @Override
        public PublicAddress[] newArray(int length) {
            return new PublicAddress[length];
        }
    };

    private native byte[] calculate_address_hash_data();

}
