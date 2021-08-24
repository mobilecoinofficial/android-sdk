package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;

/**
 * Builds memos for TxOuts.
 *
 * <p>Provides implementations for the MemoBuilder Rust trait.
 * @see <a href="https://github.com/mobilecoinfoundation/mobilecoin/blob/master/transaction/std/src/memo_builder/mod.rs">MemoBuilder</a>.
 **/
public class TxOutMemoBuilder extends Native {
  private static final String TAG = TxOutMemoBuilder.class.getName();

  /** Creates an {@link TxOutMemoBuilder} with the sender and destination RTH memos enabled. */
  public static TxOutMemoBuilder createSenderAndDestinationRTHMemoBuilder(AccountKey accountKey) throws TransactionBuilderException {
    return new TxOutMemoBuilder(accountKey);
  }

  /** Creates an {@link TxOutMemoBuilder} that writes the dfeault RTH memo, which does not
   * contain a sender memo or destination memo, and is empty. **/
  public static TxOutMemoBuilder createDefaultRTHMemoBuilder() throws TransactionBuilderException {
    return new TxOutMemoBuilder();
  }

  private TxOutMemoBuilder(@NonNull AccountKey accountKey) throws TransactionBuilderException {
    try {
      init_jni_with_sender_and_destination_rth_memo(accountKey);
    } catch (Exception exception) {
      throw new TransactionBuilderException("Unable to create TxOutMemoBuilder", exception);
    }
  }

  private TxOutMemoBuilder() throws TransactionBuilderException {
    try {
      init_jni_with_default_rth_memo();
    } catch (Exception exception) {
      throw new TransactionBuilderException("Unable to create TxOutMemoBuilder", exception);
    }
  }

  private native void init_jni_with_sender_and_destination_rth_memo(@NonNull AccountKey accountKey);

  private native void init_jni_with_default_rth_memo();

}
