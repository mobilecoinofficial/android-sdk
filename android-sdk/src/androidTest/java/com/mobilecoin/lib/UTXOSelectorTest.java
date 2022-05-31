// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mobilecoin.lib.exceptions.FragmentedAccountException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class UTXOSelectorTest {

  @Test(expected = InsufficientFundsException.class)
  public void selectTxOutNodesForMerging_oneInput() throws Exception {
    BigInteger txFee = BigInteger.ZERO;
    BigInteger inputFee = BigInteger.ZERO;
    BigInteger smallAmount = BigInteger.ONE;
    List<UTXOSelector.TxOutNode> utxos =
            Collections.singletonList(createMockTxOutNode(smallAmount, txFee, inputFee));

    UTXOSelector.selectTxOutNodesForMerging(utxos, txFee, inputFee, /* outputFee=
     */ BigInteger.ZERO);
  }

  @Test(expected = InsufficientFundsException.class)
  public void selectTxOutNodesForMerging_txFeeIsTooLargeToMerge() throws Exception {
    List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
    BigInteger txFee = BigInteger.TEN;
    BigInteger inputFee = BigInteger.ZERO;
    BigInteger smallAmount = BigInteger.ONE;
    for (int i = 0; i < 3; i++) {
      utxos.add(createMockTxOutNode(smallAmount, txFee, inputFee));
    }

    UTXOSelector.selectTxOutNodesForMerging(utxos, txFee, inputFee, /* outputFee=
     */ BigInteger.ZERO);

  }

  @Test(expected = InsufficientFundsException.class)
  public void selectTxOutNodesForMerging_inputFeeIsTooLargeToMerge() throws Exception {
    List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
    BigInteger txFee = BigInteger.ZERO;
    BigInteger inputFee = BigInteger.TEN;
    BigInteger smallAmount = BigInteger.ONE;
    for (int i = 0; i < 3; i++) {
      utxos.add(createMockTxOutNode(smallAmount, txFee, inputFee));
    }

    UTXOSelector.selectTxOutNodesForMerging(utxos, txFee, inputFee, /* outputFee=
     */ BigInteger.ZERO);
  }

  @Test
  public void selectTxOutNodesForMerging_selectsCorrectNodes() throws Exception {
    List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
    BigInteger txFee = BigInteger.ZERO;
    BigInteger inputFee = BigInteger.ZERO;
    BigInteger smallAmount = BigInteger.ONE;
    for (int i = 0; i < 3; i++) {
      utxos.add(createMockTxOutNode(smallAmount, txFee, inputFee));
    }

    UTXOSelector.TxOutNode mergeNode = UTXOSelector.selectTxOutNodesForMerging(
            utxos,
            txFee,
            inputFee,
            /* outputFee= */ BigInteger.ZERO
    );

    // All 3 UTXOs must be selected for this use case.
    Assert.assertEquals(3, mergeNode.children.size());

    // Add MAX_INPUTS count of larger TXOs of value 10.
    BigInteger largerAmount = BigInteger.TEN;
    for (int i = 0; i < UTXOSelector.MAX_INPUTS; i++) {
      utxos.add(createMockTxOutNode(largerAmount, txFee, inputFee));
    }

    mergeNode = UTXOSelector.selectTxOutNodesForMerging(
            utxos,
            txFee,
            inputFee,
            /* outputFee= */ BigInteger.ZERO
    );
    // All larger UTXOs must be selected.
    Assert.assertEquals(UTXOSelector.MAX_INPUTS, mergeNode.children.size());
    for (UTXOSelector.TxOutNode node : mergeNode.children) {
      // If any of the small amount nodes are selected this must fail.
      Assert.assertEquals(largerAmount, node.getValue());
    }
  }

  @Test(expected = InsufficientFundsException.class)
  public void selectTxOutNodesForAmount_emptyAccount()
          throws FragmentedAccountException, InsufficientFundsException {
    List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();

    UTXOSelector.selectTxOutNodesForAmount(utxos,
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ZERO,
            BigInteger.ZERO,
            /* outputsCount= */ 2
    );
  }

  @Test(expected = FragmentedAccountException.class)
  public void selectTxOutNodesForAmount_fragmentedAccount()
          throws FragmentedAccountException, InsufficientFundsException {
    List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      utxos.add(createMockTxOutNode(/* value=*/ BigInteger.ONE, /* txFee= */ BigInteger.ZERO, /* inputFee= */ BigInteger.ZERO));
    }

    UTXOSelector.selectTxOutNodesForAmount(utxos,
            /* amount= */ BigInteger.valueOf(50),
            /* txFee= */  BigInteger.ZERO,
            /* inputFee= */ BigInteger.ZERO,
            /* outputFee= */ BigInteger.ZERO,
            /* outputsCount= */2
    );
  }

  @Test
  public void selectTxOutNodesForAmount_threeInputs_defragmentsInputs() throws Exception {
    BigInteger txFee = BigInteger.ZERO;
    BigInteger inputFee = BigInteger.ZERO;
    List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
    UTXOSelector.TxOutNode firstTxOut = createMockTxOutNode(/* value= */ BigInteger.ONE,
            txFee, inputFee);
    UTXOSelector.TxOutNode secondTxOut = createMockTxOutNode(/* value= */ BigInteger.TEN,
            txFee, inputFee);
    UTXOSelector.TxOutNode thirdTxOut = createMockTxOutNode(/* value= */ BigInteger.ONE,
            txFee, inputFee);
    utxos.add(firstTxOut);
    utxos.add(secondTxOut);
    utxos.add(thirdTxOut);

    UTXOSelector.Selection<UTXOSelector.TxOutNode> selection =
            UTXOSelector.selectTxOutNodesForAmount(utxos,
                    /* amount= */ BigInteger.valueOf(3),
                    txFee,
                    inputFee,
                    /* outputFee= */ BigInteger.ZERO,
                    /* outputsCount= */ 2
            );

    List<UTXOSelector.TxOutNode> selectedTxOuts = selection.txOuts;
    Assert.assertEquals(2, selectedTxOuts.size());
    Assert.assertEquals(firstTxOut, selectedTxOuts.get(0));
    Assert.assertEquals(secondTxOut, selectedTxOuts.get(1));
  }

  @Test(expected = InsufficientFundsException.class)
  public void selectTxOutNodesForAmount_inputsDoNotCoverAmount() throws Exception {
    List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
    utxos.add(createMockTxOutNode(/* value= */ BigInteger.TEN, /* txFee= */ BigInteger.ZERO, /* inputFee= */ BigInteger.ZERO));

    UTXOSelector.selectTxOutNodesForAmount(utxos,
            /* amount= */ BigInteger.valueOf(15),
            /* txFee= */  BigInteger.ZERO,
            /* inputFee= */ BigInteger.ZERO,
            /* outputFee= */ BigInteger.ZERO,
            /* outputsCount= */ 2
    );
  }

  @Test(expected = InsufficientFundsException.class)
  public void selectTxOutNodesForAmount_inputsDoNotCoverAmountPlusInpuFee() throws Exception {
    BigInteger txFee = BigInteger.ONE;
    BigInteger inputFee = BigInteger.ZERO;
    List<UTXOSelector.TxOutNode> utxos = new ArrayList<>();
    utxos.add(createMockTxOutNode(/* value= */ BigInteger.TEN, txFee, inputFee));

    UTXOSelector.selectTxOutNodesForAmount(utxos,
            /* amount= */ BigInteger.TEN,
            txFee,
            inputFee,
            /* outputFee= */ BigInteger.ZERO,
            /* outputsCount */2
    );
  }

  @Test
  public void calculateFee_nonZeroTxFee_zeroInputFee_zeroOutputFee_calculatesFee() throws Exception {
    BigInteger amount = BigInteger.TEN;
    BigInteger txFee = BigInteger.TEN;
    BigInteger inputFee = BigInteger.ZERO;
    BigInteger outputFee = BigInteger.ZERO;
    Set<OwnedTxOut> utxos = new HashSet<>();
    utxos.add(createMockTxOut(new Amount(BigInteger.TEN, KnownTokenId.MOB.getId())));
    utxos.add(createMockTxOut(new Amount(BigInteger.TEN, KnownTokenId.MOB.getId())));

    BigInteger fee = UTXOSelector.calculateFee(utxos, amount, txFee, inputFee, outputFee, /*
        outputsCount= */ 2);

    BigInteger expectedFee = inputFee.add(txFee).add(outputFee.multiply(BigInteger.valueOf(2)));
    Assert.assertEquals(expectedFee, fee);
  }

  @Test
  public void calculateFee_nonZeroTxFee_nonZeroInputFee_nonZeroOutputFee_calculatesFee() throws Exception {
    BigInteger amount = BigInteger.TEN;
    BigInteger txFee = BigInteger.TEN;
    BigInteger inputFee = BigInteger.valueOf(2);
    BigInteger outputFee = BigInteger.ONE;
    Set<OwnedTxOut> utxos = new HashSet<>();
    utxos.add(createMockTxOut(new Amount(BigInteger.valueOf(13), KnownTokenId.MOB.getId())));
    utxos.add(createMockTxOut(new Amount(BigInteger.valueOf(13), KnownTokenId.MOB.getId())));

    BigInteger fee = UTXOSelector.calculateFee(utxos, amount, txFee, inputFee, outputFee, /*
        outputsCount= */ 2);

    BigInteger expectedFee = inputFee.multiply(BigInteger.valueOf(2)).add(txFee).add(outputFee.multiply(BigInteger.valueOf(2)));
    Assert.assertEquals(expectedFee, fee);
  }

  @Test
  public void calculateFee_multiStep_calculatesFees() throws Exception {
    BigInteger txFee = BigInteger.ONE;
    BigInteger inputFee = BigInteger.valueOf(2);
    BigInteger outputFee = BigInteger.ONE;
    int outputsCount = 2;

    Set<OwnedTxOut> utxos = new HashSet<>();
    int numberOfInputUtxos = 100;
    for (int i = 0; i < numberOfInputUtxos; ++i) {
      utxos.add(createMockTxOut(new Amount(BigInteger.valueOf(3), KnownTokenId.MOB.getId())));
    }

    BigInteger fee = UTXOSelector.calculateFee(utxos, /* amount= */ BigInteger.valueOf(10),
            txFee, inputFee, outputFee, outputsCount);

    BigInteger firstExpectedOutPutFee = outputFee.multiply(BigInteger.valueOf(outputsCount));
    BigInteger firstExpectedNumberOfInputs = BigInteger.valueOf(13);
    BigInteger firstExpectedInputFee = inputFee.multiply(firstExpectedNumberOfInputs);
    BigInteger firstExpectedTotalFee = txFee.add(firstExpectedOutPutFee).add(firstExpectedInputFee);
    Assert.assertEquals(firstExpectedTotalFee, fee);

    // This transaction leads to a fragmented state. This section ensures that calculateFee
    // performs defragmentation correctly.
    fee = UTXOSelector.calculateFee(utxos, /* amount= */ BigInteger.valueOf(20), txFee,
            inputFee, outputFee, outputsCount);

    // The defragmentation process involves an additional transaction, so the total number
    // of transactions is 2.
    BigInteger secondExpectedNumberOfTransactions = BigInteger.valueOf(2);
    BigInteger secondExpectedTxFee = txFee.multiply(secondExpectedNumberOfTransactions);
    BigInteger secondExpectedOutPutFee = outputFee.multiply(BigInteger.valueOf(outputsCount));
    BigInteger secondExpectedNumberOfInputs = BigInteger.valueOf(24);
    BigInteger secondExpectedInputFee = inputFee.multiply(secondExpectedNumberOfInputs);
    BigInteger secondExpectedTotalFee = secondExpectedTxFee.add(secondExpectedOutPutFee).add(secondExpectedInputFee);
    Assert.assertEquals(secondExpectedTotalFee, fee);

    // This transaction also leads to a fragmented state. This section ensures that calculateFee
    // performs defragmentation correctly.
    fee = UTXOSelector.calculateFee(utxos, /* amount= */ BigInteger.valueOf(32), txFee,
            inputFee, outputFee
            , outputsCount);

    // The defragmentation process involves two additional transaction, so the total number
    // of transactions is 3.
    BigInteger thirdExpectedNumberOfTransactions = BigInteger.valueOf(3);
    BigInteger thirdExpectedTxFee = txFee.multiply(thirdExpectedNumberOfTransactions);
    BigInteger thirdExpectedOutPutFee = outputFee.multiply(BigInteger.valueOf(outputsCount));
    BigInteger thirdExpectedNumberOfInputs = BigInteger.valueOf(37);
    BigInteger thirdExpectedInputFee = inputFee.multiply(thirdExpectedNumberOfInputs);
    BigInteger thirdExpectedTotalFee = thirdExpectedTxFee.add(thirdExpectedOutPutFee).add(thirdExpectedInputFee);
    Assert.assertEquals(thirdExpectedTotalFee, fee);
  }

  @Test(expected = NullPointerException.class)
  public void getTransferableAmount_nullUnspent() throws Exception {
    BigInteger balance = UTXOSelector.getTransferableAmount(/* unspent= */ null, /* txFee=
     */BigInteger.ZERO, /* inputFee= */ BigInteger.ZERO, /* outputFee= */ BigInteger.ZERO);
  }

  @Test
  public void getTransferableAmount_emptyUnspent_returnsZeroBalance() throws Exception {
    BigInteger balance = UTXOSelector.getTransferableAmount(/* unspent= */ new HashSet<>(), /*
    txFee=
     */BigInteger.ZERO, /* inputFee= */ BigInteger.ZERO, /* outputFee= */ BigInteger.ZERO);

    Assert.assertEquals(balance, BigInteger.ZERO);
  }

  @Test
  public void getTransferableAmount_zeroInputFee_zeroOutputFee_transfersSmallBalance() throws Exception {
    BigInteger txFee = BigInteger.ONE;
    BigInteger inputFee = BigInteger.ZERO;
    Amount smallAmount = new Amount(BigInteger.ONE, KnownTokenId.MOB.getId());
    Set<OwnedTxOut> txOuts = new HashSet<>();
    final int numTxOuts = 5;
    for (int i = 0; i < numTxOuts; i++) {
      txOuts.add(createMockTxOut(smallAmount));
    }

    BigInteger transferableBalance = UTXOSelector.getTransferableAmount(txOuts, txFee,
            inputFee, /* outputFee= */ BigInteger.ZERO);

    BigInteger expectedInputFee = inputFee.multiply(BigInteger.valueOf(numTxOuts));
    BigInteger expectedTotalFee = txFee.add(expectedInputFee);
    BigInteger expectedBalance =
            smallAmount.getValue().multiply(BigInteger.valueOf(numTxOuts)).subtract(expectedTotalFee);
    Assert.assertEquals(expectedBalance, transferableBalance);
  }

  @Test
  public void getTransferableAmount_nonZeroInputFee_nonZeroOutputFee_transfersLargerBalance() throws Exception {
    BigInteger txFee = BigInteger.ONE;
    BigInteger inputFee = BigInteger.ONE;
    Amount largerAmount = new Amount(BigInteger.valueOf(100), KnownTokenId.MOB.getId());
    Set<OwnedTxOut> txOuts = new HashSet<>();
    final int numTxOuts = 5;
    for (int i = 0; i < numTxOuts; i++) {
      txOuts.add(createMockTxOut(largerAmount));
    }

    BigInteger transferableBalance = UTXOSelector.getTransferableAmount(txOuts, txFee,
            inputFee, /* outputFee= */ BigInteger.valueOf(2));

    BigInteger expectedInputFee = inputFee.multiply(BigInteger.valueOf(numTxOuts));
    BigInteger expectedTotalFee = txFee.add(expectedInputFee);
    BigInteger expectedBalance =
            largerAmount.getValue().multiply(BigInteger.valueOf(numTxOuts)).subtract(expectedTotalFee);
    Assert.assertEquals(expectedBalance, transferableBalance);
  }

  @Test
  public void getTransferableAmount_fragmented_transfersBalance() throws Exception {
    BigInteger txFee = BigInteger.ONE;
    BigInteger inputFee = BigInteger.ZERO;
    Amount smallAmount = new Amount(BigInteger.ONE, KnownTokenId.MOB.getId());
    Set<OwnedTxOut> txOuts = new HashSet<>();
    final int numTxOuts = UTXOSelector.MAX_INPUTS + 1;
    for (int i = 0; i < numTxOuts; i++) {
      txOuts.add(createMockTxOut(smallAmount));
    }

    BigInteger transferableBalance = UTXOSelector.getTransferableAmount(txOuts, txFee,
            inputFee, /* outputFee= */ BigInteger.ONE);

    BigInteger expectedTotalFee = txFee.multiply(BigInteger.valueOf(2));
    Assert.assertEquals(smallAmount.getValue().multiply(BigInteger.valueOf(numTxOuts)).subtract(expectedTotalFee), transferableBalance);
  }

  @Test
  public void getTransferableAmount_multiLevelFragmentedBalance_transfersBalance() throws Exception {
    BigInteger txFee = BigInteger.ONE;
    BigInteger inputFee = BigInteger.ZERO;
    Amount smallAmount = new Amount(BigInteger.ONE, KnownTokenId.MOB.getId());
    Set<OwnedTxOut> txOuts = new HashSet<>();
    final int numTxOuts = UTXOSelector.MAX_INPUTS * 10;
    for (int i = 0; i < numTxOuts; i++) {
      txOuts.add(createMockTxOut(smallAmount));
    }

    BigInteger transferableBalance = UTXOSelector.getTransferableAmount(txOuts, txFee,
            inputFee, /* outputFee= */ BigInteger.ZERO);

    BigInteger expectedTotalFee = txFee.multiply(BigInteger.valueOf(11));
    Assert.assertEquals(smallAmount.getValue().multiply(BigInteger.valueOf(numTxOuts).subtract(expectedTotalFee)), transferableBalance);
  }

  private static UTXOSelector.TxOutNode createMockTxOutNode(BigInteger value, BigInteger txFee,
                                                            BigInteger inputFee) {
    UTXOSelector.TxOutNode node = mock(UTXOSelector.TxOutNode.class);
    when(node.getValue()).thenReturn(value);
    when(node.getFee(txFee, inputFee)).thenReturn(inputFee);
    return node;
  }

  private static OwnedTxOut createMockTxOut(Amount amount) {
    OwnedTxOut txOut = mock(OwnedTxOut.class);
    when(txOut.getAmount()).thenReturn(amount);
    return txOut;
  }
}

