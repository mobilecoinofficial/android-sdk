package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.InvalidTransactionException;
import com.mobilecoin.lib.exceptions.NetworkException;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import consensus_common.ConsensusCommon;

public class BalanceTransferTest {

    // send multiple TxOuts from a test account to a new account to make sure it's fragmented
    // get total transferable balance on a new account
    // defragment the new account and send the entire balance back to a test account
    // the test account balance should be between 0 and the txFee
    @Test
    public void test_fragmented_balance_transfer() throws Exception {
        AccountKey testKey = TestKeysManager.getNextAccountKey();
        // make sure the account is fragmented
        int TEST_FRAGMENTS = UTXOSelector.MAX_INPUTS + 1;
        TestFogConfig fogConfig = Environment.getTestFogConfig();
        MobileCoinClient mobileCoinClient = MobileCoinClientBuilder.newBuilder()
            .setAccountKey(testKey).build();
        AccountKey accountKey = AccountKey.createNew(fogConfig.getFogUri(),
                fogConfig.getFogReportId(),
                fogConfig.getFogAuthoritySpki()
        );
        MobileCoinClient balanceAccount = MobileCoinClientBuilder.newBuilder()
            .setAccountKey(accountKey).build();
        BigInteger amount = balanceAccount.getOrFetchMinimumTxFee().multiply(BigInteger.TEN);
        for (int i = 0; i < TEST_FRAGMENTS; ++i) {
            BigInteger fee = mobileCoinClient.estimateTotalFee(amount);
            PendingTransaction pendingTransaction = mobileCoinClient.prepareTransaction(
                    accountKey.getPublicAddress(),
                    amount,
                    fee
                );
            mobileCoinClient.submitTransaction(pendingTransaction.getTransaction());
            UtilTest.waitForReceiptStatus(balanceAccount, pendingTransaction.getReceipt());
        }
        BigInteger transferableAmount = balanceAccount.getTransferableAmount();

        if (balanceAccount.requiresDefragmentation(transferableAmount)) {
            balanceAccount.defragmentAccount(transferableAmount, new DefragmentationDelegate() {
                @Override
                public void onStart() {

                }

                @Override
                public boolean onStepReady(@NonNull PendingTransaction defragStepTx,
                                           @NonNull BigInteger fee) throws NetworkException,
                        InvalidTransactionException, AttestationException {
                    balanceAccount.submitTransaction(defragStepTx.getTransaction());
                    return true;
                }

                @Override
                public DefragmentationStepResult onStepReady(@NonNull DefragmentationStep defragStep) throws NetworkException,
                        AttestationException {
                    try {
                        balanceAccount.submitTransaction(defragStep.getTransaction());
                    } catch (InvalidTransactionException e) {
                        return new DefragmentationStepResult(false, e.getResult());
                    }
                    return new DefragmentationStepResult(true, ConsensusCommon.ProposeTxResult.Ok);
                }

                @Override
                public void onComplete() {

                }

                @Override
                public void onCancel() {
                    Assert.fail("Defrag should not be cancelled in this test");
                }
            });
        }
        BigInteger totalFee = balanceAccount.estimateTotalFee(transferableAmount);
        PendingTransaction pendingTransaction =
                balanceAccount.prepareTransaction(testKey.getPublicAddress(),
                        transferableAmount,
                        totalFee
                    );
        balanceAccount.submitTransaction(pendingTransaction.getTransaction());
        UtilTest.waitForReceiptStatus(mobileCoinClient, pendingTransaction.getReceipt());

        Assert.assertTrue(balanceAccount.getBalance().getAmountPicoMob()
                .compareTo(balanceAccount.getOrFetchMinimumTxFee()) < 0);

    }

}

