// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

import java.util.ArrayList;
import java.util.List;

final class Ring {
    private final static String TAG = Ring.class.getName();
    public final short realIndex;
    public final OwnedTxOut utxo;
    private final List<TxOut> nativeTxOuts;
    private final List<TxOutMembershipProof> nativeTxOutMembershipProofs;
    private final List<MobileCoinAPI.TxOut> txOuts;
    private final List<MobileCoinAPI.TxOutMembershipProof> proofs;

    public Ring(
            @NonNull List<MobileCoinAPI.TxOut> txOuts,
            @NonNull List<MobileCoinAPI.TxOutMembershipProof> proofs,
            short realIndex,
            @NonNull OwnedTxOut utxo
    ) throws SerializationException {
        Logger.d(TAG, "Initializing ring", null,
                    "txOuts:", txOuts,
                    "proofs:", proofs,
                    "realIndex:", realIndex,
                    "utxo:", utxo);
        this.txOuts = txOuts;
        this.proofs = proofs;
        this.realIndex = realIndex;
        this.utxo = utxo;

        this.nativeTxOuts = new ArrayList<>();
        for (MobileCoinAPI.TxOut txOut : txOuts) {
            nativeTxOuts.add(TxOut.fromProtoBufObject(txOut));
        }

        this.nativeTxOutMembershipProofs = new ArrayList<>();
        for (MobileCoinAPI.TxOutMembershipProof proof : this.proofs) {
            nativeTxOutMembershipProofs.add(new TxOutMembershipProof(proof.toByteArray()));
        }
    }

    @NonNull
    public List<TxOut> getNativeTxOuts() {
        return nativeTxOuts;
    }

    @NonNull
    public List<TxOutMembershipProof> getNativeTxOutMembershipProofs() {
        return nativeTxOutMembershipProofs;
    }
}
