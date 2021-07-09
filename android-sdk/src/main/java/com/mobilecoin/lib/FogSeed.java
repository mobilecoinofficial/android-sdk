// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.KexRngException;
import com.mobilecoin.lib.log.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fog_view.View;

final class FogSeed implements Serializable {
    private final static String TAG = FogSeed.class.getName();

    // Bump serial version and read/write code if fields change
    private static final long serialVersionUID = 1L;

    private final long ingestInvocationId;
    // RNG
    private final ClientKexRng kexRng;
    // Data that comes straight from fog.
    private byte[] nonce;
    private int rngVersion;
    // True if the seed is decommissioned and all utxos have been retrieved.
    private boolean isDeprecated;
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

    public long getIngestInvocationId() {
        return ingestInvocationId;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public void deprecate() {
        isDeprecated = true;
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.write(nonce.length);
        out.write(nonce);
        out.writeInt(rngVersion);
        out.writeObject(startBlock);
        out.writeObject(utxos);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int nonceLength = in.read();
        nonce = new byte[nonceLength];
        int bytesRead = in.read(nonce);
        if (bytesRead != nonceLength) {
            throw new IOException();
        }
        rngVersion = in.readInt();
        startBlock = (UnsignedLong) in.readObject();
        utxos = (ArrayList<OwnedTxOut>) in.readObject();
    }
}
