package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Parcel;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.util.Hex;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import fog_view.View.TxOutRecord;

@RunWith(JUnit4.class)
public class OwnedTxOutTest {

  private static final String TAG = OwnedTxOutTest.class.getSimpleName();

  // Test data was constructed using Rust code from mobilecoin.git. Specifically, the tx_processing
  // test.
  private static final String senderAccountKeyHexProtoBytes = "0a220a20b1f765d30fbb85b605f04edd29bb9cbb83938f68600d4a618863e9664e7b960912220a20dae7da08e27ea4f17a233f15c234b58ce20d0d2727abb98e9bdcf04aeea540081a11666f673a2f2f6578616d706c652e636f6d";
  private static final String receiverAccountKeyHexProtoBytes = "0a220a20ff6b8ebfe4cda6a2bca7fa6061e73c752ecc3c01876a25b984f0230bcdab8b0712220a20197d2746aac53be4911b6dd01b3e67d5565fcf322c87c75add37959a608e4a021a11666f673a2f2f6578616d706c652e636f6d";
  private static final String viewRecordWithNotSetMemoHexProtoBytes = "11b89889a83748c3b71a20ea28e0a73e2e579163d8710ef1d19bafc1bd04f681168a7eed50054c7c91b45d2220f40936fb0af75ae89f632685e930a9a53abcac8665ae6a7cd59915e07f15d86e296400000000000000310100000000000000390a0000000000000045dad4f606";
  private static final String viewRecordWithUnusedMemoHexProtoBytes = "11639758694fb8292e1a20d2da037ee1c216c48c9b2742a2ea1ac7d7c29ab754f650ad160424871df5ee662220566c5eeee7236065bce4a8f6c9c70dc8f51f271527fb68114e97bd26874a963a296500000000000000310100000000000000390a0000000000000045b0ced38e4a42c39286d2a3e9c746c2cd19025d3d27c32818f23aa7280c655e794a45b2bff247a627ed203dc007bddd65139f57eeb41e9ea74dd2ffe3276e84e20c7d5f08508812e0";
  private static final String viewRecordWithSenderMemoHexProtoBytes = "11b89889a83748c3b71a20ea28e0a73e2e579163d8710ef1d19bafc1bd04f681168a7eed50054c7c91b45d2220f40936fb0af75ae89f632685e930a9a53abcac8665ae6a7cd59915e07f15d86e296400000000000000310100000000000000390a0000000000000045dad4f6064a4246597e555ff2700a08d66334a78b43f43c02a270bd580225a05f4f1bb4ca56017ab622dcdb26555c7340344a0a0499f6ee48a77a1fe9525496cd87f70d154ca2a436";
  // Contains fee of 21, number of recipients of 1, and total outlay of 472.
  private static final String viewRecordWithDestinationMemoHexProtoBytes = "11e8672b2c2a3dfdb01a20d633484d79c87c7eb43174137fb8f3ef76a903480be709aff0b4965f0f96f91222207a70b708482ad30825d12029215b0445c838d17c3300f304abc2c99354344d4c296400000000000000310100000000000000390a00000000000000450976a45c4a42be19c8919ab21ec0597c85816703535faeb208b84a01ae5fb6d708be9cf67280b2d6a2a116f93bf1895ba4c33bf1779728527ccd621271f0e67e01d1d85cf95c09d1";
  private static final UnsignedLong expectedDestinationFee = UnsignedLong.valueOf(21);
  private static final short expectedDestinationNumberOfRecipients = 1;
  private static final UnsignedLong expectedDestinationTotalOutlay = UnsignedLong.valueOf(472);
  // Contains a payment request id of 322.
  private static final String viewRecordWithSenderWithPaymentRequestMemoHexProtoBytes = "11b89889a83748c3b71a20ea28e0a73e2e579163d8710ef1d19bafc1bd04f681168a7eed50054c7c91b45d2220f40936fb0af75ae89f632685e930a9a53abcac8665ae6a7cd59915e07f15d86e296400000000000000310100000000000000390a0000000000000045dad4f6064a4246587e555ff2700a08d66334a78b43f43c02a270bd580225a11d4f1bb4ca56017ab622dcdb26555c7340344a0a0499f6ee48ea1335e6a8c4ba4424cfe8ccc523dd1e";
  private static final UnsignedLong expectedPaymentRequestId = UnsignedLong.valueOf(322);

  private AccountKey senderAccountKey;
  private AccountKey receiverAccountKey;

  @Before
  public void setUp() {
    try {
      senderAccountKey = AccountKey.fromBytes(Hex.toByteArray(
          OwnedTxOutTest.senderAccountKeyHexProtoBytes));
      receiverAccountKey = AccountKey.fromBytes(Hex.toByteArray(
          OwnedTxOutTest.receiverAccountKeyHexProtoBytes));
    } catch (Exception e) {
      Log.e(TAG,"OwnedTxOutTest exception during set up: " + e.getMessage());
    }

  }

  @Test
  public void getTxOutMemo_memoNotSet_returnsNotSetMemo() throws Exception {
    TxOutRecord txOutRecord =
        TxOutRecord.parseFrom(Hex.toByteArray(viewRecordWithNotSetMemoHexProtoBytes));
    OwnedTxOut ownedTxOut = new OwnedTxOut(txOutRecord, receiverAccountKey);

    TxOutMemo txOutMemo = ownedTxOut.getTxOutMemo();

    Assert.assertEquals(TxOutMemoType.NOT_SET, txOutMemo.getTxOutMemoType());
  }

  @Test
  public void getTxOutMemo_unusedMemo_returnsUnusedMemo() throws Exception {
    TxOutRecord txOutRecord =
        TxOutRecord.parseFrom(Hex.toByteArray(viewRecordWithUnusedMemoHexProtoBytes));
    OwnedTxOut ownedTxOut = new OwnedTxOut(txOutRecord, receiverAccountKey);

    TxOutMemo txOutMemo = ownedTxOut.getTxOutMemo();

    Assert.assertEquals(TxOutMemoType.UNUSED, txOutMemo.getTxOutMemoType());
  }

  @Test
  public void getTxOutMemo_senderMemo_returnsSenderMemo() throws Exception {
    TxOutRecord txOutRecord =
        TxOutRecord.parseFrom(Hex.toByteArray(viewRecordWithSenderMemoHexProtoBytes));
    OwnedTxOut ownedTxOut = new OwnedTxOut(txOutRecord, receiverAccountKey);

    TxOutMemo txOutMemo = ownedTxOut.getTxOutMemo();

    Assert.assertEquals(TxOutMemoType.SENDER, txOutMemo.getTxOutMemoType());
  }

  @Test
  public void getTxOutMemo_senderMemo_returnsCorrectSenderMemoData() throws Exception {
    TxOutRecord txOutRecord =
        TxOutRecord.parseFrom(Hex.toByteArray(viewRecordWithSenderMemoHexProtoBytes));
    OwnedTxOut ownedTxOut = new OwnedTxOut(txOutRecord, receiverAccountKey);

    TxOutMemo txOutMemo = ownedTxOut.getTxOutMemo();
    SenderMemoData senderMemoData = ((SenderMemo) txOutMemo)
        .getSenderMemoData(senderAccountKey.getPublicAddress(), receiverAccountKey.getDefaultSubAddressViewKey());

    AddressHash expectedAddressHash = senderAccountKey.getPublicAddress().calculateAddressHash();
    Assert.assertEquals(expectedAddressHash,senderMemoData.getAddressHash());
  }

  @Test
  public void getTxOutMemo_destinationMemo_returnsDestinationMemo() throws Exception {
    TxOutRecord txOutRecord =
        TxOutRecord.parseFrom(Hex.toByteArray(viewRecordWithDestinationMemoHexProtoBytes));
    OwnedTxOut ownedTxOut = new OwnedTxOut(txOutRecord, senderAccountKey);

    TxOutMemo txOutMemo = ownedTxOut.getTxOutMemo();

    Assert.assertEquals(TxOutMemoType.DESTINATION, txOutMemo.getTxOutMemoType());
  }

  @Test
  public void getTxOutMemo_destinationMemo_returnsCorrectDestinationMemoData() throws Exception {
    TxOutRecord txOutRecord =
        TxOutRecord.parseFrom(Hex.toByteArray(viewRecordWithDestinationMemoHexProtoBytes));
    OwnedTxOut ownedTxOut = new OwnedTxOut(txOutRecord, senderAccountKey);

    TxOutMemo txOutMemo = ownedTxOut.getTxOutMemo();
    DestinationMemoData destinationMemoData = ((DestinationMemo) txOutMemo).getDestinationMemoData();

    Assert.assertEquals(expectedDestinationFee, destinationMemoData.getFee());
    Assert.assertEquals(expectedDestinationNumberOfRecipients, destinationMemoData.getNumberOfRecipients());
    Assert.assertEquals(expectedDestinationTotalOutlay, destinationMemoData.getTotalOutlay());
  }

  @Test
  public void getTxOutMemo_senderWithPaymentRequestMemo_returnsSenderWithPaymentRequestMemo() throws Exception {
    TxOutRecord txOutRecord =
        TxOutRecord.parseFrom(Hex.toByteArray(viewRecordWithSenderWithPaymentRequestMemoHexProtoBytes));
    OwnedTxOut ownedTxOut = new OwnedTxOut(txOutRecord, receiverAccountKey);

    TxOutMemo txOutMemo = ownedTxOut.getTxOutMemo();

    Assert.assertEquals(TxOutMemoType.SENDER_WITH_PAYMENT_REQUEST, txOutMemo.getTxOutMemoType());
  }

  @Test
  public void getTxOutMemo_senderWithPaymentRequestMemo_returnsCorrectSenderWithPaymentRequestMemoData() throws Exception {
    TxOutRecord txOutRecord =
        TxOutRecord.parseFrom(Hex.toByteArray(viewRecordWithSenderWithPaymentRequestMemoHexProtoBytes));
    OwnedTxOut ownedTxOut = new OwnedTxOut(txOutRecord, receiverAccountKey);

    TxOutMemo txOutMemo = ownedTxOut.getTxOutMemo();
    SenderWithPaymentRequestMemoData senderWithPaymentRequestMemoData = ((SenderWithPaymentRequestMemo) txOutMemo)
        .getSenderWithPaymentRequestMemoData(senderAccountKey.getPublicAddress(), receiverAccountKey.getDefaultSubAddressViewKey());

    AddressHash expectedAddressHash = senderAccountKey.getPublicAddress().calculateAddressHash();
    Assert.assertEquals(expectedAddressHash, senderWithPaymentRequestMemoData.getAddressHash());
    Assert.assertEquals(expectedPaymentRequestId,senderWithPaymentRequestMemoData.getPaymentRequestId());
  }

  @Test
  public void testParcelable() throws Exception {
    MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
    AccountActivity activity = client.getAccountActivity();
    for(OwnedTxOut parcelInput : activity.getAllTokenTxOuts()) {
      Parcel parcel = Parcel.obtain();
      parcelInput.writeToParcel(parcel, 0);
      parcel.setDataPosition(0);
      OwnedTxOut parcelOutput = OwnedTxOut.CREATOR.createFromParcel(parcel);
      assertEquals(parcelInput, parcelOutput);
      parcel.recycle();
    }
  }

  @Test
  public void testVerifyCorrectCommitment() throws Exception {
    // Test parsing of valid and invalid TxOuts with no commitment data but with commitment crc32

    // Test valid with CRC32
    TxOutRecord recordWithCrc32 = TxOutRecord.parseFrom(Hex.toByteArray(viewRecordWithSenderMemoHexProtoBytes));
    TxOutRecord invalidRecordWithCrc32 = TxOutRecord.newBuilder(recordWithCrc32)
            .setTxOutAmountCommitmentDataCrc32(
                    recordWithCrc32.getTxOutAmountCommitmentDataCrc32() - 1
            ).build();
    OwnedTxOut txOutFromCrc32 = new OwnedTxOut(recordWithCrc32, receiverAccountKey);

    // Test invalid with CRC32
    try {
      OwnedTxOut txOutFromInvalidCrc32 = new OwnedTxOut(invalidRecordWithCrc32, receiverAccountKey);
      fail("Parsing of invalid record must fail");
    } catch(IllegalArgumentException e) {
      assertEquals(SerializationException.class, e.getCause().getClass());
    }

    // Test valid with commitment
    RistrettoPublic txOutSharedSecret =
            OnetimeKeys.getSharedSecret(receiverAccountKey.getViewKey(), txOutFromCrc32.getPublicKey());
    byte validCommitmentData[] = new MaskedAmountV1(
            txOutSharedSecret,
            recordWithCrc32.getTxOutAmountMaskedValue(),
            recordWithCrc32.getTxOutAmountMaskedV1TokenId().toByteArray()
    ).getCommitment();
    TxOutRecord recordWithCommitment = TxOutRecord.newBuilder(recordWithCrc32)
            .setTxOutAmountCommitmentDataCrc32(0)
            .setTxOutAmountCommitmentData(ByteString.copyFrom(validCommitmentData))
            .build();
    OwnedTxOut txOutFromCommitment = new OwnedTxOut(recordWithCommitment, receiverAccountKey);

    // Test invalid with commitment
    byte invalidCommitmentData[] = Arrays.copyOf(validCommitmentData, validCommitmentData.length);
    invalidCommitmentData[0] = (byte)(validCommitmentData[0] - 1);
    TxOutRecord recordWithInvalidCommitment = TxOutRecord.newBuilder(recordWithCrc32)
            .setTxOutAmountCommitmentDataCrc32(0)
            .setTxOutAmountCommitmentData(ByteString.copyFrom(invalidCommitmentData))
            .build();
    try {
      OwnedTxOut txOutFromInvalidCommitment = new OwnedTxOut(recordWithInvalidCommitment, receiverAccountKey);
      fail("Parsing of invalid record must fail");
    } catch(IllegalArgumentException e) {
      assertEquals(SerializationException.class, e.getCause().getClass());
    }

  }

  @Test
  public void testCopyConstructor() throws Exception {
    OwnedTxOut original = new OwnedTxOut(
            TxOutRecord.parseFrom(Hex.toByteArray(viewRecordWithSenderMemoHexProtoBytes)),
            receiverAccountKey
    );
    OwnedTxOut copy = new OwnedTxOut(original);
    assertEquals(original, copy);
    assertNotSame(original, copy);
  }

  @Test
  public void testPublicAPITxOutsCopied() throws Exception {
    OwnedTxOut originalOtxo = new OwnedTxOut(
            TxOutRecord.parseFrom(Hex.toByteArray(viewRecordWithSenderMemoHexProtoBytes)),
            receiverAccountKey
    );
    Set<OwnedTxOut> syncedTxOuts = new HashSet<OwnedTxOut>();
    syncedTxOuts.add(originalOtxo);
    TxOutStore txOutStore = mock(TxOutStore.class);
    when(txOutStore.getSyncedTxOuts()).thenReturn(syncedTxOuts);
    when(txOutStore.getCurrentBlockIndex()).thenReturn(originalOtxo.getReceivedBlockIndex());
    doNothing().when(txOutStore).refresh(any(), any(), any());
    MobileCoinClient client = new MobileCoinClient(
            null,
            txOutStore,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
    );
    AccountSnapshot snapshot = client.getAccountSnapshot();
    AccountActivity activity = snapshot.getAccountActivity();
    assertEquals(1, activity.getAllTokenTxOuts().size());// Check must pass for next line to be a valid
    OwnedTxOut copiedOtxo = activity.getAllTokenTxOuts().stream().findFirst().get();
    assertNotSame(originalOtxo, copiedOtxo);
    assertEquals(originalOtxo, copiedOtxo);
  }

  @Test
  public void copiedOwnedTxOutIntegrationTest() throws Exception {
    MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
    AccountActivity activityBefore = client.getAccountActivity();
    Set<OwnedTxOut> publicUnspentTxOutsBefore = activityBefore.getAllTokenTxOuts()
            .stream().filter(p -> !p.isSpent(client.getTxOutStore().getCurrentBlockIndex()))
            .collect(Collectors.toCollection(HashSet::new));
    Set<OwnedTxOut> privateUnspentTxOutsBefore = client.getUnspentTxOuts(TokenId.MOB);
    Amount amountToSend = Amount.ofMOB(BigInteger.TEN);
    PendingTransaction pendingTransaction = client.prepareTransaction(
            TestKeysManager.getNextAccountKey().getPublicAddress(),
            amountToSend,
            client.estimateTotalFee(amountToSend),
            TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder(client.getAccountKey())
    );
    client.submitTransaction(pendingTransaction.getTransaction());
    UtilTest.waitForTransactionStatus(client, pendingTransaction.getTransaction());
    for(OwnedTxOut otxo : publicUnspentTxOutsBefore) {
      if((otxo.getSpentBlockIndex() != null) || (otxo.getSpentBlockTimestamp() != null)) {
        fail("Unspent TxOut from old AccountActivity marked spent");
      }
    }
    for(OwnedTxOut otxo : privateUnspentTxOutsBefore) {
      if((otxo.getSpentBlockIndex() != null) && (otxo.getSpentBlockTimestamp() != null)) {
        return;// pass
      }
    }
    fail("No private API TxOuts marked spent");
  }

}

