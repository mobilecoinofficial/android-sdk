// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;
import com.mobilecoin.lib.log.Logger;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;


final class TransactionBuilder extends Native {
    private static final String TAG = TransactionBuilder.class.getName();

    TransactionBuilder(
        @NonNull FogResolver fogResolver,
        @NonNull TxOutMemoBuilder txOutMemoBuilder,
        int blockVersion
    ) throws FogReportException {
        try {
            init_jni(fogResolver, txOutMemoBuilder, blockVersion);
        } catch (Exception exception) {
            throw new FogReportException("Unable to create TxBuilder", exception);
        }
    }

    void addInput(
            @NonNull List<TxOut> ring,
            @NonNull List<TxOutMembershipProof> membershipProofs,
            short realIndex,
            @NonNull RistrettoPrivate onetimePrivateKey,
            @NonNull RistrettoPrivate viewPrivateKey
    ) throws TransactionBuilderException {
        Logger.i(TAG, "Adding transaction input");
        try {
            add_input(ring.toArray(new TxOut[0]),
                    membershipProofs.toArray(new TxOutMembershipProof[0]),
                    realIndex,
                    onetimePrivateKey,
                    viewPrivateKey
            );
        } catch (Exception exception) {
            Logger.e(TAG, "Unable to add transaction input", exception);
            throw new TransactionBuilderException("Unable to add transaction input", exception);
        }
    }

    @NonNull
    TxOut addOutput(
            @NonNull BigInteger value,
            @NonNull PublicAddress recipient,
            @Nullable byte[] confirmationNumberOut
    ) throws TransactionBuilderException {
        Logger.i(TAG, "Adding transaction output");
        byte[] confirmationOut = new byte[Receipt.CONFIRMATION_NUMBER_LENGTH];
        try {
            long rustObj = add_output(value,
                    recipient,
                    confirmationOut
            );
            if (confirmationNumberOut != null) {
                if (confirmationNumberOut.length < Receipt.CONFIRMATION_NUMBER_LENGTH) {
                    throw new IllegalArgumentException("ConfirmationNumber buffer is too small");
                }
                System.arraycopy(confirmationOut, 0, confirmationNumberOut, 0,
                        confirmationOut.length);
            }
            return TxOut.fromJNI(rustObj);
        } catch (Exception exception) {
            Logger.e(TAG, "Unable to add transaction output", exception);
            throw new TransactionBuilderException(exception.getLocalizedMessage(), exception);
        }
    }

    @NonNull
    TxOut addChangeOutput(
        @NonNull BigInteger value,
        @NonNull AccountKey accountKey,
        @Nullable byte[] confirmationNumberOut
    ) throws TransactionBuilderException {
        Logger.i(TAG, "Adding transaction output");
        byte[] confirmationOut = new byte[Receipt.CONFIRMATION_NUMBER_LENGTH];
        try {
            long rustObj = add_change_output(value, accountKey, confirmationOut);
            if (confirmationNumberOut != null) {
                if (confirmationNumberOut.length < Receipt.CONFIRMATION_NUMBER_LENGTH) {
                    throw new IllegalArgumentException("ConfirmationNumber buffer is too small");
                }
                System.arraycopy(confirmationOut, 0, confirmationNumberOut, 0,
                    confirmationOut.length);
            }
            return TxOut.fromJNI(rustObj);
        } catch (Exception exception) {
            Logger.e(TAG, "Unable to add transaction change output", exception);
            throw new TransactionBuilderException(exception.getLocalizedMessage(), exception);
        }
    }

    void setTombstoneBlockIndex(@NonNull UnsignedLong value) throws TransactionBuilderException {
        Logger.i(TAG, String.format(Locale.US, "Set transaction tombstone %s", value.toString()));
        try {
            set_tombstone_block(value.longValue());
        } catch (Exception exception) {
            Logger.e(TAG, "Unable to set tombstone block", exception);
            throw new TransactionBuilderException(exception.getLocalizedMessage(), exception);
        }
    }

    void setFee(long value) throws TransactionBuilderException {
        Logger.i(TAG, String.format(Locale.US, "Set transaction fee %d", value));
        try {
            set_fee(value);
        } catch (Exception exception) {
            Logger.e(TAG, "Unable to set transaction fee", exception);
            throw new TransactionBuilderException(exception.getLocalizedMessage(), exception);
        }
    }

    @NonNull
    public Transaction build() throws TransactionBuilderException {
        Logger.i(TAG, "Building the native transaction");
        try {
            long rustTx = build_tx();
            return Transaction.fromJNI(rustTx);
        } catch (Exception exception) {
            Logger.e(TAG, "Unable to build transaction", exception);
            throw new TransactionBuilderException(
                    "Unable to build transaction from supplied arguments", exception);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    private native void init_jni(@NonNull FogResolver fog_resolver, @NonNull TxOutMemoBuilder txOutMemoBuilder, int blockVersion);

    private native void finalize_jni();

    private native void add_input(
            @NonNull TxOut[] ring,
            @NonNull TxOutMembershipProof[] membershipProofs,
            short real_index,
            @NonNull RistrettoPrivate onetimePrivateKey,
            @NonNull RistrettoPrivate viewPrivateKey
    );

    private native long add_output(
            @NonNull BigInteger value,
            @NonNull PublicAddress recipient,
            @NonNull byte[] confirmationNumberOut
    );

    private native long add_change_output(
        @NonNull BigInteger value,
        @NonNull AccountKey accountKey,
        @NonNull byte[] confirmationNumberOut
    );

    private native void set_tombstone_block(long value);

    private native void set_fee(long value);

    private native long build_tx();
}
