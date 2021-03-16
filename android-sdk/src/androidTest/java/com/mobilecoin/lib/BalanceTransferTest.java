package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FeeRejectedException;
import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.FragmentedAccountException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidReceiptException;
import com.mobilecoin.lib.exceptions.InvalidTransactionException;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.TimeoutException;

public class BalanceTransferTest {

    // send multiple TxOuts from a test account to a new account to make sure it's fragmented
    // get total transferable balance on a new account
    // defragment the new account and send the entire balance back to a test account
    // the test account balance should be between 0 and the txFee
    @Test
    public void test_fragmented_balance_transfer() throws InvalidUriException,
            InsufficientFundsException,
            NetworkException, InvalidFogResponse, AttestationException, FogReportException,
            TransactionBuilderException, FragmentedAccountException, FeeRejectedException,
            InvalidTransactionException, InterruptedException, TimeoutException,
            InvalidReceiptException {
        AccountKey testKey = TestKeysManager.getNextAccountKey();
        // make sure the account is fragmented
        int TEST_FRAGMENTS = UTXOSelector.MAX_INPUTS + 1;
        MobileCoinClient mobileCoinClient = Environment.makeFreshMobileCoinClient(testKey);
        AccountKey accountKey = AccountKey.createNew(Environment.FOG_URI,
                Environment.fogReportId,
                Environment.fogAuthoritySpki
        );
        MobileCoinClient balanceAccount = Environment.makeFreshMobileCoinClient(accountKey);
        BigInteger amount = MobileCoinClient.TX_FEE.multiply(BigInteger.TEN);
        for (int i = 0; i < TEST_FRAGMENTS; ++i) {
            BigInteger fee = mobileCoinClient.estimateTotalFee(amount);
            PendingTransaction pendingTransaction = mobileCoinClient.prepareTransaction(
                    accountKey.getPublicAddress(),
                    amount,
                    fee);
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
                        totalFee);
        balanceAccount.submitTransaction(pendingTransaction.getTransaction());
        UtilTest.waitForReceiptStatus(mobileCoinClient, pendingTransaction.getReceipt());

        Assert.assertTrue(balanceAccount.getBalance().getAmountPicoMob()
                .compareTo(MobileCoinClient.TX_FEE) < 0);

    }

}

