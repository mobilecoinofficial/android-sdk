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
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;
import java.math.BigInteger;

/** Enables clients to make MobileCoin transactions. */
public interface MobileCoinTransactionClient {

  /**
   * Calculate the total transferable amount excluding all the required fees for such transfer.
   */
  @NonNull
  BigInteger getTransferableAmount() throws NetworkException, InvalidFogResponse,
      AttestationException;

  /**
   * Prepares a {@link PendingTransaction} to be executed.
   *
   * @param recipient {@link PublicAddress} of the recipient
   * @param amount    transaction amount
   * @param fee       transaction fee (see {@link MobileCoinClient#estimateTotalFee})
   * @return {@link PendingTransaction} which encapsulates the {@link Transaction} and {@link
   * Receipt} objects
   */
  @NonNull
  PendingTransaction prepareTransaction(
      @NonNull final PublicAddress recipient,
      @NonNull final BigInteger amount,
      @NonNull final BigInteger fee
  ) throws InsufficientFundsException, FragmentedAccountException, FeeRejectedException,
      InvalidFogResponse, AttestationException, NetworkException,
      TransactionBuilderException, FogReportException;


  /**
   * Submits a {@link Transaction} to the consensus service.
   *
   * @param transaction a valid transaction object to submit (see {@link MobileCoinClient#prepareTransaction}}
   */
  void submitTransaction(@NonNull Transaction transaction)
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
      InvalidReceiptException;

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
      NetworkException;

  /**
   * Estimates the minimum fee required to send a transaction with the specified amount. The account balance
   * consists of multiple coins, if there are no big enough coins to successfully send the
   * transaction {@link FragmentedAccountException} will be thrown. The account needs to be
   * defragmented in order to send the specified amount. See
   * {@link MobileCoinClient#defragmentAccount}.
   *
   * @param amount an amount value in picoMob
   */
  @NonNull
  BigInteger estimateTotalFee(@NonNull BigInteger amount)
      throws InsufficientFundsException, NetworkException, InvalidFogResponse,
      AttestationException;

  /**
   * Fetches or returns the cached minimum transaction fee.
   */
  @NonNull
  public BigInteger getOrFetchMinimumTxFee() throws NetworkException;


}

