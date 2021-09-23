package com.mobilecoin.lib;

import android.util.Log;
import com.mobilecoin.lib.util.Hex;
import fog_view.View.TxOutRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OwnedTxOutTest {

  private static final String TAG = OwnedTxOutTest.class.getSimpleName();

  // Test data was constructed using Rust code from mobilecoin.git. Specifically, the tx_processing
  // test.
  private static final String senderAccountKeyHexProtoBytes = "0a220a20b1f765d30fbb85b605f04edd29bb9cbb83938f68600d4a618863e9664e7b960912220a20dae7da08e27ea4f17a233f15c234b58ce20d0d2727abb98e9bdcf04aeea540081a11666f673a2f2f6578616d706c652e636f6d";
  private static final String receiverAccountKeyHexProtoBytes = "0a220a20ff6b8ebfe4cda6a2bca7fa6061e73c752ecc3c01876a25b984f0230bcdab8b0712220a20197d2746aac53be4911b6dd01b3e67d5565fcf322c87c75add37959a608e4a021a11666f673a2f2f6578616d706c652e636f6d";
  private static final String viewRecordWithNotSetMemoHexProtoBytes = "11b89889a83748c3b71a20ea28e0a73e2e579163d8710ef1d19bafc1bd04f681168a7eed50054c7c91b45d2220f40936fb0af75ae89f632685e930a9a53abcac8665ae6a7cd59915e07f15d86e296400000000000000310100000000000000390a0000000000000045dad4f606";
  private static final String viewRecordWithUnusedMemoHexProtoBytes = "11b89889a83748c3b71a20ea28e0a73e2e579163d8710ef1d19bafc1bd04f681168a7eed50054c7c91b45d2220f40936fb0af75ae89f632685e930a9a53abcac8665ae6a7cd59915e07f15d86e296400000000000000310100000000000000390a0000000000000045dad4f6064a2e4759392107e63e86fc20b67fcb4f9e02b7b0a270bd580225a05f4f1bb4ca56017ab622dcdb26555c7340344a0a04";
  private static final String viewRecordWithSenderMemoHexProtoBytes = "11b89889a83748c3b71a20ea28e0a73e2e579163d8710ef1d19bafc1bd04f681168a7eed50054c7c91b45d2220f40936fb0af75ae89f632685e930a9a53abcac8665ae6a7cd59915e07f15d86e296400000000000000310100000000000000390a0000000000000045dad4f6064a2e46597e555ff2700a08d66334a78b43f43c02a270bd580225a05f4f1bb4ca24a425c93702759e66725acf947ebfaf";
  // Contains fee of 21, number of recipients of 1, and total outlay of 472.
  private static final String viewRecordWithDestinationMemoHexProtoBytes = "11e47be4931936458a1a203c682a1593b6082d0295ffcdd26838b639b81f1674aa43f9ba63034f66b98a742220a069c5a2018a6e2806c51349f7db62461a2644a79a2d689c805c007e2fd5885c296400000000000000310100000000000000390a00000000000000452874bffa4a2eb9b873de7434baa1a43df49a140d563057dc3f8831383d5a39ad06569f7dddd2d4c60454112ecd833cbee3bc75d5";
  private static final UnsignedLong expectedDestinationFee = UnsignedLong.valueOf(21);
  private static final short expectedDestinationNumberOfRecipients = 1;
  private static final UnsignedLong expectedDestinationTotalOutlay = UnsignedLong.valueOf(472);
  // Contains a payment request id of 302.
  private static final String viewRecordWithSenderWithPaymentRequestMemoHexProtoBytes = "11b89889a83748c3b71a20ea28e0a73e2e579163d8710ef1d19bafc1bd04f681168a7eed50054c7c91b45d2220f40936fb0af75ae89f632685e930a9a53abcac8665ae6a7cd59915e07f15d86e296400000000000000310100000000000000390a0000000000000045dad4f6064a2e46587e555ff2700a08d66334a78b43f43c02a270bd580225a1714f1bb4ca7abe12e46005e1e2b40d2068235f3186";
  private static final UnsignedLong expectedPaymentRequestId = UnsignedLong.valueOf(302);

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
        .getSenderMemoData(senderAccountKey.getPublicAddress());

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
        .getSenderWithPaymentRequestMemoData(senderAccountKey.getPublicAddress());

    AddressHash expectedAddressHash = senderAccountKey.getPublicAddress().calculateAddressHash();
    Assert.assertEquals(expectedAddressHash, senderWithPaymentRequestMemoData.getAddressHash());
    Assert.assertEquals(expectedPaymentRequestId,senderWithPaymentRequestMemoData.getPaymentRequestId());
  }
}