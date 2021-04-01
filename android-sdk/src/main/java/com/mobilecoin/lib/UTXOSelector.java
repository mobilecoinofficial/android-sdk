// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.exceptions.FragmentedAccountException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;
import com.mobilecoin.lib.log.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Provides the TXO selection for the AccountKey defragmentation, fee estimations, and transaction
 * composition purposes.
 */
class UTXOSelector {
    private static final String TAG = UTXOSelector.class.getName();
    public static final int MAX_INPUTS = 16;

    /**
     * Select optimal TxOuts for merging during the account de-fragmentation This method will select
     * up to MAX_INPUTS elements
     *
     * @param inputs    a set of unspent OwnedTxOuts to select from
     * @param txFee     the fee amount to post a transaction
     * @param inputFee  the fee per each transaction input
     * @param outputFee the fee per each transaction output
     */
    static Selection<OwnedTxOut> selectInputsForMerging(
            @NonNull Set<OwnedTxOut> inputs,
            @NonNull BigInteger txFee,
            @NonNull BigInteger inputFee,
            @NonNull BigInteger outputFee
    ) throws InsufficientFundsException {
        Logger.i(TAG, "Selecting inputs for merging");
        List<TxOutNode> nodes = inputs.stream()
                .map(ownedTxOut -> new TxOutNode(ownedTxOut, null))
                .collect(Collectors.toList());

        TxOutNode txOutNode = selectTxOutNodesForMerging(
                nodes,
                txFee,
                inputFee,
                outputFee
        );
        List<OwnedTxOut> result = txOutNode.children.stream()
                .map(node -> node.txOut)
                .collect(Collectors.toList());
        return new Selection<>(result, txOutNode.getFee(txFee, inputFee));
    }

    @NonNull
    static TxOutNode selectTxOutNodesForMerging(
            @NonNull List<TxOutNode> inputs,
            @NonNull BigInteger txFee,
            @NonNull BigInteger inputFee,
            @NonNull BigInteger outputFee
    ) throws InsufficientFundsException {
        Logger.i(TAG, "Selecting TxOutNodes for merging");
        // sort in descending order so it's easy to pick up the largest TxOuts first
        List<TxOutNode> txOuts = inputs.stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());

        BigInteger totalAmountAvailable = inputs.stream()
                .map(TxOutNode::getValue)
                .reduce(BigInteger.ZERO, BigInteger::add);

        List<TxOutNode> result = new ArrayList<>();
        // initial fee is tx fee + one output fee
        BigInteger fee = txFee.add(outputFee);
        BigInteger selectionAmount = BigInteger.ZERO;
        // select up to MAX_INPUTS txOut nodes
        for (TxOutNode txOutNode : txOuts) {
            if (result.size() == MAX_INPUTS) break;
            BigInteger newSelectionAmount = selectionAmount.add(txOutNode.getValue());
            BigInteger newFee = fee.add(txOutNode.getFee(txFee, inputFee));
            if (newSelectionAmount.add(fee).compareTo(totalAmountAvailable) > 0) break;
            fee = newFee;
            selectionAmount = newSelectionAmount;
            result.add(txOutNode);
        }
        // need at least two inputs for a successful merge
        if (result.size() < 2) {
            throw new InsufficientFundsException();
        }
        return new TxOutNode(null, result);
    }

    /**
     * Select a minimal number of UTXOs required to cover the specified amount. The selection
     * algorithm is looking for the largest UTXO that can cover the amount plus the smallest UTXO to
     * reduce the account fragmentation. If such UTXO cannot be found, add the largest available
     * UTXO and repeat the selection again for the remainder.
     *
     * @param inputs       a list of unspent TxOuts to select from
     * @param amount       the amount of MOB to select UTXOs for
     * @param txFee        the fee amount to post a transaction
     * @param inputFee     the fee per each transaction input
     * @param outputFee    the fee per each transaction output
     * @param outputsCount the number of transaction outputs
     *                     (recipients + an address for remaining change if there is change)
     * @return {@link Selection} contains a list of inputs (up to MAX_INPUTS)
     * and a fee required to post these inputs in a transaction
     */
    @NonNull
    static Selection<OwnedTxOut> selectInputsForAmount(
            @NonNull Set<OwnedTxOut> inputs,
            @NonNull BigInteger amount,
            @NonNull BigInteger txFee,
            @NonNull BigInteger inputFee,
            @NonNull BigInteger outputFee,
            int outputsCount
    ) throws InsufficientFundsException, FragmentedAccountException {
        Logger.i(TAG, "Selecting inputs for amount", null,
                      "amount:", amount,
                      "txFee:", txFee,
                      "inputFee:", inputFee,
                      "outputFee:", outputFee);
        List<TxOutNode> nodes = inputs.stream()
                .map(ownedTxOut -> new TxOutNode(ownedTxOut, null))
                .collect(Collectors.toList());

        Selection<TxOutNode> selection = selectTxOutNodesForAmount(
                nodes,
                amount,
                txFee,
                inputFee,
                outputFee,
                outputsCount
        );
        List<OwnedTxOut> result = selection.txOuts.stream()
                .map(node -> node.txOut)
                .collect(Collectors.toList());
        return new Selection<>(result, selection.fee);
    }

    @NonNull
    static Selection<TxOutNode> selectTxOutNodesForAmount(
            @NonNull List<TxOutNode> inputs,
            @NonNull BigInteger amount,
            @NonNull BigInteger txFee,
            @NonNull BigInteger inputFee,
            @NonNull BigInteger outputFee,
            int outputsCount
    ) throws InsufficientFundsException, FragmentedAccountException {
        Logger.i(TAG, "Selecting TxOutNodes for amount", null,
                      "amount:", amount,
                      "txFee:", txFee,
                      "inputFee:", inputFee,
                      "outputFee:", outputFee);
        BigInteger totalAvailable = inputs.stream()
                .map(TxOutNode::getValue)
                .reduce(BigInteger.ZERO, BigInteger::add);

        if (inputs.size() == 0) {
            throw new InsufficientFundsException();
        }

        // sort vector in the ascending order so it's easy to pick up smalled/largest TxOuts
        Vector<TxOutNode> sortedInputsVector = inputs.stream()
                .sorted((a, b) -> a.getValue().compareTo(b.getValue()))
                .collect(Collectors.toCollection(Vector::new));

        BigInteger fee = txFee.add(BigInteger.valueOf(outputsCount).multiply(outputFee));
        List<TxOutNode> selected = new ArrayList<>();
        // add the first smallest input and the input fee
        TxOutNode input = sortedInputsVector.firstElement();
        fee = fee.add(input.getFee(txFee, inputFee));
        selected.add(input);
        // remove the input from available inputs
        sortedInputsVector.removeElement(input);

        BigInteger selectedAmount = input.getValue();
        while (sortedInputsVector.size() > 0 && selected.size() < MAX_INPUTS) {
            if (selectedAmount.compareTo(amount.add(fee)) >= 0) {
                break;
            }
            // add largest value TxOut and update the fee
            input = sortedInputsVector.lastElement();
            selected.add(input);
            fee = fee.add(input.getFee(txFee, inputFee));
            sortedInputsVector.removeElement(input);
            selectedAmount = selectedAmount.add(input.getValue());
        }

        if (totalAvailable.compareTo(amount.add(fee)) < 0) {
            throw new InsufficientFundsException();
        }

        if (selectedAmount.compareTo(amount.add(fee)) < 0) {
            throw new FragmentedAccountException(
                    "The account requires defragmentation to send the required amount");
        }

        return new Selection<>(selected, fee);
    }

    /**
     * Calculate the total fee required to send a specified amount from the list of unspent inputs
     *
     * @param unspent      a list of unspent TxOuts to select from
     * @param amount       the amount to calculate fee for
     * @param txFee        the fee amount to post a transaction
     * @param inputFee     the fee per each transaction input
     * @param outputFee    the fee per each transaction output
     * @param outputsCount the number of transaction outputs
     *                     (recipients + an address for remaining change if there is change)
     */
    public static BigInteger calculateFee(
            @NonNull Set<OwnedTxOut> unspent,
            @NonNull BigInteger amount,
            @NonNull BigInteger txFee,
            @NonNull BigInteger inputFee,
            @NonNull BigInteger outputFee,
            int outputsCount
    ) throws InsufficientFundsException {
        Logger.i(TAG, "Calculating fee", null,
                      "amount:", amount,
                      "txFee:", txFee,
                      "inputFee:", inputFee,
                      "outputFee:", outputFee);
        // convert inputs into promises for simplify calculation
        List<TxOutNode> inputs = unspent.stream().map(
                txOut -> new TxOutNode(txOut, null)
        ).collect(Collectors.toCollection(ArrayList::new));

        BigInteger totalAmountAvailable = inputs.stream()
                .map(TxOutNode::getValue)
                .reduce(BigInteger.ZERO, BigInteger::add);

        do {
            try {
                Selection<TxOutNode> selection = selectTxOutNodesForAmount(
                        inputs,
                        amount,
                        txFee,
                        inputFee,
                        outputFee,
                        outputsCount
                );
                return selection.fee;
            } catch (FragmentedAccountException e) {
                TxOutNode node = selectTxOutNodesForMerging(inputs, txFee, inputFee, outputFee);
                if (totalAmountAvailable
                        .compareTo(
                                node.getValue().add(node.getFee(txFee, inputFee))
                        ) < 0) {
                    throw new InsufficientFundsException();
                }
                inputs.removeAll(node.children);
                inputs.add(node);
            }
        } while (true);
    }

    /**
     * Calculate the total transferable amount excluding all the required fees for such transfer
     *
     * @param unspent   a list of unspent TxOuts to select from
     * @param txFee     the fee amount to post a transaction
     * @param inputFee  the fee per each transaction input
     * @param outputFee the fee per each transaction output
     */

    public static BigInteger getTransferableAmount(
            @NonNull Set<OwnedTxOut> unspent,
            @NonNull BigInteger txFee,
            @NonNull BigInteger inputFee,
            @NonNull BigInteger outputFee
    ) throws InsufficientFundsException {
        Logger.i(TAG, "Getting transferable amount", null,
                      "unspent:", unspent,
                      "txFee:", txFee,
                      "inputFee:", inputFee,
                      "outputFee:", outputFee);
        // convert inputs into promises for simplify calculation
        List<TxOutNode> inputs = unspent.stream()
                .filter(txOut -> txOut.getValue().compareTo(inputFee) > 0)
                .map(txOut -> new TxOutNode(txOut, null)
                ).collect(Collectors.toCollection(ArrayList::new));

        BigInteger totalAmountAvailable = inputs.stream()
                .map(TxOutNode::getValue)
                .reduce(BigInteger.ZERO, BigInteger::add);
        if (inputs.size() == 0) {
            return BigInteger.ZERO;
        }
        while (inputs.size() > MAX_INPUTS) {
            TxOutNode node = selectTxOutNodesForMerging(inputs, txFee, inputFee, outputFee);
            inputs.removeAll(node.children);
            inputs.add(node);
        }
        BigInteger fees = inputs.stream()
                .map(txOutNode -> txOutNode.getFee(txFee, inputFee))
                .reduce(txFee, BigInteger::add);
        if (totalAmountAvailable.compareTo(fees) <= 0) {
            return BigInteger.ZERO;
        }
        return totalAmountAvailable.subtract(fees);
    }

    static class Selection<T> {
        public final List<T> txOuts;
        public final BigInteger fee;

        public Selection(
                @NonNull List<T> txOuts,
                @NonNull BigInteger fee
        ) {
            this.txOuts = txOuts;
            this.fee = fee;
        }
    }

    static class TxOutNode {
        public final OwnedTxOut txOut;
        public final List<TxOutNode> children;

        TxOutNode(@Nullable OwnedTxOut txOut, @Nullable List<TxOutNode> children) {
            if (null == children) {
                children = new ArrayList<>();
            }
            this.children = children;
            this.txOut = txOut;
        }

        @NonNull
        BigInteger getValue() {
            return (null != txOut)
                    ? txOut.getValue()
                    : children.stream()
                    .map(TxOutNode::getValue)
                    .reduce(BigInteger.ZERO, BigInteger::add);
        }

        @NonNull
        BigInteger getFee(@NonNull BigInteger txFee, @NonNull BigInteger inputFee) {
            return (null != txOut)
                    ? inputFee
                    : txFee.add(
                    children.stream()
                            .map(txOutNode -> txOutNode.getFee(txFee, inputFee))
                            .reduce(BigInteger.ZERO, BigInteger::add)
            );
        }
    }
}
