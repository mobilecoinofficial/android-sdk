package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.log.Logger;

import fog_view.View;

public class DefaultVersionedCryptoBox extends Native implements VersionedCryptoBox {

    private static final String TAG = DefaultVersionedCryptoBox.class.getName();

    public DefaultVersionedCryptoBox() {
    }


    @NonNull
    @Override
    public byte[] versionedCryptoBoxDecrypt(@NonNull RistrettoPrivate viewKey,
                                            @NonNull byte[] cipherText) throws InvalidFogResponse {
        Logger.i(TAG, "Decrypting with view key", null,
                "viewKey public:", viewKey.getPublicKey());
        try {
            return versioned_crypto_box_decrypt(
                    viewKey,
                    cipherText
            );
        } catch (Exception ex) {
            throw new InvalidFogResponse(ex.getLocalizedMessage(), ex);
        }
    }

    @NonNull
    private native byte[] versioned_crypto_box_decrypt(
            @NonNull RistrettoPrivate viewKey,
            @NonNull byte[] cipherText
    );

    @NonNull
    @Override
    public OwnedTxOut ownedTxOutFor(@NonNull View.TxOutRecord record, @NonNull AccountKey accountKey) {
        return new OwnedTxOut(record, accountKey);
    }

}
