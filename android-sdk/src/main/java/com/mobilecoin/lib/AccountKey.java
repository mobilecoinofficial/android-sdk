// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.BadBip39SeedException;
import com.mobilecoin.lib.exceptions.BadMnemonicException;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.uri.FogUri;
import com.mobilecoin.lib.util.Hex;

import java.util.Arrays;
import java.util.Objects;

/**
 * The {@code AccountKey} class represents an abstraction of view & spent private keys
 */
public class AccountKey extends Native {
    private final static String TAG = AccountKey.class.getName();
    private static final int MOBILECOIN_COIN_TYPE = 866;

    private final Uri fogReportUri;
    private final String fogReportId;
    private final byte[] fogAuthoritySpki;
    private final RistrettoPrivate subAddressViewKey;
    private final RistrettoPrivate subAddressSpendKey;
    private final RistrettoPrivate viewKey;
    private final RistrettoPrivate spendKey;
    private final PublicAddress publicAddress;

    /**
     * Package private AccountKey constructor for Ristretto private keys with fog info
     *
     * @param viewKey          private view key
     * @param spendKey         private spend key
     * @param fogReportUri     URL of the fog report server (i.e. fog://fog-ingest.test
     *                         .mobilecoin.com)
     * @param fogAuthoritySpki fog authority public key a byte array, provided by the fog operator
     * @param fogReportId      A Fog report server is permitted to hold and serve several
     *                         different reports with different id’s, this field specifies which
     *                         one should be used for a given user.
     */
    AccountKey(
            @NonNull RistrettoPrivate viewKey,
            @NonNull RistrettoPrivate spendKey,
            @NonNull Uri fogReportUri,
            @NonNull String fogReportId,
            @NonNull byte[] fogAuthoritySpki
    ) throws InvalidUriException {
        Logger.i(TAG, "Create a new AccountKey from view/spend keys",
                null,
                "fogReportUri:", fogReportUri,
                "fogReportId:", fogReportId,
                "fogAuthoritySpki: ", Hex.toString(fogAuthoritySpki));

        FogUri fogUriWrapper = new FogUri(fogReportUri);
        try {
            init_jni(viewKey,
                    spendKey,
                    fogUriWrapper.toString(),
                    fogAuthoritySpki,
                    fogReportId
            );
            // normalize fog report Uri
            this.fogReportUri = new FogUri(fogReportUri).getUri();
            this.fogReportId = fogReportId;
            this.fogAuthoritySpki = fogAuthoritySpki;
            this.viewKey = viewKey;
            this.spendKey = spendKey;
            this.subAddressViewKey = RistrettoPrivate.fromJNI(get_default_subaddress_view_key());
            this.subAddressSpendKey = RistrettoPrivate.fromJNI(get_default_subaddress_spend_key());
            this.publicAddress = PublicAddress.fromJNI(get_public_address());
            Logger.i(TAG, "AccountKey created from view/spend keys");
        } catch (Exception ex) {
            IllegalArgumentException illegalArgumentException =
                    new IllegalArgumentException("Failed to create an AccountKey", ex);
            Util.logException(TAG, illegalArgumentException);
            throw illegalArgumentException;
        }
    }

    /**
     * Constructs an AccountKey object from root entropy and fog info
     *
     * @param rootEntropy      32 bytes of randomness. It is used to derive account keys
     *                         therefore it must be treated as highly sensitive
     *                         information.
     * @param fogReportUri     URL of the fog report server including port (i.e.
     *                         fog://fog-report.test.mobilecoin.com)
     * @param fogAuthoritySpki fog authority public key a byte array, provided by the fog operator
     * @param fogReportId      The fog report server may serve multiple reports, this id
     *                         disambiguates which one to use when sending to this
     *                         account
     * @throws IllegalArgumentException if the fogReportUri is invalid
     */
    private AccountKey(
            @NonNull byte[] rootEntropy,
            @NonNull Uri fogReportUri,
            @NonNull String fogReportId,
            @NonNull byte[] fogAuthoritySpki
    ) {
        Logger.i(TAG, "Create a new AccountKey from rootEntropy",
                null,
                "fogReportUri:", fogReportUri,
                "fogReportId:", fogReportId,
                "fogAuthoritySpki: ", Hex.toString(fogAuthoritySpki));
        try {
            init_jni_from_root_entropy(rootEntropy,
                    fogReportUri.toString(),
                    fogAuthoritySpki,
                    fogReportId
            );
            this.fogReportUri = fogReportUri;
            this.fogReportId = fogReportId;
            this.fogAuthoritySpki = fogAuthoritySpki;
            this.viewKey = RistrettoPrivate.fromJNI(get_view_key());
            this.spendKey = RistrettoPrivate.fromJNI(get_spend_key());
            this.subAddressViewKey = RistrettoPrivate.fromJNI(get_default_subaddress_view_key());
            this.subAddressSpendKey = RistrettoPrivate.fromJNI(get_default_subaddress_spend_key());
            this.publicAddress = PublicAddress.fromJNI(get_public_address());
            Logger.i(TAG, "AccountKey created from the root entropy");
        } catch (Exception ex) {
            IllegalArgumentException illegalArgumentException =
                    new IllegalArgumentException("Failed to create an AccountKey", ex);
            Util.logException(TAG, illegalArgumentException);
            throw illegalArgumentException;
        }
    }

    /**
     * AccountKey static constructor for root entropy with fog info
     *
     * @param rootEntropy      32 bytes of randomness. It is used to derive account keys
     *                         therefore it must be treated as highly sensitive
     *                         information.
     * @param fogReportUri     URL of the fog report server including port (i.e.
     *                         fog://fog-ingest.test.mobilecoin.com:443)
     * @param fogAuthoritySpki fog authority public key a byte array, provided by the fog operator
     * @param fogReportId      The fog report server may serve multiple reports, this id
     *                         disambiguates which one to use when sending to this
     *                         account
     * @throws IllegalArgumentException if the fogReportUri is invalid
     */
    @NonNull
    public static AccountKey fromRootEntropy(
            @NonNull byte[] rootEntropy,
            @NonNull Uri fogReportUri,
            @NonNull String fogReportId,
            @NonNull byte[] fogAuthoritySpki
    ) {
        return new AccountKey(
                rootEntropy,
                fogReportUri,
                fogReportId,
                fogAuthoritySpki
        );
    }

    /**
     * Constructs an AccountKey object from protobuf
     *
     * @param accountKey 32 bytes of randomness. It is used to derive account keys therefore it must
     *                   be treated as highly sensitive information.
     * @throws IllegalArgumentException if the fogReportUri is invalid
     */

    @NonNull
    static AccountKey fromProtoBufObject(@NonNull MobileCoinAPI.AccountKey accountKey)
            throws SerializationException {
        Logger.i(TAG, "Reading AccountKey from the protoBuf object");
        String fogReportUrl = accountKey.getFogReportUrl();
        byte[] fogAuthoritySpki = accountKey.getFogAuthoritySpki().toByteArray();
        String fogReportId = accountKey.getFogReportId();

        RistrettoPrivate viewKey =
                RistrettoPrivate.fromProtoBufObject(accountKey.getViewPrivateKey());
        RistrettoPrivate spendKey =
                RistrettoPrivate.fromProtoBufObject(accountKey.getSpendPrivateKey());
        try {
            FogUri fogUri = new FogUri(fogReportUrl);
            return new AccountKey(
                    viewKey,
                    spendKey,
                    fogUri.getUri(),
                    fogReportId,
                    fogAuthoritySpki
            );
        } catch (InvalidUriException exception) {
            throw new SerializationException("Invalid serialized uri", exception);
        }
    }

    /**
     * Constructs an AccountKey object from auto-generated account keys and fog info
     *
     * @param fogReportUri     fog report service url.
     * @param fogAuthoritySpki fog authority public key a byte array, provided by the fog operator
     * @param fogReportId      The fog report server may serve multiple reports, this id
     *                         disambiguates which one to use when sending to this
     *                         account
     * @throws IllegalArgumentException if method parameters are invalid
     */
    @NonNull
    public static AccountKey createNew(
            @NonNull Uri fogReportUri,
            @NonNull String fogReportId,
            @NonNull byte[] fogAuthoritySpki
    ) throws InvalidUriException {
        RistrettoPrivate viewKey = RistrettoPrivate.generateNewKey();
        RistrettoPrivate spendKey = RistrettoPrivate.generateNewKey();
        return new AccountKey(viewKey,
                spendKey,
                fogReportUri,
                fogReportId,
                fogAuthoritySpki
        );
    }

    /**
     * Derives the nth root entropy from a {@code mnemonic}
     * Obtained {@code rootEntropy} can be used to generate an {@code AccountKey} using
     * {@link AccountKey#fromRootEntropy} method.
     */
    public static byte[] deriveAccountRootEntropy(String mnemonic, int accountIndex) throws BadMnemonicException {
        Logger.i(TAG, "Derive root entropy from mnemonic",
                null,
                "account index:", accountIndex);
        byte[] bip39Seed = Mnemonics.getBip39Seed(mnemonic);
        try {
            return Slip10.deriveEd25519PrivateKey(bip39Seed, 44, MOBILECOIN_COIN_TYPE,
                    accountIndex);
        } catch (BadBip39SeedException exception) {
            BadMnemonicException mnemonicException = new BadMnemonicException("Unable to derive " +
                    "root entropy from the mnemonic", exception);
            Util.logException(TAG, mnemonicException);
            throw mnemonicException;
        }
    }

    /**
     * Construct an AccountKey object from the serialized bytes.
     *
     * @param serializedBytes a binary representation of the {@link AccountKey} protocol buffer
     *                        object (see {@link AccountKey#toByteArray()})
     */
    @NonNull
    public static AccountKey fromBytes(@NonNull byte[] serializedBytes)
            throws SerializationException {
        try {
            Logger.i(TAG, "Deserializing AccountKey from byte array");
            MobileCoinAPI.AccountKey accountKey =
                    MobileCoinAPI.AccountKey.parseFrom(serializedBytes);
            return AccountKey.fromProtoBufObject(accountKey);
        } catch (InvalidProtocolBufferException exception) {
            SerializationException serializationException = new SerializationException("Unable to" +
                    " parse serialized AccountKey", exception);
            Util.logException(TAG, serializationException);
            throw serializationException;
        }
    }

    /**
     * Returns a binary representation of the {@link AccountKey} protocol buffer object
     */
    @NonNull
    public byte[] toByteArray() {
        Logger.i(TAG, "Serialize AccountKey to byte array");
        return toProtoBufObject().toByteArray();
    }

    /**
     * @return instance of account's view key
     */
    @NonNull
    RistrettoPrivate getViewKey() {
        return viewKey;
    }

    /**
     * @return instance of account's spend key
     */
    @NonNull
    RistrettoPrivate getSpendKey() {
        return spendKey;
    }

    /**
     * @return instance of account's fog uri
     */
    @NonNull
    public Uri getFogReportUri() {
        return fogReportUri;
    }

    /**
     * @return account's {@link PublicAddress}
     */
    @NonNull
    public PublicAddress getPublicAddress() {
        return publicAddress;
    }

    /**
     * @return account's default subaddress private spend key as {@link RistrettoPrivate}
     */
    @NonNull
    RistrettoPrivate getSubAddressSpendKey() {
        return subAddressSpendKey;
    }

    /**
     * @return account's default subaddress private view key as {@link RistrettoPrivate}
     */
    @NonNull
    RistrettoPrivate getSubAddressViewKey() {
        return subAddressViewKey;
    }

    /**
     * @return fog authority fingerprint signature
     */
    @NonNull
    public byte[] getFogAuthoritySpki() {
        return fogAuthoritySpki;
    }

    /**
     * @return fog report id
     */
    @NonNull
    public String getFogReportId() {
        return fogReportId;
    }

    /**
     * Construct and return a new AccountKey protocol buffer object
     */
    @NonNull
    MobileCoinAPI.AccountKey toProtoBufObject() {
        Logger.i(TAG, "Serialize AccountKey to proto buf");
        MobileCoinAPI.AccountKey.Builder accountBuilder = MobileCoinAPI.AccountKey.newBuilder();
        accountBuilder.setViewPrivateKey(getViewKey().toProtoBufObject());
        accountBuilder.setSpendPrivateKey(getSpendKey().toProtoBufObject());
        accountBuilder.setFogReportUrl(getFogUriString());
        accountBuilder
                .setFogAuthoritySpki(ByteString.copyFrom(getFogAuthoritySpki()));
        accountBuilder.setFogReportId(getFogReportId());
        return accountBuilder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountKey that = (AccountKey) o;
        return fogReportId.equals(that.fogReportId) &&
                fogReportUri.equals(that.fogReportUri) &&
                Arrays.equals(fogAuthoritySpki, that.fogAuthoritySpki) &&
                viewKey.equals(that.viewKey) &&
                spendKey.equals(that.spendKey) &&
                subAddressViewKey.equals(that.subAddressViewKey) &&
                subAddressSpendKey.equals(that.subAddressSpendKey);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(fogReportId, fogReportUri,
                viewKey, spendKey,
                subAddressViewKey, subAddressSpendKey
        );
        result = 31 * result + Arrays.hashCode(fogAuthoritySpki);
        return result;
    }

    @Override
    protected void finalize() throws Throwable {
        Logger.i(TAG, "Finalizing the object");
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    /* Native calls */
    private native void init_jni(
            @NonNull RistrettoPrivate viewKey,
            @NonNull RistrettoPrivate spendKey,
            @NonNull String fogUri,
            @NonNull byte[] fogAuthoritySpki,
            @NonNull String fogReportId
    );

    private native void init_jni_from_root_entropy(
            @NonNull byte[] rootEntropy,
            @NonNull String fogUri,
            @NonNull byte[] fogAuthoritySpki,
            @NonNull String fogReportId
    );

    private native long get_default_subaddress_view_key();

    private native long get_default_subaddress_spend_key();

    private native void finalize_jni();

    @NonNull
    private native String getFogUriString();

    @NonNull
    private native byte[] get_fog_authority_fingerprint();

    @NonNull
    private native String get_report_id();

    private native long get_public_address();

    private native long get_view_key();

    private native long get_spend_key();
}
