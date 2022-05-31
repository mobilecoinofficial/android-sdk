package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.FogSyncException;
import com.mobilecoin.lib.exceptions.InsufficientFundsException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidTransactionException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.exceptions.StorageNotFoundException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Handles interactions with the user's MobileCoin account.
 *
 * <p>>Makes requests to the MobileCoin blockchain to provide information regarding the account.
 */
public interface MobileCoinAccountClient {

  /**
   * Fetches the latest account snapshot.
   */
  @NonNull
  AccountSnapshot getAccountSnapshot() throws NetworkException,
      InvalidFogResponse, AttestationException, FogSyncException;

  /**
   * Creates an account snapshot for the provided block index.
   *
   * @param blockIndex is the index of the last block to include TxOuts from
   * @return {@link AccountSnapshot} or {@code null} if the {@code blockIndex} is higher than what
   * fog currently at and not equals to {@link UnsignedLong#MAX_VALUE}
   */
  @Nullable
  AccountSnapshot getAccountSnapshot(UnsignedLong blockIndex) throws NetworkException,
      InvalidFogResponse, AttestationException, FogSyncException;

  /**
   * Retrieves {@code AccountKey}'s MOB balance in picoMOB.
   */
  @NonNull
  @Deprecated
  Balance getBalance() throws AttestationException, InvalidFogResponse, NetworkException;

  /**
   * Retrieves {@code AccountKey}'s balance of a specified token.
   */
  @NonNull
  Balance getBalance(UnsignedLong tokenId) throws AttestationException, InvalidFogResponse, NetworkException, FogSyncException;

  /**
   * Retrieves {@code AccountKey}'s balance for every discovered token.
   */
  @NonNull
  Map<UnsignedLong, Balance> getBalances() throws AttestationException, InvalidFogResponse, NetworkException, FogSyncException;

  /**
   * Returns whether the defragmentation is required on the active account in order to send the
   * specified amount of picoMOB
   */
  @Deprecated
  boolean requiresDefragmentation(@NonNull BigInteger amountPicoMOB)
      throws NetworkException, InvalidFogResponse, AttestationException,
      InsufficientFundsException;

  /**
   * Returns whether the defragmentation is required on the active account in order to send the
   * specified amount
   */
  boolean requiresDefragmentation(@NonNull Amount amountToSend)
      throws NetworkException, InvalidFogResponse, AttestationException,
      InsufficientFundsException;

  /**
   * Defragments the user's account.
   *
   * <p>An account needs to be defragmented when an account balance consists of multiple coins and
   * there are no big enough coins to successfully send the transaction.
   *
   * <p>If the account is too fragmented, it might be necessary to defragment the account more than
   * once. However, wallet fragmentation is a rare occurrence since there is an internal mechanism
   * to defragment the account during other operations.
   *  @param delegate monitors and controls the defragmentation process
   */
  @Deprecated
  void defragmentAccount(
      @NonNull BigInteger amountPicoMOB,
      @NonNull DefragmentationDelegate delegate
  ) throws InvalidFogResponse, AttestationException, NetworkException, InsufficientFundsException,
          TransactionBuilderException, InvalidTransactionException,
          FogReportException, TimeoutException;

  /**
   * Defragments the user's account.
   *
   * <p>An account needs to be defragmented when an account balance consists of multiple coins and
   * there are no big enough coins to successfully send the transaction.
   *
   * <p>If the account is too fragmented, it might be necessary to defragment the account more than
   * once. However, wallet fragmentation is a rare occurrence since there is an internal mechanism
   * to defragment the account during other operations.
   *  @param delegate monitors and controls the defragmentation process
   * @param shouldWriteRTHMemos writes sender and destination memos for a defrag transaction if true.
   */
  void defragmentAccount(
      @NonNull Amount amountToSend,
      @NonNull DefragmentationDelegate delegate,
      boolean shouldWriteRTHMemos) throws InvalidFogResponse, AttestationException, NetworkException, 
      InsufficientFundsException, TransactionBuilderException, InvalidTransactionException,
      FogReportException, TimeoutException, FogSyncException;

  /**
   * Retrieves the account activity.
   */
  @NonNull
  AccountActivity getAccountActivity() throws NetworkException, InvalidFogResponse, AttestationException, FogSyncException;

  /**
   * Provides the {@link AccountKey} associated with this client instance.
   */
  @NonNull
  AccountKey getAccountKey();

  /**
   * Serializes and caches user data that's then deserialized on the next app boot.
   *
   * <p>You must provide an implementation for {@link StorageAdapter} in the {@code
   * ClientConfig}</p>
   *
   * <p>The serialized data is sensitive, so we encrypt it using a private key via Android's {@code
   * KeyStore}. Please consider adding additional layers of security.
   *
   * @throws {@link StorageNotFoundException} if {@link StorageAdapter} is not provided by the
   *                {@link ClientConfig}.
   */
  void cacheUserData()
      throws StorageNotFoundException, SerializationException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, BadPaddingException, NoSuchPaddingException, UnrecoverableEntryException, IOException;

}
