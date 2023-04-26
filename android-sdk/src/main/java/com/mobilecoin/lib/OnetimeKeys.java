package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.TransactionBuilderException;
import com.mobilecoin.lib.log.Logger;

public class OnetimeKeys extends Native {

    private static final String TAG = OnetimeKeys.class.getName();

    public static RistrettoPrivate recoverOnetimePrivateKey(
            @NonNull final RistrettoPublic tx_pub_key,
            @NonNull final RistrettoPublic tx_target_key,
            @NonNull final AccountKey account_key
    ) throws TransactionBuilderException {
        Logger.i(TAG, "Recovering onetime private key", null, "tx_pub_key:", tx_pub_key);
        try {
            long rustObj = recover_onetime_private_key(
                    tx_pub_key,
                    tx_target_key,
                    account_key
            );
            return RistrettoPrivate.fromJNI(rustObj);
        } catch (Exception ex) {
            throw new TransactionBuilderException(ex.getLocalizedMessage(), ex);
        }
    }

    @NonNull
    public static RistrettoPublic getSharedSecret(
        @NonNull final RistrettoPrivate viewPrivateKey,
        @NonNull final RistrettoPublic txOutPublicKey
    ) throws TransactionBuilderException {
      Logger.i(TAG, "Retrieving shared secret", null, "txOut public:", txOutPublicKey);
      try {
        long rustObj = get_shared_secret(viewPrivateKey, txOutPublicKey);
        return RistrettoPublic.fromJNI(rustObj);
      } catch(Exception ex) {
        throw new TransactionBuilderException(ex.getLocalizedMessage(), ex);
      }
    }

    @NonNull
    public static RistrettoPublic createTxOutPublicKey(
            @NonNull final RistrettoPrivate txOutPrivateKey,
            @NonNull final RistrettoPublic recipientSpendPublicKey
    ) {
        return RistrettoPublic.fromJNI(create_tx_out_public_key(txOutPrivateKey, recipientSpendPublicKey));
    }

    private static native long recover_onetime_private_key(
            @NonNull final RistrettoPublic tx_pub_key,
            @NonNull final RistrettoPublic tx_target_key,
            @NonNull final AccountKey account_key
    );

    private static native long get_shared_secret(
        @NonNull final RistrettoPrivate viewPrivateKey,
        @NonNull final RistrettoPublic txOutPublicKey
    );

    private static native long create_tx_out_public_key(
            @NonNull final RistrettoPrivate tx_out_private_key,
            @NonNull final RistrettoPublic recipient_spend_key
    );
}
