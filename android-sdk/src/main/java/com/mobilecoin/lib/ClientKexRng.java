// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.KexRngException;
import com.mobilecoin.lib.log.Logger;
import java.io.Serializable;
import java.util.Arrays;

final class ClientKexRng extends Native implements Serializable, Parcelable {//TODO: Remove Serializable implementation
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

    ClientKexRng(@NonNull byte[] protobufBytes) throws KexRngException {
        Logger.i(TAG, "Initializing ClientKexRng");
        try {
            init_from_stored_rng_protobuf_bytes(protobufBytes);
        } catch (Exception ex) {
            KexRngException kexRngException =
                new KexRngException("Unable to create a KexRng from the protobuf bytes", ex);
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

    @NonNull
    byte[] getProtobufBytes() throws KexRngException {
        Logger.i(TAG, "Getting protobuf bytes.");
        try {
            return get_stored_rng_protobuf_bytes();
        } catch (Exception ex) {
            KexRngException kexRngException =
                new KexRngException("Unable to get protobuf bytes.", ex);
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

    private native void init_from_stored_rng_protobuf_bytes(byte[] bytes);

    private native byte[] get_stored_rng_protobuf_bytes();

    private native void finalize_jni();

    private native void rng_advance();

    @NonNull
    private native byte[] get_output();

    @NonNull
    private native byte[][] get_next_n(long n);


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientKexRng that = (ClientKexRng) o;

        boolean equal = Arrays.equals(
            this.get_stored_rng_protobuf_bytes(),
            that.get_stored_rng_protobuf_bytes()
        );

        return equal;
    }

    public static final Creator<ClientKexRng> CREATOR = new Creator<ClientKexRng>() {
        @Override
        public ClientKexRng createFromParcel(Parcel parcel) {
            try {
                return new ClientKexRng(parcel.createByteArray());
            } catch (KexRngException e) {
                Logger.e(ClientKexRng.class.getSimpleName(), "Failed to deserialize ClientKexRng", e);
            }
            return null;
        }

        @Override
        public ClientKexRng[] newArray(int length) {
            return new ClientKexRng[length];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        try {
            parcel.writeByteArray(getProtobufBytes());
        } catch (KexRngException e) {
            Logger.e(ClientKexRng.class.getSimpleName(), "Failed to serialize ClientKexRng", e);
        }
    }
}
