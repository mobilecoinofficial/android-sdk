package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.concurrent.TimeoutException;

/**
 * <pre>
 * The {@link MobileCoinClientImpl} class is a high-level Fog-enabled client to access MobileCoin
 * blockchain.
 *
 * Fog-enabled {@link AccountKey} is required to use {@code MobileCoinClient}.
 * </pre>
 */
public interface MobileCoinClient {

  /**
   * Fetch the latest account snapshot.
   */
  @NonNull
  AccountSnapshot getAccountSnapshot() throws NetworkException,
      InvalidFogResponse, AttestationException;

  /**
   * Create an account snapshot for the provided block index.
   *
   * @param blockIndex is the index of the last block to include TxOuts from
   * @return {@link AccountSnapshot} or {@code null} if the {@code blockIndex} is higher than what
   * fog currently at and not equals to {@link UnsignedLong#MAX_VALUE}
   */
  @Nullable
  AccountSnapshot getAccountSnapshot(UnsignedLong blockIndex) throws NetworkException,
      InvalidFogResponse, AttestationException;

  /**
   * Retrieve accountKey's balance
   */
  @NonNull
  Balance getBalance() throws InvalidFogResponse, NetworkException, AttestationException;

  /**
   * Calculate the total transferable amount excluding all the required fees for such transfer
   */
  @NonNull
  BigInteger getTransferableAmount() throws NetworkException, InvalidFogResponse,
      AttestationException;

  /**
   * @param recipient {@link PublicAddress} of the recipient
   * @param amount    transaction amount
   * @param fee       transaction fee (see {@link MobileCoinClient#estimateTotalFee})
   * @return {@link PendingTransaction} which encapsulates the {@link Transaction} and {@link
   * Receipt} objects
   */
  @NonNull
  PendingTransaction prepareTransaction(
      @NonNull PublicAddress recipient,
      @NonNull BigInteger amount,
      @NonNull BigInteger fee
  ) throws InsufficientFundsException, FragmentedAccountException, FeeRejectedException,
      InvalidFogResponse, AttestationException, NetworkException,
      TransactionBuilderException, FogReportException;

  /**
   * Submit transaction to the consensus service
   *
   * @param transaction a valid transaction object to submit (see {@link MobileCoinClient#prepareTransaction}}
   */
  void submitTransaction(@NonNull Transaction transaction)
      throws InvalidTransactionException, NetworkException, AttestationException;

  /**
   * Check the status of the transaction receipt. Recipient's key is required to decode verification
   * data, hence only the recipient of the transaction can verify receipts. Sender should use {@link
   * MobileCoinClient#getTransactionStatus}
   *
   * @param receipt provided by the transaction sender to the recipient
   * @return {@link Receipt.Status}
   */
  @NonNull
  Receipt.Status getReceiptStatus(@NonNull Receipt receipt)
      throws InvalidFogResponse, NetworkException, AttestationException,
      InvalidReceiptException;

  /**
   * Check the status of the transaction. Sender's key is required to decode verification data,
   * hence only the sender of the transaction can verify it's status. Recipients should use {@link
   * MobileCoinClient#getReceiptStatus}
   *
   * @param transaction obtained from {@link MobileCoinClient#prepareTransaction}
   * @return {@link Transaction.Status}
   */
  @NonNull
  Transaction.Status getTransactionStatus(@NonNull Transaction transaction)
      throws InvalidFogResponse, AttestationException,
      NetworkException;

  /**
   * The minimum fee required to send a transaction with the specified amount. The account balance
   * consists of multiple coins, if there are no big enough coins to successfully send the
   * transaction {@link FragmentedAccountException} will be thrown. The account needs to be
   * defragmented in order to send the specified amount. See {@link MobileCoinClient#defragmentAccount}
   *
   * @param amount an amount value in picoMob
   */
  @NonNull
  BigInteger estimateTotalFee(@NonNull BigInteger amount)
      throws InsufficientFundsException, NetworkException, InvalidFogResponse,
      AttestationException;

  /**
   * The account balance consists of multiple coins, if there are no big enough coins to
   * successfully send transaction, the account needs to be defragmented. If the account is too
   * fragmented, there may be a need to defragment the account more than once. However, wallet
   * fragmentation is a rare occurrence since there is an internal mechanism to defragment the
   * account during other operations.
   *
   * @param delegate monitors and controls the defragmentation process
   */
  void defragmentAccount(
      @NonNull BigInteger amountToSend,
      @NonNull DefragmentationDelegate delegate
  ) throws InvalidFogResponse, AttestationException, NetworkException, InsufficientFundsException,
      TransactionBuilderException, InvalidTransactionException,
      FogReportException, TimeoutException;

  /**
   * Returns whether the defragmentation is required on the active account in order to send the
   * specified amount
   */
  boolean requiresDefragmentation(@NonNull BigInteger amountToSend)
      throws NetworkException, InvalidFogResponse, AttestationException,
      InsufficientFundsException;

  /**
   * Fetch or return cached minimum transaction fee
   */
  @NonNull
  BigInteger getOrFetchMinimumTxFee() throws NetworkException;

  /**
   * Retrieve the account activity
   */
  @NonNull
  AccountActivity getAccountActivity() throws NetworkException, InvalidFogResponse,
      AttestationException;

  /**
   * AccountKey associated with this client instance.
   */
  @NonNull
  AccountKey getAccountKey();

  /**
   * Sets HTTP authorization username and password for FOG requests.
   */
  void setFogBasicAuthorization(
      @NonNull String username,
      @NonNull String password
  );

  /**
   * Sets HTTP authorization username and password for consensus server requests.
   */
  void setConsensusBasicAuthorization(@NonNull String username, @NonNull String password);

  /**
   * Attempt to gracefully shutdown internal networking and threading services This is a blocking
   * call which in rare cases may take up to 10 seconds to complete.
   */
  void shutdown();
}
