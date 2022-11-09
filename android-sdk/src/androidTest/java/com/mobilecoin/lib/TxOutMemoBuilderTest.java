package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TxOutMemoBuilderTest {

  @Test
  public void createWithSenderAndDestination_validAccountKey_constructsRTHMemoBuilder()
      throws Exception {
    AccountKey accountKey = TestKeysManager.getNextAccountKey();

    TxOutMemoBuilder txOutMemoBuilder = TxOutMemoBuilder
        .createSenderAndDestinationRTHMemoBuilder(accountKey);
  }

  @Test
  public void createDefault_constructsRTHMemoBuilder() throws Exception {
    TxOutMemoBuilder txOutMemoBuilder = TxOutMemoBuilder.createDefaultRTHMemoBuilder();
  }

  @Test
  public void createSenderPaymentRequestAndDestinationRTHMemoBuilder_validAccountKey_paymentRequestId_constructsRTHMemoBuilder()
      throws Exception {
    AccountKey accountKey = TestKeysManager.getNextAccountKey();
    UnsignedLong paymentRequestId = UnsignedLong.TEN;

    TxOutMemoBuilder txOutMemoBuilder = TxOutMemoBuilder
        .createSenderPaymentRequestAndDestinationRTHMemoBuilder(accountKey, paymentRequestId);
  }

  @Test
  public void createSenderPaymentIntentAndDestinationRTHMemoBuilder_validAccountKey_paymentIntentId_constructsRTHMemoBuilder()
          throws Exception {
    AccountKey accountKey = TestKeysManager.getNextAccountKey();
    UnsignedLong paymentIntentId = UnsignedLong.TEN;

    TxOutMemoBuilder txOutMemoBuilder = TxOutMemoBuilder
            .createSenderPaymentIntentAndDestinationRTHMemoBuilder(accountKey, paymentIntentId);
  }

}