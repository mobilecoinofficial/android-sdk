package com.mobilecoin.lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.os.Parcel;

import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.util.Hex;

import org.junit.Test;

public class SenderWithPaymentRequestMemoTest {

  // This data was generated from the rust code.
  private final String senderAccountKeyHexProtoBytes = "0a220a20ec8cb9814ac5c1a4aacbc613e756744679050927cc9e5f8772c6d649d4a5ac0612220a20e7ef0b2772663314ecd7ee92008613764ab5669666d95bd2621d99d60506cb0d1a1e666f673a2f2f666f672e616c7068612e6d6f62696c65636f696e2e636f6d2aa60430820222300d06092a864886f70d01010105000382020f003082020a0282020100c853a8724bc211cf5370ed4dbec8947c5573bed0ec47ae14211454977b41336061f0a040f77dbf529f3a46d8095676ec971b940ab4c9642578760779840a3f9b3b893b2f65006c544e9c16586d33649769b7c1c94552d7efa081a56ad612dec932812676ebec091f2aed69123604f4888a125e04ff85f5a727c286664378581cf34c7ee13eb01cc4faf3308ed3c07a9415f98e5fbfe073e6c357967244e46ba6ebbe391d8154e6e4a1c80524b1a6733eca46e37bfdd62d75816988a79aac6bdb62a06b1237a8ff5e5c848d01bbff684248cf06d92f301623c893eb0fba0f3faee2d197ea57ac428f89d6c000f76d58d5aacc3d70204781aca45bc02b1456b454231d2f2ed4ca6614e5242c7d7af0fe61e9af6ecfa76674ffbc29b858091cbfb4011538f0e894ce45d21d7fac04ba2ff57e9ff6db21e2afd9468ad785c262ec59d4a1a801c5ec2f95fc107dc9cb5f7869d70aa84450b8c350c2fa48bddef20752a1e43676b246c7f59f8f1f4aee43c1a15f36f7a36a9ec708320ea42089991551f2656ec62ea38233946b85616ff182cf17cd227e596329b546ea04d13b053be4cf3338de777b50bc6eca7a6185cf7a5022bc9be3749b1bb43e10ecc88a0c580f2b7373138ee49c7bafd8be6a64048887230480b0c85a045255494e04a9a81646369ce7a10e08da6fae27333ec0c16c8a74d93779a9e055395078d0b07286f9930203010001";
  private final String recipientAccountKeyHexProtoBytes = "0a220a20553a1c51c1e91d3105b17c909c163f8bc6faf93718deb06e5b9fdb9a24c2560912220a20db8b25545216d606fc3ff6da43d3281e862ba254193aff8c408f3564aefca5061a1e666f673a2f2f666f672e616c7068612e6d6f62696c65636f696e2e636f6d2aa60430820222300d06092a864886f70d01010105000382020f003082020a0282020100c853a8724bc211cf5370ed4dbec8947c5573bed0ec47ae14211454977b41336061f0a040f77dbf529f3a46d8095676ec971b940ab4c9642578760779840a3f9b3b893b2f65006c544e9c16586d33649769b7c1c94552d7efa081a56ad612dec932812676ebec091f2aed69123604f4888a125e04ff85f5a727c286664378581cf34c7ee13eb01cc4faf3308ed3c07a9415f98e5fbfe073e6c357967244e46ba6ebbe391d8154e6e4a1c80524b1a6733eca46e37bfdd62d75816988a79aac6bdb62a06b1237a8ff5e5c848d01bbff684248cf06d92f301623c893eb0fba0f3faee2d197ea57ac428f89d6c000f76d58d5aacc3d70204781aca45bc02b1456b454231d2f2ed4ca6614e5242c7d7af0fe61e9af6ecfa76674ffbc29b858091cbfb4011538f0e894ce45d21d7fac04ba2ff57e9ff6db21e2afd9468ad785c262ec59d4a1a801c5ec2f95fc107dc9cb5f7869d70aa84450b8c350c2fa48bddef20752a1e43676b246c7f59f8f1f4aee43c1a15f36f7a36a9ec708320ea42089991551f2656ec62ea38233946b85616ff182cf17cd227e596329b546ea04d13b053be4cf3338de777b50bc6eca7a6185cf7a5022bc9be3749b1bb43e10ecc88a0c580f2b7373138ee49c7bafd8be6a64048887230480b0c85a045255494e04a9a81646369ce7a10e08da6fae27333ec0c16c8a74d93779a9e055395078d0b07286f9930203010001";
  private final String senderPublicAddressHexProtoBytes = "0a220a20269f76626e8eaa1e466fe45f57cc100b5f9ec696ce922e34095294db2581ee7012220a202683a173c59787b013c2e0a5486c6b82b8736b3411a52caa45a293e83d79b355";
  private final String txOutPublicKeyHexProtoBytes = "0a20f64454c1e0ad6b79d0dc2c16437d6279446f15b7c601ea0467876cb04c882459";
  private final String validMemoDataHexBytes = "ccb5a98f0c0c42f68491e5e0c9362452000000000000000700000000000000000000000000000000000000000000000017a1b627bf37fdb1e49daf5b74822516";

  @Test(expected = IllegalArgumentException.class)
  public void create_memoDataIncorrectLength() {
    byte[] memoData = new byte[1];

    SenderWithPaymentRequestMemo.create(null, memoData);
  }

  @Test
  public void create_memoDataLength_createsSenderWithPaymentRequestMemo() {
    byte[] memoData = new byte[TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES];

    SenderWithPaymentRequestMemo.create(null, memoData);
  }

  @Test
  public void getSenderWithPaymentRequestMemoData_validMemoData_returnsMemoDataWithCorrectAddressHash() throws Exception {
    RistrettoPublic txOutPublicKey = createRistrettoPublic(txOutPublicKeyHexProtoBytes);
    byte[] memoData = Hex.toByteArray(validMemoDataHexBytes);
    SenderWithPaymentRequestMemo senderWithPaymentRequestMemo = SenderWithPaymentRequestMemo.create(
        txOutPublicKey,
        memoData
    );

    AccountKey senderAccountKey = AccountKey.fromBytes(Hex.toByteArray(senderAccountKeyHexProtoBytes));
    AccountKey recipientAccountKey = AccountKey.fromBytes(Hex.toByteArray(recipientAccountKeyHexProtoBytes));
    PublicAddress senderPublicAddress =
            PublicAddress.fromBytes(Hex.toByteArray(senderPublicAddressHexProtoBytes));
    SenderWithPaymentRequestMemoData senderWithPaymentRequestMemoData =
        senderWithPaymentRequestMemo.getSenderWithPaymentRequestMemoData(senderAccountKey.getPublicAddress(), recipientAccountKey.getDefaultSubAddressViewKey());

    assertNotNull(senderWithPaymentRequestMemoData);
    assertArrayEquals(
        senderWithPaymentRequestMemoData.getAddressHash().getHashData(),
        senderAccountKey.getPublicAddress().calculateAddressHash().getHashData()
    );
    // The rust code generated a memo with a payment request id of 7
    UnsignedLong expectedPaymentRequestId = UnsignedLong.valueOf(7);
    assertEquals(expectedPaymentRequestId, senderWithPaymentRequestMemoData.getPaymentRequestId());
  }

  @Test
  public void getUnvalidatedAddressHash_returnsAddressHash() {
    SenderWithPaymentRequestMemo senderMemo = SenderWithPaymentRequestMemo.create(null, new byte[TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES]);

    senderMemo.getUnvalidatedAddressHash();
  }

  @Test
  public void getUnvalidatedSenderWithPaymentRequestMemoData_returnsSenderWithPaymentRequestMemoData() {
    SenderWithPaymentRequestMemo senderMemo = SenderWithPaymentRequestMemo.create(null, new byte[TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES]);

    senderMemo.getUnvalidatedSenderWithPaymentRequestMemoData();
  }

  @Test
  public void testParcelable() throws Exception {
    RistrettoPublic txOutPublicKey = createRistrettoPublic(txOutPublicKeyHexProtoBytes);
    byte[] memoData = Hex.toByteArray(validMemoDataHexBytes);
    SenderWithPaymentRequestMemo senderWithPaymentRequestMemo = SenderWithPaymentRequestMemo.create(
            txOutPublicKey,
            memoData
    );
    Parcel parcel = Parcel.obtain();
    senderWithPaymentRequestMemo.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    assertEquals(senderWithPaymentRequestMemo, SenderWithPaymentRequestMemo.CREATOR.createFromParcel(parcel));
    parcel.recycle();
  }

  private static RistrettoPrivate createRistrettoPrivate(String hexProtoBytes) throws Exception {
    MobileCoinAPI.RistrettoPrivate ristrettoPrivateProto =
        MobileCoinAPI.RistrettoPrivate
            .parseFrom(Hex.toByteArray(hexProtoBytes));

    return RistrettoPrivate.fromBytes(ristrettoPrivateProto.getData().toByteArray());
  }

  private static RistrettoPublic createRistrettoPublic(String compressedRistrettoHexBytes) throws Exception {
    MobileCoinAPI.CompressedRistretto compressedRistrettoProto =
        MobileCoinAPI.CompressedRistretto.parseFrom(Hex.toByteArray(compressedRistrettoHexBytes));

    return RistrettoPublic.fromBytes(compressedRistrettoProto.getData().toByteArray());
  }

}