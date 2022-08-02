package com.mobilecoin.lib;

import static com.mobilecoin.lib.TokenId.MOB;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.mobilecoin.lib.exceptions.AmountDecoderException;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FeeRejectedException;
import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.FogSyncException;
import com.mobilecoin.lib.exceptions.FragmentedAccountException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidReceiptException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fog_ledger.Ledger;

@RunWith(RobolectricTestRunner.class)
public class AccountSnapshotTests {

    private final KeyImage keyImage1 = mock(KeyImage.class);
    private final KeyImage keyImage2 = mock(KeyImage.class);
    private final BigInteger amountTen = BigInteger.TEN;
    private final BigInteger amountZero = BigInteger.ZERO;
    private final BigInteger amountOne = BigInteger.ONE;
    private final UnsignedLong blockIndexOne = UnsignedLong.ONE;
    private final UnsignedLong blockIndexZero = UnsignedLong.ZERO;
    private final UnsignedLong blockIndexTen = UnsignedLong.TEN;
    private static final Amount TEN_MOB = createAmountMOB(BigInteger.TEN);
    private static final Amount ONE_MOB = createAmountMOB(BigInteger.ONE);

    @Test
    public void test_balance() {
        Set<OwnedTxOut> txOuts = createMockTxOut();

        AccountSnapshot accountSnapshot = new AccountSnapshot(mock(MobileCoinClient.class), txOuts, blockIndexOne);

        assertEquals(new Balance(amountTen, blockIndexOne), accountSnapshot.getBalance(MOB));
    }

    @Test
    public void retrieve_block_index() {
        Set<OwnedTxOut> txOuts = createMockTxOut();

        AccountSnapshot accountSnapshot = new AccountSnapshot(mock(MobileCoinClient.class), txOuts, blockIndexOne);

        assertEquals(blockIndexOne, accountSnapshot.getBlockIndex());
    }

    @Test
    public void transaction_receipts_unknown_status() throws InvalidReceiptException {
        Set<OwnedTxOut> txOuts = createMockTxOut();
        Receipt receipt = createMockReceipt(blockIndexTen);

        AccountSnapshot accountSnapshot = new AccountSnapshot(mock(MobileCoinClient.class), txOuts, blockIndexOne);

        assertEquals(Receipt.Status.UNKNOWN, accountSnapshot.getReceiptStatus(receipt));
        assertEquals(blockIndexOne, accountSnapshot.getReceiptStatus(receipt).getBlockIndex());
    }

    @Test
    public void transaction_receipts_failed_status() throws InvalidReceiptException {
        Set<OwnedTxOut> txOuts = createMockTxOut();
        Receipt receipt = createMockReceipt(blockIndexOne);

        AccountSnapshot accountSnapshot = new AccountSnapshot(mock(MobileCoinClient.class), txOuts, blockIndexOne);
        assertEquals(Receipt.Status.FAILED, accountSnapshot.getReceiptStatus(receipt));
        assertEquals(blockIndexOne, accountSnapshot.getReceiptStatus(receipt).getBlockIndex());
    }

    @Test
    public void transaction_receipts_received_status() throws InvalidReceiptException, AmountDecoderException {
        Set<OwnedTxOut> txOuts = new HashSet<>();
        OwnedTxOut txOut = mock(OwnedTxOut.class);
        when(txOut.getAmount()).thenReturn(new Amount(amountTen, MOB));
        RistrettoPublic ristrettoPublic = mock(RistrettoPublic.class);
        when(txOut.getPublicKey()).thenReturn(ristrettoPublic);
        when(txOut.getReceivedBlockIndex()).thenReturn(blockIndexOne);
        txOuts.add(txOut);
        Receipt receipt = mock(Receipt.class);
        when(receipt.getPublicKey()).thenReturn(ristrettoPublic);
        when(receipt.getAmountData(any())).thenReturn(new Amount(amountTen, MOB));
        when(receipt.getTombstoneBlockIndex()).thenReturn(blockIndexOne);

        AccountSnapshot accountSnapshot = new AccountSnapshot(mock(MobileCoinClient.class), txOuts, blockIndexOne);

        assertEquals(Receipt.Status.RECEIVED, accountSnapshot.getReceiptStatus(receipt));
        assertEquals(blockIndexOne, accountSnapshot.getReceiptStatus(receipt).getBlockIndex());
    }

    @Test
    public void transaction_receipt_unknown_status() throws InvalidReceiptException {
        Receipt receipt = createMockReceipt(blockIndexOne);

        AccountSnapshot accountSnapshot = new AccountSnapshot(mock(MobileCoinClient.class), new HashSet<>(), blockIndexZero);

        assertEquals(Receipt.Status.UNKNOWN, accountSnapshot.getReceiptStatus(receipt));
        assertEquals(blockIndexZero, accountSnapshot.getReceiptStatus(receipt).getBlockIndex());
    }

    @Test
    public void transferable_amount() {
        Set<OwnedTxOut> txOuts = createMockTxOuts(false);

        AccountSnapshot accountSnapshot = new AccountSnapshot(mock(MobileCoinClient.class), txOuts, blockIndexOne);

        assertEquals(amountTen, accountSnapshot.getTransferableAmount(amountOne));
    }

    @Test
    public void transferable_amount_zero() {
        Set<OwnedTxOut> txOuts = createMockTxOuts(true);

        AccountSnapshot accountSnapshot = new AccountSnapshot(mock(MobileCoinClient.class), txOuts, blockIndexOne);

        assertEquals(amountZero, accountSnapshot.getTransferableAmount(amountOne));
    }

    @Test
    public void estimate_total_fee() throws InsufficientFundsException {
        Set<OwnedTxOut> txOuts = createMockTxOuts(false);

        AccountSnapshot accountSnapshot = new AccountSnapshot(mock(MobileCoinClient.class), txOuts, blockIndexOne);

        assertEquals(amountOne, accountSnapshot.estimateTotalFee(amountTen, amountOne));
    }

    @Test
    public void estimate_total_fees() throws InsufficientFundsException {
        Set<OwnedTxOut> txOuts = createMockTxOuts(false);
        try (MockedStatic<UTXOSelector> selector = mockStatic(UTXOSelector.class)) {
            selector.when(() -> UTXOSelector.calculateFee(any(), any(), any(), any(), any(), anyInt())).thenReturn(amountOne);

            AccountSnapshot accountSnapshot = new AccountSnapshot(mock(MobileCoinClient.class), txOuts, blockIndexOne);

            assertEquals(createAmountMOB(amountOne), accountSnapshot.estimateTotalFee(TEN_MOB, ONE_MOB));
        }

    }

    @Test
    public void prepare_transaction() throws InsufficientFundsException, FragmentedAccountException, AttestationException, FogReportException, FeeRejectedException, FogSyncException, InvalidFogResponse, NetworkException, TransactionBuilderException {
        Set<OwnedTxOut> txOuts = createMockTxOuts(false);
        MobileCoinClient client = mock(MobileCoinClient.class);
        PublicAddress publicAddress = mock(PublicAddress.class);
        TxOutMemoBuilder memoBuilder = mock(TxOutMemoBuilder.class);
        PendingTransaction expectedTransaction = mock(PendingTransaction.class);
        ChaCha20Rng rng = mock(ChaCha20Rng.class);
        when(rng.nextBytes(32)).thenReturn(new byte[32]);
        when(client.prepareTransaction(eq(publicAddress), eq(TEN_MOB), any(), eq(ONE_MOB), eq(memoBuilder), any())).thenReturn(expectedTransaction);

        AccountSnapshot accountSnapshot = new AccountSnapshot(client, txOuts, blockIndexOne);

        assertEquals(expectedTransaction, accountSnapshot.prepareTransaction(publicAddress, TEN_MOB, ONE_MOB, memoBuilder, rng));
    }

    @Test
    public void transaction_status_unknown() throws NetworkException {
        Transaction transaction = createMockTransaction();
        Set<OwnedTxOut> txOuts = createMockTxOut();
        MobileCoinClient client = mock(MobileCoinClient.class);

        AccountSnapshot accountSnapshot = new AccountSnapshot(client, txOuts, blockIndexOne);

        assertEquals(Transaction.Status.UNKNOWN, accountSnapshot.getTransactionStatus(transaction));
    }

    @Test
    public void transaction_status_failed() throws NetworkException {
        Transaction transaction = createMockTransaction();
        Set<OwnedTxOut> txOuts = createMockTxOut();
        MobileCoinClient client = mock(MobileCoinClient.class);

        AccountSnapshot accountSnapshot = new AccountSnapshot(client, txOuts, blockIndexTen);

        assertEquals(Transaction.Status.FAILED, accountSnapshot.getTransactionStatus(transaction));
    }

    @Test
    public void transaction_status_accepted() throws NetworkException {
        Transaction transaction = createMockTransaction();
        Set<OwnedTxOut> txOuts = createMockTxOutsWithKeyImage(true);
        MobileCoinClient client = mock(MobileCoinClient.class);
        FogUntrustedClient untrustedClient = mock(FogUntrustedClient.class);
        when(client.getUntrustedClient()).thenReturn(untrustedClient);
        Ledger.TxOutResponse txOutResponse = mock(Ledger.TxOutResponse.class);
        when(untrustedClient.fetchTxOuts(any())).thenReturn(txOutResponse);
        List<Ledger.TxOutResult> results = new ArrayList<>();
        Ledger.TxOutResult res = mock(Ledger.TxOutResult.class);
        when(res.getResultCode()).thenReturn(Ledger.TxOutResultCode.Found);
        long index = 2;
        when(res.getBlockIndex()).thenReturn(index);
        results.add(res);
        when(txOutResponse.getResultsList()).thenReturn(results);

        AccountSnapshot accountSnapshot = new AccountSnapshot(client, txOuts, blockIndexTen);

        assertEquals(Transaction.Status.ACCEPTED, accountSnapshot.getTransactionStatus(transaction));
    }

    public static Amount createAmountMOB(BigInteger amount) {
        return new Amount(amount, MOB);
    }

    public Set<OwnedTxOut> createMockTxOuts(boolean isSpent) {
        Set<OwnedTxOut> txOuts = new HashSet<>();
        OwnedTxOut txOut_1 = mock(OwnedTxOut.class);
        when(txOut_1.getAmount()).thenReturn(createAmountMOB(amountTen));
        when(txOut_1.isSpent(blockIndexOne)).thenReturn(isSpent);
        OwnedTxOut txOut_2 = mock(OwnedTxOut.class);
        when(txOut_2.getAmount()).thenReturn(createAmountMOB(amountOne));
        when(txOut_2.isSpent(blockIndexOne)).thenReturn(isSpent);
        txOuts.add(txOut_1);
        txOuts.add(txOut_2);
        return txOuts;
    }

    public Set<OwnedTxOut> createMockTxOutsWithKeyImage(boolean isSpent) {
        Set<OwnedTxOut> txOuts = new HashSet<>();
        OwnedTxOut txOut_1 = mock(OwnedTxOut.class);
        when(txOut_1.getAmount()).thenReturn(TEN_MOB);
        when(txOut_1.isSpent(blockIndexTen)).thenReturn(isSpent);
        when(txOut_1.getKeyImage()).thenReturn(keyImage1);
        OwnedTxOut txOut_2 = mock(OwnedTxOut.class);
        when(txOut_2.getAmount()).thenReturn(ONE_MOB);
        when(txOut_2.isSpent(blockIndexTen)).thenReturn(isSpent);
        when(txOut_2.getKeyImage()).thenReturn(keyImage2);
        txOuts.add(txOut_1);
        txOuts.add(txOut_2);
        return txOuts;
    }

    public Set<OwnedTxOut> createMockTxOut() {
        Set<OwnedTxOut> txOuts = new HashSet<>();
        OwnedTxOut txOut = mock(OwnedTxOut.class);
        when(txOut.getAmount()).thenReturn(TEN_MOB);
        RistrettoPublic ristrettoPublic = mock(RistrettoPublic.class);
        when(txOut.getPublicKey()).thenReturn(ristrettoPublic);
        when(txOut.getKeyImage()).thenReturn(keyImage1);
        txOuts.add(txOut);
        return txOuts;
    }

    public Receipt createMockReceipt(UnsignedLong tomstoneBlockIndex) {
        Receipt receipt = mock(Receipt.class);
        RistrettoPublic ristrettoPublic = mock(RistrettoPublic.class);
        when(receipt.getPublicKey()).thenReturn(ristrettoPublic);
        when(receipt.getTombstoneBlockIndex()).thenReturn(tomstoneBlockIndex);
        return receipt;
    }

    public Transaction createMockTransaction() {
        Transaction transaction = mock(Transaction.class);
        when(transaction.getKeyImages()).thenReturn(createMockKeyImage());
        long TOMBSTONE_INDEX = 3;
        when(transaction.getTombstoneBlockIndex()).thenReturn(TOMBSTONE_INDEX);
        RistrettoPublic ristrettoPublic = mock(RistrettoPublic.class);
        Set<RistrettoPublic> outputPublicKeys = new HashSet<>();
        outputPublicKeys.add(ristrettoPublic);
        when(transaction.getOutputPublicKeys()).thenReturn(outputPublicKeys);
        return transaction;
    }

    public Set<KeyImage> createMockKeyImage() {
        Set<KeyImage> keyImages = new HashSet<>();
        keyImages.add(keyImage1);
        keyImages.add(keyImage2);
        return keyImages;
    }

}
