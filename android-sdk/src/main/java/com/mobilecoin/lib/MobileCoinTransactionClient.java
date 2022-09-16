package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FeeRejectedException;
import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.FogSyncException;
import com.mobilecoin.lib.exceptions.FragmentedAccountException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidReceiptException;
import com.mobilecoin.lib.exceptions.InvalidTransactionException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.SignedContingentInputBuilderException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;

import java.math.BigInteger;

/**
 * Enables clients to make MobileCoin transactions.
 */
public interface MobileCoinTransactionClient {

  /**
   * Calculate the total transferable amount of picoMOB excluding all the required fees for such transfer.
   *
   * @deprecated Deprecated as of 1.2.0. Please use {@link MobileCoinTransactionClient#getTransferableAmount(TokenId)}
   * @see MobileCoinTransactionClient#getTransferableAmount(TokenId)
   */
  @Deprecated
  @NonNull
  BigInteger getTransferableAmount() throws NetworkException,
          InvalidFogResponse, AttestationException;

  /**
   * Calculate the total transferable amount excluding all the required fees for such transfer.
   */
  @NonNull
  Amount getTransferableAmount(@NonNull TokenId tokenId) throws NetworkException,
          InvalidFogResponse, AttestationException, FogSyncException;

  // TODO: doc
  @NonNull
  public PendingTransaction preparePresignedTransaction(
          @NonNull final SignedContingentInput presignedInput,
          @NonNull final Amount fee
  );

  /**
   * Prepares a {@link PendingTransaction} to be executed.
   *
   * @param recipient {@link PublicAddress} of the recipient
   * @param amountPicoMOB    transaction amount in picoMOB
   * @param feePicoMOB       transaction fee (see {@link MobileCoinClient#estimateTotalFee})
   * @return {@link PendingTransaction} which encapsulates the {@link Transaction} and {@link
   * Receipt} objects
   *
   * @deprecated Deprecated as of 1.2.0. Please use {@link MobileCoinTransactionClient#prepareTransaction(PublicAddress, Amount, Amount, TxOutMemoBuilder)}
   * @see MobileCoinTransactionClient#prepareTransaction(PublicAddress, Amount, Amount, TxOutMemoBuilder)
   */
  @Deprecated
  @NonNull
  PendingTransaction prepareTransaction(
          @NonNull final PublicAddress recipient,
          @NonNull final BigInteger amountPicoMOB,
          @NonNull final BigInteger feePicoMOB
  ) throws InsufficientFundsException, FragmentedAccountException, FeeRejectedException,
          InvalidFogResponse, AttestationException, NetworkException,
          TransactionBuilderException, FogReportException;

  /**
   * Prepares a {@link PendingTransaction} to be executed.
   *
   * @param recipient {@link PublicAddress} of the recipient
   * @param amount    transaction amount
   * @param fee       transaction fee (see {@link MobileCoinClient#estimateTotalFee})
   * @param txOutMemoBuilder
   * @return {@link PendingTransaction} which encapsulates the {@link Transaction} and {@link
   * Receipt} objects
   */
  @NonNull
  PendingTransaction prepareTransaction(
      @NonNull final PublicAddress recipient,
      @NonNull final Amount amount,
      @NonNull final Amount fee,
      @NonNull final TxOutMemoBuilder txOutMemoBuilder
  ) throws InsufficientFundsException, FragmentedAccountException, FeeRejectedException,
          InvalidFogResponse, AttestationException, NetworkException,
          TransactionBuilderException, FogReportException, FogSyncException;

  /**
   * Prepares a {@link PendingTransaction} to be executed.
   *
   * @param recipient {@link PublicAddress} of the recipient
   * @param amount    transaction amount
   * @param fee       transaction fee (see {@link MobileCoinClient#estimateTotalFee})
   * @param txOutMemoBuilder Builder for {@link TxOutMemo}s
   * @param rng Random Number Generator for {@link TransactionBuilder}.
   * @return {@link PendingTransaction} which encapsulates the {@link Transaction} and {@link
   * Receipt} objects
   */
  @NonNull
  PendingTransaction prepareTransaction(
          @NonNull final PublicAddress recipient,
          @NonNull final Amount amount,
          @NonNull final Amount fee,
          @NonNull final TxOutMemoBuilder txOutMemoBuilder,
          @NonNull final Rng rng
  ) throws InsufficientFundsException, FragmentedAccountException, FeeRejectedException,
          InvalidFogResponse, AttestationException, NetworkException,
          TransactionBuilderException, FogReportException, FogSyncException;

  /**
   * Submits a {@link Transaction} to the consensus service.
   *
   * @param transaction a valid transaction object to submit (see {@link MobileCoinClient#prepareTransaction}}
   */
  long submitTransaction(@NonNull Transaction transaction)
      throws InvalidTransactionException, NetworkException, AttestationException;

  /**
   * Checks the status of the transaction receipt. Recipient's key is required to decode
   * verification data, hence only the recipient of the transaction can verify receipts. Sender
   * should use {@link MobileCoinClient#getTransactionStatus}.
   *
   * @param receipt provided by the transaction sender to the recipient
   * @return {@link Receipt.Status}
   */
  @NonNull
  Receipt.Status getReceiptStatus(@NonNull Receipt receipt)
      throws InvalidFogResponse, NetworkException, AttestationException,
      InvalidReceiptException, FogSyncException;

  /**
   * Checks the status of the {@link Transaction}. Sender's key is required to decode verification
   * data, hence only the sender of the transaction can verify it's status. Recipients should use
   * {@link MobileCoinClient#getReceiptStatus}.
   *
   * @param transaction obtained from {@link MobileCoinClient#prepareTransaction}
   * @return {@link Transaction.Status}
   */
  @NonNull
  Transaction.Status getTransactionStatus(@NonNull Transaction transaction)
      throws InvalidFogResponse, AttestationException,
          NetworkException, FogSyncException;

  /**
   * Estimates the minimum fee required to send a transaction with the specified amount. The account
   * balance consists of multiple coins, if there are no big enough coins to successfully send the
   * transaction {@link FragmentedAccountException} will be thrown. The account needs to be
   * defragmented in order to send the specified amount. See {@link MobileCoinAccountClient#defragmentAccount}.
   *
   * @param amountPicoMOB amount to send in picoMOB
   *
   * @deprecated Deprecated as of 1.2.0. Please use {@link MobileCoinTransactionClient#estimateTotalFee(Amount)}
   * @see MobileCoinTransactionClient#estimateTotalFee(Amount)
   */
  @Deprecated
  @NonNull
  BigInteger estimateTotalFee(@NonNull BigInteger amountPicoMOB)
          throws InsufficientFundsException, NetworkException, InvalidFogResponse,
          AttestationException;

  /**
   * Estimates the minimum fee required to send a transaction with the specified amount. The account
   * balance consists of multiple coins, if there are no big enough coins to successfully send the
   * transaction {@link FragmentedAccountException} will be thrown. The account needs to be
   * defragmented in order to send the specified amount. See {@link MobileCoinAccountClient#defragmentAccount}.
   *
   * @param amount amount to send
   */
  @NonNull
  Amount estimateTotalFee(@NonNull Amount amount)
      throws InsufficientFundsException, NetworkException, InvalidFogResponse,
      AttestationException, FogSyncException;

  /**
   * Fetches or returns the cached minimum MOB transaction fee in picoMOB
   *
   * @deprecated Deprecated as of 1.2.0. Please use {@link MobileCoinTransactionClient#getOrFetchMinimumTxFee(TokenId)}
   * @see MobileCoinTransactionClient#getOrFetchMinimumTxFee(TokenId)
   */
  @Deprecated
  @NonNull
  BigInteger getOrFetchMinimumTxFee() throws NetworkException;

  /**
   * Fetches or returns the cached minimum transaction fee.
   */
  @NonNull
  Amount getOrFetchMinimumTxFee(@NonNull TokenId tokenId) throws NetworkException;

}

