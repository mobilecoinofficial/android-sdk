package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidFogResponse;

import fog_view.View;

public interface VersionedCryptoBox {
    @NonNull
    public byte[] versionedCryptoBoxDecrypt(@NonNull RistrettoPrivate viewKey,
                                            @NonNull byte[] cipherText) throws InvalidFogResponse;

    @NonNull
    public OwnedTxOut ownedTxOutFor(@NonNull View.TxOutRecord record, @NonNull AccountKey accountKey);

    @NonNull ViewableTxOut viewableTxOutFor(@NonNull View.TxOutRecord record, @NonNull ViewAccountKey viewAccountKey);

}
