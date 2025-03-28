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
import com.mobilecoin.lib.exceptions.SerializationException;
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

  /**
   * Creates a {@link SignedContingentInput} to swap the two provided {@link Amount}s.
   * Most of the time, the recipient of the contingent amountToReceive will be the user building the
   * {@link SignedContingentInput}. In such cases, {@link MobileCoinTransactionClient#createSignedContingentInput(Amount, Amount)}
   * can be used. This method is provided for cases where the user providing the reward would like the
   * amountToReceive to be sent to a different wallet.
   *
   * This functionality is only supported on networks with block version 3 or higher
   *
   * @param amountToSend the {@link Amount} that is provided by the client calling this method contingent on the amountToReceive being provided
   * @param amountToReceive the {@link Amount} that will be sent from the fulfilling party to the provided {@link PublicAddress}
   * @param recipientPublicAddress the {@link PublicAddress} that the contingent amountToReceive will be sent to
   * @return a {@link SignedContingentInput} to swap the two provided {@link Amount}s
   * @throws InsufficientFundsException if this {@link MobileCoinTransactionClient} does not have sufficient {@link Balance} to provide amountToSend
   * @throws NetworkException if there is an error reaching or authenticating with the MobileCoin network
   * @throws FogReportException if there is an error fetching info from MobileCoin Fog
   * @throws FragmentedAccountException if this {@link MobileCoinTransactionClient} is too fragmented to provide amountToSend
   * @throws AttestationException if attestation fails
   * @throws InvalidFogResponse if invalid data is received from MobileCoin fog
   * @throws TransactionBuilderException if this {@link MobileCoinTransactionClient} fails to create credentials to sign the {@link SignedContingentInput}
   * @throws SignedContingentInputBuilderException if attempting to build the {@link SignedContingentInput} from invalid data
   * @throws FogSyncException if, due to temporary network state, the {@link Balance} of this {@link MobileCoinTransactionClient} cannot be verified
   * @see SignedContingentInput
   * @see MobileCoinTransactionClient#prepareTransaction(SignedContingentInput, Amount)
   * @see MobileCoinTransactionClient#prepareTransaction(SignedContingentInput, Amount, Rng)
   * @see MobileCoinTransactionClient#cancelSignedContingentInput(SignedContingentInput, Amount)
   * @see Amount
   * @see Balance
   * @since 1.3.0
   */
  @NonNull
  SignedContingentInput createSignedContingentInput(
          @NonNull final Amount amountToSend,
          @NonNull final Amount amountToReceive,
          @NonNull final PublicAddress recipientPublicAddress
  ) throws InsufficientFundsException, NetworkException, FogReportException, FragmentedAccountException,
          AttestationException, InvalidFogResponse, TransactionBuilderException, SignedContingentInputBuilderException, FogSyncException;

  /**
   * Creates a {@link SignedContingentInput} to swap the two provided {@link Amount}s.
   *
   * This functionality is only supported on networks with block version 3 or higher
   *
   * @param amountToSend the {@link Amount} that is provided by the client calling this method contingent on the amountToReceive being provided
   * @param amountToReceive the {@link Amount} that will be sent from the fulfilling party to this {@link MobileCoinTransactionClient}
   * @return a {@link SignedContingentInput} to swap the two provided {@link Amount}s
   * @throws InsufficientFundsException if this {@link MobileCoinTransactionClient} does not have sufficient {@link Balance} to provide amountToSend
   * @throws NetworkException if there is an error reaching or authenticating with the MobileCoin network
   * @throws FogReportException if there is an error fetching info from MobileCoin Fog
   * @throws FragmentedAccountException if this {@link MobileCoinTransactionClient} is too fragmented to provide amountToSend
   * @throws AttestationException if attestation fails
   * @throws InvalidFogResponse if invalid data is received from MobileCoin Fog
   * @throws TransactionBuilderException if this {@link MobileCoinTransactionClient} fails to create credentials to sign the {@link SignedContingentInput}
   * @throws SignedContingentInputBuilderException if attempting to build the {@link SignedContingentInput} from invalid data
   * @throws FogSyncException if, due to temporary network state, the {@link Balance} of this {@link MobileCoinTransactionClient} cannot be verified
   * @see SignedContingentInput
   * @see MobileCoinTransactionClient#prepareTransaction(SignedContingentInput, Amount)
   * @see MobileCoinTransactionClient#prepareTransaction(SignedContingentInput, Amount, Rng)
   * @see MobileCoinTransactionClient#cancelSignedContingentInput(SignedContingentInput, Amount)
   * @see Amount
   * @see Balance
   * @since 1.3.0
   */
  @NonNull
  SignedContingentInput createSignedContingentInput(
          @NonNull final Amount amountToSend,
          @NonNull final Amount amountToReceive
  ) throws InsufficientFundsException, NetworkException, FogReportException, FragmentedAccountException,
          AttestationException, InvalidFogResponse, TransactionBuilderException, SignedContingentInputBuilderException, FogSyncException;

  /**
   * Cancels the provided {@link SignedContingentInput} so that it cannot be fulfilled.
   * This is accomplished by creating a new {@link Transaction} to spend the input that was previously
   * signed. This can fail if the {@link SignedContingentInput} was already fulfilled, if the user
   * attempting to cancel the {@link SignedContingentInput} is not the user who created it, or if
   * there is a network issue.
   *
   * This functionality is only supported on networks with block version 3 or higher
   *
   * @param presignedInput the {@link SignedContingentInput} to cancel
   * @param fee the {@link Transaction} fee that must be paid to process the cancelation {@link Transaction}
   * @return the result of the cancelation {@link Transaction}
   * @throws SerializationException if the {@link SignedContingentInput} does not contain valid data
   * @throws NetworkException if there is an error reaching or authenticating with the MobileCoin network
   * @throws TransactionBuilderException if the cancelation {@link Transaction} cannot be built
   * @throws AttestationException if attestation fails
   * @throws FogReportException if there is an error fetching info from MobileCoin Fog
   * @throws InvalidFogResponse if invalid data is received from MobileCoin Fog
   * @throws FogSyncException if, due to a temporary network state, the account cannot be verified as being up-to-date
   * @see SignedContingentInput.CancelationResult
   * @see SignedContingentInput
   * @since 1.3.0
   */
  @NonNull
  SignedContingentInput.CancelationResult cancelSignedContingentInput(
          @NonNull final SignedContingentInput presignedInput,
          @NonNull final Amount fee
  ) throws SerializationException, NetworkException, TransactionBuilderException, AttestationException, FogReportException, InvalidFogResponse, FogSyncException;

  /**
   * Creates an unspendable {@link SignedContingentInput} that can be used to prove that the client is in possession of
   * an unspent TxOut that is identified by a given TxOut public key.
   *
   * This works because the {@link SignedContingentInput} contains both the transaction amount and its keyimage, as well as
   * an MLSAG signature that proves their validity.
   */
  @NonNull
  SignedContingentInput createProofOfReserveSignedContingentInput(
        @NonNull byte[] txOutPublicKeyBytes
  ) throws SerializationException, SignedContingentInputBuilderException, NetworkException, FogReportException, InvalidFogResponse, TransactionBuilderException, AttestationException;

  /**
   * Creates a {@link Transaction} to fulfill the provided {@link SignedContingentInput}. The resultant
   * {@link Transaction} can be submitted using {@link MobileCoinTransactionClient#submitTransaction(Transaction)}.
   * To process the {@link Transaction}, a small fee must be paid. The fee is subtracted from the reward
   * amount of the {@link SignedContingentInput} (see {@link SignedContingentInput#getRewardAmount()}).
   * If the current network fee is more than the reward {@link Amount}, the {@link Transaction} cannot be built.
   * It is recommended to fetch the fee using {@link MobileCoinTransactionClient#getOrFetchMinimumTxFee(TokenId)}
   * and the {@link TokenId} of the reward {@link Amount}.
   * Upon completing the {@link Transaction}, the {@link SignedContingentInput} required {@link Amount} will be paid
   * by this {@link MobileCoinTransactionClient} (see {@link SignedContingentInput#getRequiredAmount()}).
   *
   * This functionality is only supported on networks with block version 3 or higher
   *
   * @param presignedInput the {@link SignedContingentInput} to fulfill with the {@link Transaction}
   * @param fee the {@link Transaction} fee paid from the reward amount of this {@link SignedContingentInput}
   * @return the {@link Transaction} to submit in order to fulfill this {@link SignedContingentInput}
   * @throws TransactionBuilderException if there is an error building the {@link Transaction}
   * @throws AttestationException if attestation fails
   * @throws FogSyncException if, due to a temporary network state, the account cannot be verified as being up-to-date
   * @throws InvalidFogResponse if invalid data is received from MobileCoin Fog
   * @throws NetworkException if there is an error reaching or authenticating with the MobileCoin network
   * @throws InsufficientFundsException if this {@link MobileCoinTransactionClient} does not have access to enough funds to fulfill the {@link SignedContingentInput}
   * @throws FragmentedAccountException if this {@link MobileCoinTransactionClient} is too fragmented to fulfill the {@link SignedContingentInput}
   * @throws FogReportException if there is an error fetching info from MobileCoin Fog
   * @see SignedContingentInput#getRewardAmount()
   * @see SignedContingentInput#getRequiredAmount()
   * @see SignedContingentInput
   * @see MobileCoinTransactionClient#getOrFetchMinimumTxFee(TokenId)
   * @see MobileCoinTransactionClient#submitTransaction(Transaction)
   * @see Amount
   * @see TokenId
   * @see Transaction
   * @since 1.3.0
   */
  @NonNull
  Transaction prepareTransaction(
          @NonNull final SignedContingentInput presignedInput,
          @NonNull final Amount fee
  ) throws TransactionBuilderException, AttestationException, FogSyncException, InvalidFogResponse,
          NetworkException, InsufficientFundsException, FragmentedAccountException, FogReportException;

  /**
   * Creates a {@link Transaction} to fulfill the provided {@link SignedContingentInput}. The resultant
   * {@link Transaction} can be submitted using {@link MobileCoinTransactionClient#submitTransaction(Transaction)}.
   * To process the {@link Transaction}, a small fee must be paid. The fee is subtracted from the reward
   * amount of the {@link SignedContingentInput} (see {@link SignedContingentInput#getRewardAmount()}).
   * If the current network fee is more than the reward {@link Amount}, the {@link Transaction} cannot be built.
   * It is recommended to fetch the fee using {@link MobileCoinTransactionClient#getOrFetchMinimumTxFee(TokenId)}
   * and the {@link TokenId} of the reward {@link Amount}.
   * Upon completing the {@link Transaction}, the {@link SignedContingentInput} required {@link Amount} will be paid
   * by this {@link MobileCoinTransactionClient} (see {@link SignedContingentInput#getRequiredAmount()}).
   *
   * This functionality is only supported on networks with block version 3 or higher
   *
   * @param presignedInput the {@link SignedContingentInput} to fulfill with the {@link Transaction}
   * @param fee the {@link Transaction} fee paid from the reward amount of this {@link SignedContingentInput}
   * @param rng the {@link Rng} to use for building the {@link Transaction}
   * @return the {@link Transaction} to submit in order to fulfill this {@link SignedContingentInput}
   * @throws TransactionBuilderException if there is an error building the {@link Transaction}
   * @throws AttestationException if attestation fails
   * @throws FogSyncException if, due to a temporary network state, the account cannot be verified as being up-to-date
   * @throws InvalidFogResponse if invalid data is received from MobileCoin Fog
   * @throws NetworkException if there is an error reaching or authenticating with the MobileCoin network
   * @throws InsufficientFundsException if this {@link MobileCoinTransactionClient} does not have access to enough funds to fulfill the {@link SignedContingentInput}
   * @throws FragmentedAccountException if this {@link MobileCoinTransactionClient} is too fragmented to fulfill the {@link SignedContingentInput}
   * @throws FogReportException if there is an error fetching info from MobileCoin Fog
   * @see SignedContingentInput#getRewardAmount()
   * @see SignedContingentInput#getRequiredAmount()
   * @see SignedContingentInput
   * @see MobileCoinTransactionClient#getOrFetchMinimumTxFee(TokenId)
   * @see MobileCoinTransactionClient#submitTransaction(Transaction)
   * @see Amount
   * @see TokenId
   * @see Transaction
   * @see Rng
   * @since 1.3.0
   */
  @NonNull
  Transaction prepareTransaction(
          @NonNull final SignedContingentInput presignedInput,
          @NonNull final Amount fee,
          @NonNull final Rng rng
  ) throws TransactionBuilderException, AttestationException, FogSyncException, InvalidFogResponse,
          NetworkException, InsufficientFundsException, FragmentedAccountException, FogReportException;

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
