package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.TransactionBuilderException;

/**
 * Builds memos for TxOuts.
 *
 * <p>Provides implementations for the MemoBuilder Rust trait.
 *
 * @see <a href="https://github.com/mobilecoinfoundation/mobilecoin/blob/master/transaction/std/src/memo_builder/mod.rs">MemoBuilder</a>.
 **/
public class TxOutMemoBuilder extends Native {

  private static final String TAG = TxOutMemoBuilder.class.getName();

  /**
   * Creates an {@link TxOutMemoBuilder} with the sender and destination RTH memos enabled.
   */
  public static TxOutMemoBuilder createSenderAndDestinationRTHMemoBuilder(AccountKey accountKey)
      throws TransactionBuilderException {
    return new TxOutMemoBuilder(accountKey);
  }

  /**
   * Creates a {@link TxOutMemoBuilder} that will build a {@link SenderWithPaymentRequestMemo} for a
   * {@link Transaction} recipient and a {@link DestinationWithPaymentRequestMemo} for the sender.
   *
   * This field uniquely identifies a <strong>payment request</strong>. This ID is not guaranteed to be unique
   * on the blockchain. Any value can be selected for this field. Clients should, however, select this field in such
   * a way as to keep it unique at minimum for a pair of users. Whatever value is chosen, it should be agreed upon by
   * both the sender and recipient outside of the blockchain in order to be useful.
   *
   * @see SenderWithPaymentRequestMemo
   * @see SenderWithPaymentRequestMemoData
   * @see SenderWithPaymentRequestMemoData#getPaymentRequestId()
   * @see DestinationWithPaymentRequestMemo
   * @see DestinationWithPaymentRequestMemoData
   * @see DestinationWithPaymentRequestMemoData#getPaymentRequestId()
   * @since 1.2.0
   **/
  public static TxOutMemoBuilder createSenderPaymentRequestAndDestinationRTHMemoBuilder(AccountKey accountKey, UnsignedLong paymentRequestId) throws TransactionBuilderException {
    return new TxOutMemoBuilder(accountKey, paymentRequestId, false);
  }

  /**
   * Creates a {@link TxOutMemoBuilder} that will build a {@link SenderWithPaymentIntentMemo} for a
   * {@link Transaction} recipient and a {@link DestinationWithPaymentIntentMemo} for the sender.
   *
   * This field uniquely identifies a <strong>payment intent</strong>. This ID is not guaranteed to be unique
   * on the blockchain. Any value can be selected for this field. Clients should, however, select this field in such
   * a way as to keep it unique at minimum for a pair of users. Whatever value is chosen, it should be agreed upon by
   * both the sender and recipient outside of the blockchain in order to be useful.
   *
   * @see SenderWithPaymentIntentMemo
   * @see SenderWithPaymentIntentMemoData
   * @see SenderWithPaymentIntentMemoData#getPaymentIntentId()
   * @see DestinationWithPaymentIntentMemo
   * @see DestinationWithPaymentIntentMemoData
   * @see DestinationWithPaymentIntentMemoData#getPaymentIntentId()
   * @since 2.0.0
   **/
  public static TxOutMemoBuilder createSenderPaymentIntentAndDestinationRTHMemoBuilder(AccountKey accountKey, UnsignedLong paymentIntentId) throws TransactionBuilderException {
    return new TxOutMemoBuilder(accountKey, paymentIntentId, true);
  }

   /**
   * Creates an {@link TxOutMemoBuilder} that writes the default RTH memo, which does not contain a
   * sender memo or destination memo and is empty.
   **/
  public static TxOutMemoBuilder createDefaultRTHMemoBuilder() throws TransactionBuilderException {
    return new TxOutMemoBuilder();
  }

  private TxOutMemoBuilder(@NonNull AccountKey accountKey, UnsignedLong paymentId, boolean isPaymentIntent) throws TransactionBuilderException {
    try {
      if(isPaymentIntent) {
        init_jni_with_sender_payment_intent_and_destination_rth_memo(
                accountKey,
                paymentId.longValue()
        );
      }
      else {
        init_jni_with_sender_payment_request_and_destination_rth_memo(
                accountKey,
                paymentId.longValue()
        );
      }
    } catch (Exception exception) {
      throw new TransactionBuilderException("Unable to create TxOutMemoBuilder", exception);
    }
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

  private native void init_jni_with_sender_payment_request_and_destination_rth_memo(@NonNull AccountKey accountKey, long paymentRequestId);

  private native void init_jni_with_sender_payment_intent_and_destination_rth_memo(@NonNull AccountKey accountKey, long paymentIntentId);

  private native void init_jni_with_default_rth_memo();

}
