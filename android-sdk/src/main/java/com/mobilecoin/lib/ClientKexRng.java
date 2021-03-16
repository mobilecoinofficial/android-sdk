// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.KexRngException;
import com.mobilecoin.lib.log.Logger;

class ClientKexRng extends Native {
    private final static String TAG = ClientKexRng.class.getName();

    ClientKexRng(
            @NonNull RistrettoPrivate viewKey,
            @NonNull byte[] seed,
            int version
    ) throws KexRngException {
        Logger.i(TAG, "Initializing ClientKexRng");
        try {
            init_jni(
                    viewKey,
                    seed,
                    version
            );
        } catch (Exception ex) {
            KexRngException kexRngException =
                    new KexRngException("Unable to create a KexRng with the provided arguments", ex);
            Util.logException(TAG, kexRngException);
            throw kexRngException;
        }
    }

    void advance() throws KexRngException {
        Logger.i(TAG, "Advancing ClientKexRng");
        try {
            rng_advance();
        } catch (Exception ex) {
            KexRngException kexRngException =
                    new KexRngException("Unable to advance KexRng", ex);
            Util.logException(TAG, kexRngException);
            throw kexRngException;
        }
    }

    @NonNull
    byte[] getOutput() throws KexRngException {
        Logger.i(TAG, "Getting output");
        try {
            return get_output();
        } catch (Exception ex) {
            KexRngException kexRngException =
                    new KexRngException("Unable to get a KexRng output", ex);
            Util.logException(TAG, kexRngException);
            throw kexRngException;
        }
    }

    @NonNull
    byte[][] getNextN(long n) throws KexRngException {
        Logger.i(TAG, "Getting next N KexRngs");
        try {
            return get_next_n(n);
        } catch (Exception ex) {
            KexRngException kexRngException =
                    new KexRngException("Unable to get the next N KexRng outputs", ex);
            Util.logException(TAG, kexRngException);
            throw kexRngException;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    private native void init_jni(
            @NonNull RistrettoPrivate view_key,
            @NonNull byte[] seed,
            int version
    );

    private native void finalize_jni();

    private native void rng_advance();

    @NonNull
    private native byte[] get_output();

    @NonNull
    private native byte[][] get_next_n(long n);
}
