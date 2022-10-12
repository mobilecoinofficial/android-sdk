package com.mobilecoin.lib;

import androidx.annotation.NonNull;

// This node class is used to give TreeSet a way to sort OwnedTxOuts by Amount
class OwnedTxOutAmountTreeNode implements Comparable<OwnedTxOutAmountTreeNode> {

    @NonNull final OwnedTxOut otxo;

    OwnedTxOutAmountTreeNode(@NonNull final OwnedTxOut otxo) {
        this.otxo = otxo;
    }

    @Override
    public int compareTo(OwnedTxOutAmountTreeNode other) {
        return this.otxo.getAmount().compareTo(other.otxo.getAmount());
    }
}