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
import com.mobilecoin.lib.exceptions.BadBip39EntropyException;
import com.mobilecoin.lib.exceptions.BadEntropyException;
import com.mobilecoin.lib.exceptions.BadMnemonicException;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.uri.FogUri;
import com.mobilecoin.lib.util.Hex;

import java.util.Arrays;
import java.util.Objects;

/**
 * The {@code AccountKey} class represents an abstraction of view & spent private keys
 */
public class AccountKey extends Native implements Parcelable {
    private final static String TAG = AccountKey.class.getName();
    private final Uri fogReportUri;
    private final String fogReportId;
    private final byte[] fogAuthoritySpki;
    private final RistrettoPrivate defaultSubAddressViewKey;
    private final RistrettoPrivate defaultSubAddressSpendKey;
    private final RistrettoPrivate changeSubAddressViewKey;
    private final RistrettoPrivate changeSubAddressSpendKey;
    private final RistrettoPrivate viewKey;
    private final RistrettoPrivate spendKey;
    private final PublicAddress publicAddress;

    /**
     * Create an {@link AccountKey} from Ristretto private keys and fog info
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

        // will throw if the Uri is invalid
        new FogUri(fogReportUri);
        try {
            init_jni(viewKey,
                    spendKey,
                    fogReportUri.toString(),
                    fogAuthoritySpki,
                    fogReportId
            );
            // normalize fog report Uri
            this.fogReportUri = fogReportUri;
            this.fogReportId = fogReportId;
            this.fogAuthoritySpki = fogAuthoritySpki;
            this.viewKey = viewKey;
            this.spendKey = spendKey;
            this.defaultSubAddressViewKey = RistrettoPrivate.fromJNI(get_default_subaddress_view_key());
            this.defaultSubAddressSpendKey = RistrettoPrivate.fromJNI(get_default_subaddress_spend_key());
            this.changeSubAddressViewKey = RistrettoPrivate.fromJNI(get_change_subaddress_view_key());
            this.changeSubAddressSpendKey = RistrettoPrivate.fromJNI(get_change_subaddress_spend_key());
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
     * Create an {@link AccountKey} object from root entropy and fog info
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
            this.defaultSubAddressViewKey = RistrettoPrivate.fromJNI(get_default_subaddress_view_key());
            this.defaultSubAddressSpendKey = RistrettoPrivate.fromJNI(get_default_subaddress_spend_key());
            this.changeSubAddressViewKey = RistrettoPrivate.fromJNI(get_change_subaddress_view_key());
            this.changeSubAddressSpendKey = RistrettoPrivate.fromJNI(get_change_subaddress_spend_key());
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
     * Create an {@link AccountKey} object for JNI {@code AccountKey} object reference
     *
     * @param rustObj JNI object reference to a native {@code AccountKey} object
     */
    AccountKey(long rustObj) {
        this.rustObj = rustObj;
        String fogUriString = getFogUriString();
        this.fogReportUri = (fogUriString != null && !fogUriString.isEmpty())
                ? Uri.parse(fogUriString)
                : null;
        this.fogReportId = get_report_id();
        this.fogAuthoritySpki = get_fog_authority_spki();
        this.viewKey = RistrettoPrivate.fromJNI(get_view_key());
        this.spendKey = RistrettoPrivate.fromJNI(get_spend_key());
        this.defaultSubAddressViewKey = RistrettoPrivate.fromJNI(get_default_subaddress_view_key());
        this.defaultSubAddressSpendKey = RistrettoPrivate.fromJNI(get_default_subaddress_spend_key());
        this.changeSubAddressViewKey = RistrettoPrivate.fromJNI(get_change_subaddress_view_key());
        this.changeSubAddressSpendKey = RistrettoPrivate.fromJNI(get_change_subaddress_spend_key());
        this.publicAddress = PublicAddress.fromJNI(get_public_address());
    }

    /**
     * Create an {@link AccountKey} object for JNI {@code AccountKey} object reference
     *
     * @param rustObj JNI object reference to a native {@code AccountKey} object
     */
    static AccountKey fomJNI(long rustObj) {
        return new AccountKey(rustObj);
    }

    /**
     * Create an {@link AccountKey} from root entropy and fog info
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
    static AccountKey fromRootEntropy(
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
     * Create an {@link AccountKey} object from an {@code AccountKey} protobuf object
     *
     * @param accountKey {@code AccountKey} protobuf
     */

    @NonNull
    static AccountKey fromProtoBufObject(@NonNull MobileCoinAPI.AccountKey accountKey)
            throws SerializationException {
        Logger.i(TAG, "Reading AccountKey from the protoBuf object");
        Uri fogReportUri = Uri.parse(accountKey.getFogReportUrl());
        byte[] fogAuthoritySpki = accountKey.getFogAuthoritySpki().toByteArray();
        String fogReportId = accountKey.getFogReportId();

        RistrettoPrivate viewKey =
                RistrettoPrivate.fromProtoBufObject(accountKey.getViewPrivateKey());
        RistrettoPrivate spendKey =
                RistrettoPrivate.fromProtoBufObject(accountKey.getSpendPrivateKey());
        try {
            return new AccountKey(
                    viewKey,
                    spendKey,
                    fogReportUri,
                    fogReportId,
                    fogAuthoritySpki
            );
        } catch (InvalidUriException exception) {
            throw new SerializationException("Invalid serialized uri", exception);
        }
    }

    /**
     * Create an {@link AccountKey} object from auto-generated account keys and fog info. {@link
     * java.security.SecureRandom} is used to generate entropy.
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
     * Derives the nth {@link AccountKey} from a {@code mnemonic}
     *
     * @param fogReportUri     fog report service url.
     * @param fogAuthoritySpki fog authority public key a byte array, provided by the fog operator.
     * @param fogReportId      The fog report server may serve multiple reports, this id.
     *                         disambiguates which one to use when sending to this account.
     * @param mnemonicPhrase   is used as a recovery phrase to derive an {@link AccountKey}.
     * @param accountIndex     account index to derive.
     */
    @NonNull
    public static AccountKey fromMnemonicPhrase(
            @NonNull String mnemonicPhrase,
            int accountIndex,
            @NonNull Uri fogReportUri,
            @NonNull String fogReportId,
            @NonNull byte[] fogAuthoritySpki
    ) throws BadMnemonicException, InvalidUriException {
        Logger.i(TAG, "Derive AccountKey from mnemonic phrase",
                null,
                "account index:", accountIndex);
        try {
            AccountKey accountWithoutFog = AccountKeyDeriver.deriveAccountKeyFromMnemonic(
                    mnemonicPhrase,
                    accountIndex
            );
            return new AccountKey(
                    accountWithoutFog.getViewKey(),
                    accountWithoutFog.getSpendKey(),
                    fogReportUri,
                    fogReportId,
                    fogAuthoritySpki
            );
        } catch (BadBip39EntropyException exception) {
            BadMnemonicException mnemonicException = new BadMnemonicException("Unable to derive " +
                    "AccountKey from the mnemonic phrase", exception);
            Util.logException(TAG, mnemonicException);
            throw mnemonicException;
        }
    }

    /**
     * Derives the nth {@link AccountKey} from a {@code mnemonic}
     *
     * @param fogReportUri     fog report service url.
     * @param fogAuthoritySpki fog authority public key a byte array, provided by the fog operator.
     * @param fogReportId      The fog report server may serve multiple reports, this id.
     *                         disambiguates which one to use when sending to this account.
     * @param bip39Entropy     mnemonic entropy, see {@link Mnemonics#bip39EntropyFromMnemonic}.
     * @param accountIndex     account index to derive.
     */
    @NonNull
    public static AccountKey fromBip39Entropy(
            @NonNull byte[] bip39Entropy,
            int accountIndex,
            @NonNull Uri fogReportUri,
            @NonNull String fogReportId,
            @NonNull byte[] fogAuthoritySpki
    ) throws InvalidUriException, BadEntropyException {
        Logger.i(TAG, "Derive AccountKey from Bip39 entropy",
                null,
                "account index:", accountIndex);
        try {
            String mnemonicPhrase = Mnemonics.bip39EntropyToMnemonic(bip39Entropy);
            AccountKey accountWithoutFog = AccountKeyDeriver.deriveAccountKeyFromMnemonic(
                    mnemonicPhrase,
                    accountIndex
            );
            return new AccountKey(
                    accountWithoutFog.getViewKey(),
                    accountWithoutFog.getSpendKey(),
                    fogReportUri,
                    fogReportId,
                    fogAuthoritySpki
            );
        } catch (BadBip39EntropyException | BadEntropyException exception) {
            BadEntropyException badEntropyException = new BadEntropyException("Unable to derive " +
                    "AccountKey from the bip39 entropy", exception);
            Util.logException(TAG, badEntropyException);
            throw badEntropyException;
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
    public RistrettoPrivate getDefaultSubAddressSpendKey() {
        return defaultSubAddressSpendKey;
    }

    /**
     * @return account's default subaddress private view key as {@link RistrettoPrivate}
     */
    @NonNull
    public RistrettoPrivate getDefaultSubAddressViewKey() {
        return defaultSubAddressViewKey;
    }

    /**
     * @return account's change subaddress private spend key as {@link RistrettoPrivate}
     */
    @NonNull
    public RistrettoPrivate getChangeSubAddressSpendKey() {
        return changeSubAddressSpendKey;
    }

    /**
     * @return account's change subaddress private view key as {@link RistrettoPrivate}
     */
    @NonNull
    public RistrettoPrivate getChangeSubAddressViewKey() {
        return changeSubAddressViewKey;
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

    @Nullable
    private Uri getNormalizedFogReportUri() {
        if (fogReportUri == null) {
            return  null;
        }
        try {
            return new FogUri(fogReportUri).getUri();
        } catch (InvalidUriException exception) {
            throw new IllegalStateException("Bug: the Uri was already verified");
        }
    }

    public boolean equivalent(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountKey that = (AccountKey) o;
        return Objects.equals(fogReportId, that.fogReportId) &&
                getNormalizedFogReportUri().equals(that.getNormalizedFogReportUri()) &&
                Arrays.equals(fogAuthoritySpki, that.fogAuthoritySpki) &&
                viewKey.equals(that.viewKey) &&
                spendKey.equals(that.spendKey) &&
                defaultSubAddressViewKey.equals(that.defaultSubAddressViewKey) &&
                defaultSubAddressSpendKey.equals(that.defaultSubAddressSpendKey) &&
                changeSubAddressViewKey.equals(that.changeSubAddressViewKey) &&
                changeSubAddressSpendKey.equals(that.changeSubAddressSpendKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountKey that = (AccountKey) o;
        return Objects.equals(fogReportId, that.fogReportId) &&
                Objects.equals(fogReportUri, that.fogReportUri) &&
                Arrays.equals(fogAuthoritySpki, that.fogAuthoritySpki) &&
                viewKey.equals(that.viewKey) &&
                spendKey.equals(that.spendKey) &&
                defaultSubAddressViewKey.equals(that.defaultSubAddressViewKey) &&
                defaultSubAddressSpendKey.equals(that.defaultSubAddressSpendKey) &&
                changeSubAddressViewKey.equals(that.changeSubAddressViewKey) &&
                changeSubAddressSpendKey.equals(that.changeSubAddressSpendKey);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(fogReportId, fogReportUri,
                viewKey, spendKey,
                defaultSubAddressViewKey, defaultSubAddressSpendKey,
                changeSubAddressViewKey, changeSubAddressSpendKey
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

    private native long get_change_subaddress_view_key();

    private native long get_change_subaddress_spend_key();

    private native void finalize_jni();

    @Nullable
    private native String getFogUriString();

    @Nullable
    private native byte[] get_fog_authority_spki();

    @Nullable
    private native String get_report_id();

    private native long get_public_address();

    private native long get_view_key();

    private native long get_spend_key();

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
        parcel.writeParcelable(fogReportUri, flags);
        parcel.writeString(fogReportId);
        parcel.writeByteArray(fogAuthoritySpki);
        parcel.writeParcelable(defaultSubAddressViewKey, flags);
        parcel.writeParcelable(defaultSubAddressSpendKey, flags);
        parcel.writeParcelable(changeSubAddressViewKey, flags);
        parcel.writeParcelable(changeSubAddressSpendKey, flags);
        parcel.writeParcelable(viewKey, flags);
        parcel.writeParcelable(spendKey, flags);
        parcel.writeParcelable(publicAddress, flags);
    }

    public static final Creator<AccountKey> CREATOR = new Creator<AccountKey>() {
        /**
         * Create AccountKey from the provided Parcel
         * @param parcel The parcel containing an AccountKey
         * @return The AccountKey contained in the provided Parcel
         */
        @Override
        public AccountKey createFromParcel(Parcel parcel) {
            return new AccountKey(parcel);
        }

        /**
         * Used by Creator to deserialize an array of AccountKeys
         * @param length
         * @return
         */
        @Override
        public AccountKey[] newArray(int length) {
            return new AccountKey[length];
        }
    };

    /**
     * Creates an AccountKey from the provided parcel
     * @param parcel The parcel that contains an AccountKey
     */
    private AccountKey(Parcel parcel) {
        fogReportUri = parcel.readParcelable(Uri.class.getClassLoader());
        fogReportId = parcel.readString();
        fogAuthoritySpki = parcel.createByteArray();
        defaultSubAddressViewKey = parcel.readParcelable(RistrettoPrivate.class.getClassLoader());
        defaultSubAddressSpendKey = parcel.readParcelable(RistrettoPrivate.class.getClassLoader());
        changeSubAddressViewKey = parcel.readParcelable(RistrettoPrivate.class.getClassLoader());
        changeSubAddressSpendKey = parcel.readParcelable(RistrettoPrivate.class.getClassLoader());
        viewKey = parcel.readParcelable(RistrettoPrivate.class.getClassLoader());
        spendKey = parcel.readParcelable(RistrettoPrivate.class.getClassLoader());
        publicAddress = parcel.readParcelable(PublicAddress.class.getClassLoader());
    }

}
