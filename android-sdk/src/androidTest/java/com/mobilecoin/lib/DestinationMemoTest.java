package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.mobilecoin.lib.util.Hex;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DestinationMemoTest {

  private final String senderAccountKeyHexProtoBytes = "0a220a20b1f765d30fbb85b605f04edd29bb9cbb83938f68600d4a618863e9664e7b960912220a20dae7da08e27ea4f17a233f15c234b58ce20d0d2727abb98e9bdcf04aeea540081a11666f673a2f2f6578616d706c652e636f6d";
  private final String recipientAccountKeyHexProtoBytes = "0a220a20ff6b8ebfe4cda6a2bca7fa6061e73c752ecc3c01876a25b984f0230bcdab8b0712220a20197d2746aac53be4911b6dd01b3e67d5565fcf322c87c75add37959a608e4a021a11666f673a2f2f6578616d706c652e636f6d";
  private final String validMemoDataHexBytes = "69783ce9f4d68a0a586072e5563cacd8010000000000001500000000000001d80000000000000000000000000000000000000000000000000000000000000000";
  private final String txOutHexProtoBytes = "0a2d0a220a20d87c9a82c8385c33b74e9a61929d5c028bfa938c9686bf4186f5100ed1060a4a11e8672b2c2a3dfdb012220a20d633484d79c87c7eb43174137fb8f3ef76a903480be709aff0b4965f0f96f9121a220a207a70b708482ad30825d12029215b0445c838d17c3300f304abc2c99354344d4c22560a540000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002a440a42be19c8919ab21ec0597c85816703535faeb208b84a01ae5fb6d708be9cf67280b2d6a2a116f93bf1895ba4c33bf1779728527ccd621271f0e67e01d1d85cf95c09d1";

  @Test(expected = IllegalArgumentException.class)
  public void create_memoDataIncorrectLength() {
    byte[] memoData = new byte[1];

    DestinationMemo.create(null, null, memoData);
  }

  @Test
  public void getDestinationMemoData_validMemoData_returnsDestinationMemoData() throws Exception {
    byte[] memoData = Hex.toByteArray(validMemoDataHexBytes);

    AccountKey senderKey = AccountKey.fromBytes(Hex.toByteArray(senderAccountKeyHexProtoBytes));
    AccountKey recipientKey = AccountKey.fromBytes(Hex.toByteArray(recipientAccountKeyHexProtoBytes));

    TxOut txOut = TxOut.fromBytes(Hex.toByteArray(txOutHexProtoBytes));
    DestinationMemo destinationMemo = DestinationMemo.create(senderKey, txOut, memoData);

    DestinationMemoData destinationMemoData = destinationMemo.getDestinationMemoData();

    assertEquals(recipientKey.getPublicAddress().calculateAddressHash(), destinationMemoData.getAddressHash());
    // The default value for this field is 1. The rust code that generated this test data used the
    // default value.
    assertEquals(1, destinationMemoData.getNumberOfRecipients());
    assertEquals(UnsignedLong.valueOf(21), destinationMemoData.getFee());
    assertEquals(UnsignedLong.valueOf(472), destinationMemoData.getTotalOutlay());
  }

  @Test
  public void testParcelable() throws Exception {
    byte[] memoData = Hex.toByteArray(validMemoDataHexBytes);
    AccountKey accountKey = AccountKey.fromBytes(Hex.toByteArray(senderAccountKeyHexProtoBytes));
    TxOut txOut = TxOut.fromBytes(Hex.toByteArray(txOutHexProtoBytes));
    DestinationMemo destinationMemo = DestinationMemo.create(accountKey, txOut, memoData);
    Parcel parcel = Parcel.obtain();
    destinationMemo.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    assertEquals(destinationMemo, DestinationMemo.CREATOR.createFromParcel(parcel));
    parcel.recycle();
  }

}