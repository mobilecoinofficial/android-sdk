// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.KexRngException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import fog_view.View.RngRecord;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fog_view.View;
import java.util.Objects;
import kex_rng.KexRng.StoredRng;

class FogSeed implements Serializable, Parcelable {
    private final static String TAG = FogSeed.class.getName();

    // Bump serial version and read/write code if fields change
    private static final long serialVersionUID = 1L;

    // RNG
    private ClientKexRng kexRng;
    // Data that comes straight from fog.
    private byte[] nonce;
    private int rngVersion;
    // True if the seed is (a) decommissioned and (b) all utxos have been retrieved.
    private boolean isObsolete;
    private long ingestInvocationId;
    private UnsignedLong startBlock;
    private ArrayList<OwnedTxOut> utxos;

    FogSeed(
            @NonNull RistrettoPrivate privateViewKey,
            @NonNull View.RngRecord rngRecord
    ) throws KexRngException {
        Logger.i(TAG, "Initializing Fog Seed");
        ingestInvocationId = rngRecord.getIngestInvocationId();
        nonce = rngRecord.getPubkey().getPubkey().toByteArray();
        rngVersion = rngRecord.getPubkey().getVersion();
        startBlock = UnsignedLong.fromLongBits(rngRecord.getStartBlock());
        utxos = new ArrayList<>();
        if (rngVersion == 0) {
            kexRng = new ClientKexRng(privateViewKey,
                    nonce,
                    rngVersion
            );
        } else {
            UnsupportedOperationException unsupportedOperationException =
                    new UnsupportedOperationException("Unsupported rng version");
            Util.logException(TAG, unsupportedOperationException);
            throw unsupportedOperationException;
        }
    }

    void update(@NonNull View.RngRecord rngRecord) {
        if (!Arrays.equals(
                nonce,
                rngRecord.getPubkey().getPubkey().toByteArray()
        )) {
            IllegalStateException illegalStateException =
                    new IllegalStateException("Update cannot change the nonce");
            Util.logException(TAG, illegalStateException);
            throw illegalStateException;
        }

        if (rngVersion != rngRecord.getPubkey().getVersion()) {
            UnsupportedOperationException unsupportedOperationException =
                    new UnsupportedOperationException("Updating rng version is not supported");
            Util.logException(TAG, unsupportedOperationException);
            throw unsupportedOperationException;
        }

        if (startBlock.compareTo(UnsignedLong.fromLongBits(rngRecord.getStartBlock())) != 0) {
            IllegalStateException illegalStateException =
                    new IllegalStateException("Start block should never change");
            Util.logException(TAG, illegalStateException);
            throw illegalStateException;
        }
    }

    long getTxoCount() {
        Logger.i(TAG, "Getting TxoCount");
        return utxos.size();
    }

    // Get the next N search keys
    @NonNull
    byte[][] getNextN(long n) throws KexRngException {
        Logger.i(TAG, "Getting the next N search keys", null, n);
        return kexRng.getNextN(n);
    }

    // Get current search key for this seed
    @NonNull
    byte[] getOutput() throws KexRngException {
        Logger.i(TAG, "Getting output");
        return kexRng.getOutput();
    }

    // Advance this seed by 1.
    void advance() throws KexRngException {
        Logger.i(TAG, "Advancing seed");
        kexRng.advance();
    }

    void addTXO(@NonNull OwnedTxOut utxo) throws KexRngException {
        Logger.i(TAG, "Adding TXO");
        utxos.add(utxo);
        advance();
    }

    @NonNull
    public UnsignedLong getStartBlock() {
        return startBlock;
    }

    @NonNull
    List<OwnedTxOut> getTxOuts() {
        return utxos;
    }

    long getIngestInvocationId() {
        return ingestInvocationId;
    }

    boolean isObsolete() {
        return isObsolete;
    }

    void markObsolete() {
        isObsolete = true;
    }


    private void writeObject(ObjectOutputStream out) throws IOException, KexRngException {
        out.write(nonce.length);
        out.write(nonce);

        byte[] storedRngProtobufBytes = kexRng.getProtobufBytes();
        out.write(storedRngProtobufBytes.length);
        out.write(storedRngProtobufBytes);

        out.writeInt(rngVersion);
        out.writeObject(startBlock);
        out.writeObject(utxos);
        out.writeLong(ingestInvocationId);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException, KexRngException {
        int nonceLength = in.read();
        nonce = new byte[nonceLength];
        int bytesRead = in.read(nonce);
        if (bytesRead != nonceLength) {
            throw new IOException();
        }
        int storedRngProtobufBytesLength = in.read();
        byte[] storedRngProtobufBytes = new byte[storedRngProtobufBytesLength];
        int kexRngProtobufBytesRead = in.read(storedRngProtobufBytes);
        if (kexRngProtobufBytesRead != storedRngProtobufBytesLength) {
            throw new IOException();
        }
        kexRng = new ClientKexRng(storedRngProtobufBytes);
        rngVersion = in.readInt();
        startBlock = (UnsignedLong) in.readObject();
        utxos = (ArrayList<OwnedTxOut>) in.readObject();
        ingestInvocationId = in.readLong();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FogSeed fogSeed = (FogSeed) o;
        return rngVersion == fogSeed.rngVersion &&
            isObsolete == fogSeed.isObsolete &&
            ingestInvocationId == fogSeed.ingestInvocationId &&
            Objects.equals(kexRng, fogSeed.kexRng) &&
            Arrays.equals(nonce, fogSeed.nonce) &&
            Objects.equals(startBlock, fogSeed.startBlock) &&
            Objects.equals(utxos, fogSeed.utxos);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(kexRng, flags);
        parcel.writeByteArray(nonce);
        parcel.writeInt(rngVersion);
        parcel.writeByte((byte) (isObsolete ? 1 : 0));
        parcel.writeLong(ingestInvocationId);
        parcel.writeParcelable(startBlock, flags);
        parcel.writeTypedList(utxos);
    }

    private FogSeed(Parcel parcel) {
        kexRng = parcel.readParcelable(ClientKexRng.class.getClassLoader());
        nonce = parcel.createByteArray();
        rngVersion = parcel.readInt();
        isObsolete = parcel.readByte() != 0;
        ingestInvocationId = parcel.readLong();
        startBlock = parcel.readParcelable(UnsignedLong.class.getClassLoader());
        utxos = parcel.createTypedArrayList(OwnedTxOut.CREATOR);
    }

    public static final Creator<FogSeed> CREATOR = new Creator<FogSeed>() {
        @Override
        public FogSeed createFromParcel(Parcel parcel) {
            return new FogSeed(parcel);
        }

        @Override
        public FogSeed[] newArray(int length) {
            return new FogSeed[length];
        }
    };

}
