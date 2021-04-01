// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.FragmentedAccountException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UTXOSelectorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_select_from_empty_account()
            throws FragmentedAccountException, InsufficientFundsException {
        thrown.expect(InsufficientFundsException.class);
        List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
        UTXOSelector.selectTxOutNodesForAmount(utxos,
                BigInteger.ONE,
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.ZERO,
                2
        );
    }

    @Test
    public void test_select_from_fragmented_account()
            throws FragmentedAccountException, InsufficientFundsException {
        thrown.expect(FragmentedAccountException.class);
        List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            utxos.add(mockTxOutNode(BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO));
        }
        UTXOSelector.selectTxOutNodesForAmount(utxos,
                BigInteger.valueOf(50),
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.ZERO,
                2
        );
    }

    @Test
    public void test_select_for_defrag() throws InsufficientFundsException {
        List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
        BigInteger txFee = BigInteger.ZERO;
        BigInteger inputFee = BigInteger.ZERO;
        BigInteger smallAmount = BigInteger.ONE;
        for (int i = 0; i < 3; i++) {
            utxos.add(mockTxOutNode(smallAmount, txFee, inputFee));
        }
        UTXOSelector.TxOutNode mergeNode = UTXOSelector.selectTxOutNodesForMerging(
                utxos,
                txFee,
                inputFee,
                BigInteger.ZERO
        );

        // all 3 UTXOs must be selected for this use case
        Assert.assertEquals(
                mergeNode.children.size(),
                3
        );
        // add MAX_INPUTS more larger TXOs of value 10
        BigInteger largerAmount = BigInteger.TEN;
        for (int i = 0; i < UTXOSelector.MAX_INPUTS; i++) {
            utxos.add(mockTxOutNode(largerAmount, txFee, inputFee));
        }
        mergeNode = UTXOSelector.selectTxOutNodesForMerging(
                utxos,
                txFee,
                inputFee,
                BigInteger.ZERO
        );
        // all larger UTXOs must be selected
        Assert.assertEquals(
                mergeNode.children.size(),
                UTXOSelector.MAX_INPUTS
        );
        for (UTXOSelector.TxOutNode node : mergeNode.children) {
            // if any of the small amount nodes are selected this must fail
            Assert.assertEquals(node.getValue(), largerAmount);
        }
    }

    @Test
    public void test_tx_auto_defrag()
            throws FragmentedAccountException, InsufficientFundsException {
        BigInteger txFee = BigInteger.ZERO;
        BigInteger inputFee = BigInteger.ZERO;
        List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
        utxos.add(mockTxOutNode(BigInteger.ONE, txFee, inputFee));
        utxos.add(mockTxOutNode(BigInteger.valueOf(100), txFee, inputFee));
        utxos.add(mockTxOutNode(BigInteger.ONE, txFee, inputFee));
        UTXOSelector.Selection<UTXOSelector.TxOutNode> selection =
                UTXOSelector.selectTxOutNodesForAmount(utxos,
                        BigInteger.valueOf(3),
                        txFee,
                        inputFee,
                        BigInteger.ZERO,
                        2
                );
        Assert.assertEquals(
                selection.txOuts.size(),
                2
        );
    }

    @Test
    public void test_insufficient_funds()
            throws FragmentedAccountException, InsufficientFundsException {
        thrown.expect(InsufficientFundsException.class);
        List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
        utxos.add(mockTxOutNode(BigInteger.TEN, BigInteger.ZERO, BigInteger.ZERO));
        UTXOSelector.selectTxOutNodesForAmount(utxos,
                BigInteger.valueOf(15),
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.ZERO,
                2
        );
    }

    @Test
    public void test_fee_is_factored_in()
            throws FragmentedAccountException, InsufficientFundsException {
        thrown.expect(InsufficientFundsException.class);
        BigInteger txFee = BigInteger.ONE;
        BigInteger inputFee = BigInteger.ZERO;
        List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
        utxos.add(mockTxOutNode(BigInteger.TEN, txFee, inputFee));
        UTXOSelector.selectTxOutNodesForAmount(utxos,
                BigInteger.TEN,
                txFee,
                inputFee,
                BigInteger.ZERO,
                2
        );
    }

    @Test
    public void test_fee_calculation() throws InsufficientFundsException {
        BigInteger amount = BigInteger.TEN;
        BigInteger txFee = BigInteger.TEN;
        BigInteger inputFee = BigInteger.ZERO;
        BigInteger outputFee = BigInteger.ZERO;

        HashSet<OwnedTxOut> utxos = new HashSet<>();
        utxos.add(mockTxOut(BigInteger.TEN, txFee, inputFee));
        utxos.add(mockTxOut(BigInteger.TEN, txFee, inputFee));

        BigInteger fee = UTXOSelector.calculateFee(utxos, amount, txFee, inputFee, outputFee, 2);
        Assert.assertEquals(fee,
                inputFee.add(txFee).add(outputFee.multiply(BigInteger.valueOf(2))));
    }

    @Test
    public void test_multi_step_fee_calculation() throws InsufficientFundsException {
        BigInteger txFee = BigInteger.ONE;
        BigInteger inputFee = BigInteger.ZERO;
        BigInteger outputFee = BigInteger.ZERO;

        HashSet<OwnedTxOut> utxos = new HashSet<>();
        for (int i = 0; i < 100; ++i) {
            utxos.add(mockTxOut(BigInteger.ONE, txFee, inputFee));
        }

        BigInteger fee = UTXOSelector.calculateFee(utxos, BigInteger.valueOf(10), txFee, inputFee
                , outputFee, 2);
        Assert.assertEquals(fee,
                inputFee.add(txFee).add(outputFee.multiply(BigInteger.valueOf(2))));

        fee = UTXOSelector.calculateFee(utxos, BigInteger.valueOf(20), txFee, inputFee, outputFee
                , 2);
        Assert.assertEquals(fee,
                inputFee.add(txFee.multiply(BigInteger.valueOf(2))).add(outputFee.multiply(BigInteger.valueOf(2))));

        fee = UTXOSelector.calculateFee(utxos, BigInteger.valueOf(32), txFee, inputFee, outputFee
                , 2);
        Assert.assertEquals(fee,
                inputFee.add(txFee.multiply(BigInteger.valueOf(3))).add(outputFee.multiply(BigInteger.valueOf(2))));
    }


    UTXOSelector.TxOutNode mockTxOutNode(BigInteger value, BigInteger txFee, BigInteger inputFee) {
        UTXOSelector.TxOutNode node = mock(UTXOSelector.TxOutNode.class);
        when(node.getValue()).thenReturn(value);
        when(node.getFee(txFee, inputFee)).thenReturn(inputFee);
        return node;
    }

    OwnedTxOut mockTxOut(BigInteger value, BigInteger txFee, BigInteger inputFee) {
        OwnedTxOut txOut = mock(OwnedTxOut.class);
        when(txOut.getValue()).thenReturn(value);
        return txOut;
    }

    @Test
    public void test_small_balance_transfer() throws InsufficientFundsException {
        BigInteger txFee = BigInteger.valueOf(1);
        BigInteger smallAmount = BigInteger.ONE;
        HashSet<OwnedTxOut> txOuts = new HashSet<>();
        final int numTxOuts = 5;
        for (int i = 0; i < numTxOuts; i++) {
            txOuts.add(mockTxOut(smallAmount, txFee, BigInteger.ZERO));
        }

        BigInteger transferableBalance = UTXOSelector.getTransferableAmount(txOuts, txFee,
                BigInteger.ZERO, BigInteger.ZERO);
        Assert.assertEquals(smallAmount.multiply(BigInteger.valueOf(numTxOuts).subtract(txFee)),
                transferableBalance);
    }

    @Test
    public void test_fragmented_balance_transfer() throws InsufficientFundsException {
        BigInteger txFee = BigInteger.valueOf(1);
        BigInteger smallAmount = BigInteger.ONE;
        HashSet<OwnedTxOut> txOuts = new HashSet<>();
        final int numTxOuts = UTXOSelector.MAX_INPUTS + 1;
        for (int i = 0; i < numTxOuts; i++) {
            txOuts.add(mockTxOut(smallAmount, txFee, BigInteger.ZERO));
        }

        BigInteger transferableBalance = UTXOSelector.getTransferableAmount(txOuts, txFee,
                BigInteger.ZERO, BigInteger.ZERO);
        Assert.assertEquals(smallAmount
                        .multiply(BigInteger.valueOf(numTxOuts)
                                .subtract(txFee.multiply(BigInteger.valueOf(2)))),
                transferableBalance);
    }

    @Test
    public void test_multi_level_fragmented_balance_transfer() throws InsufficientFundsException {
        BigInteger txFee = BigInteger.valueOf(1);
        BigInteger smallAmount = BigInteger.ONE;
        HashSet<OwnedTxOut> txOuts = new HashSet<>();
        final int numTxOuts = UTXOSelector.MAX_INPUTS * 10;
        for (int i = 0; i < numTxOuts; i++) {
            txOuts.add(mockTxOut(smallAmount, txFee, BigInteger.ZERO));
        }

        BigInteger transferableBalance = UTXOSelector.getTransferableAmount(txOuts, txFee,
                BigInteger.ZERO, BigInteger.ZERO);
        Assert.assertEquals(smallAmount
                        .multiply(BigInteger.valueOf(numTxOuts)
                                .subtract(txFee.multiply(BigInteger.valueOf(11)))),
                transferableBalance);
    }
}

