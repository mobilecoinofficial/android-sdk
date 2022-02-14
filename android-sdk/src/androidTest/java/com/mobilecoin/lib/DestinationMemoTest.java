package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import android.net.Uri;
import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.InvalidTxOutMemoException;
import com.mobilecoin.lib.util.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DestinationMemoTest {

  /**
   The memo data was constructed from this Rust code.
     ```
          let account_key = AccountKey::new(
              &RistrettoPrivate::from_random(&mut rng),
              &RistrettoPrivate::from_random(&mut rng),
          );
          let account_address = account_key.default_subaddress();
          let mut memo =
            DestinationMemo::new(
               ShortAddressHash::from(&account_address),
               / total_outlay= / 12u64,
               / fee= / 13u64
            ).unwrap();
     ```
     The memo was then serialized into protobuf bytes and hex encoded.
  */
  private final String memoDestinationAddressHexProtoBytes = "0a220a20269f76626e8eaa1e466fe45f57cc100b5f9ec696ce922e34095294db2581ee7012220a202683a173c59787b013c2e0a5486c6b82b8736b3411a52caa45a293e83d79b355";
  private final String validMemoDataHexBytes = "af450111d92495617c547937ac4b69c5010000000000000d000000000000000c000000000000000000000000";
  private final String senderViewPrivateKeyHexProtoBytes = "0a20824c753f97cd96c94d707cdbb495b1cce2a8dfd0fca2a45a93543292245dae0c";
  private final String senderSpendPrivateKeyHexProtoBytes = "0a20e30f0101d5c9b2af12d8d13154122bdde6a8f05cef9dff159e48679f7655e30e";
  private final String txOutHexProtoBytes = "0a2d0a220a202601410ca893892310b7d703a33e6f27ac6e261dcaccbcb5b844225c28aa0e6011ad66f554d9863a2912220a2008752f9ea0b32d2ddadfce12717f82d0a4a14b240c25919b0b38c8aeef1bfe0f1a220a209447d95c09b10c2e681b59086857321def1bb348da7efdc04d2aae3f2902ac6e22560a540000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002a300a2ec2c53e504047d5177fcf666439a8546b4677b6b6410726a18393f245a05cc811dd29083e5b780d418443306ac057";

  @Test(expected = IllegalArgumentException.class)
  public void create_memoDataIncorrectLength() {
    byte[] memoData = new byte[1];

    DestinationMemo.create(null, null, memoData);
  }

  @Test
  public void create_memoDataLength_createsDestinationMemo() {
    byte[] memoData = new byte[TxOutMemo.TX_OUT_MEMO_DATA_SIZE_BYTES];

    DestinationMemo.create(null, null, memoData);
  }

  @Test
  public void getDestinationMemoData_validMemoData_returnsDestinationMemoData() throws Exception {
    byte[] memoData = Hex.toByteArray(validMemoDataHexBytes);
    RistrettoPrivate viewKey = createRistrettoPrivate(senderViewPrivateKeyHexProtoBytes);
    RistrettoPrivate spendKey = createRistrettoPrivate(senderSpendPrivateKeyHexProtoBytes);
    Uri fogUri = Uri.parse("fog://some-test-uri");
    TestFogConfig fogConfig = Environment.getTestFogConfig();
    AccountKey accountKey = new AccountKey(
        viewKey,
        spendKey,
        fogUri,
        fogConfig.getFogReportId(),
        fogConfig.getFogAuthoritySpki()
    );
    TxOut txOut = TxOut.fromBytes(Hex.toByteArray(txOutHexProtoBytes));
    DestinationMemo destinationMemo = DestinationMemo.create(accountKey, txOut, memoData);

    DestinationMemoData destinationMemoData = destinationMemo.getDestinationMemoData();

    // See comment above validMemoDataHexBytes field to see where these values are coming from.
    PublicAddress memoDestinationPublicAddress = PublicAddress.fromBytes(Hex.toByteArray(memoDestinationAddressHexProtoBytes));
    AddressHash expectedAddressHash = memoDestinationPublicAddress.calculateAddressHash();

    assertEquals(expectedAddressHash, destinationMemoData.getAddressHash());
    // The default value for this field is 1. The rust code that generated this test data used the
    // default value.
    assertEquals(1, destinationMemoData.getNumberOfRecipients());
    assertEquals(UnsignedLong.valueOf(13), destinationMemoData.getFee());
    assertEquals(UnsignedLong.valueOf(12), destinationMemoData.getTotalOutlay());
  }

  @Test
  public void testParcelable() throws Exception {
    byte[] memoData = Hex.toByteArray(validMemoDataHexBytes);
    RistrettoPrivate viewKey = createRistrettoPrivate(senderViewPrivateKeyHexProtoBytes);
    RistrettoPrivate spendKey = createRistrettoPrivate(senderSpendPrivateKeyHexProtoBytes);
    Uri fogUri = Uri.parse("fog://some-test-uri");
    TestFogConfig fogConfig = Environment.getTestFogConfig();
    AccountKey accountKey = new AccountKey(
            viewKey,
            spendKey,
            fogUri,
            fogConfig.getFogReportId(),
            fogConfig.getFogAuthoritySpki()
    );
    TxOut txOut = TxOut.fromBytes(Hex.toByteArray(txOutHexProtoBytes));
    DestinationMemo destinationMemo = DestinationMemo.create(accountKey, txOut, memoData);
    Parcel parcel = Parcel.obtain();
    destinationMemo.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    assertEquals(destinationMemo, DestinationMemo.CREATOR.createFromParcel(parcel));
    parcel.recycle();
  }

  private static RistrettoPrivate createRistrettoPrivate(String hexProtoBytes) throws Exception {
    MobileCoinAPI.RistrettoPrivate ristrettoPrivateProto =
        MobileCoinAPI.RistrettoPrivate
            .parseFrom(Hex.toByteArray(hexProtoBytes));

    return RistrettoPrivate.fromBytes(ristrettoPrivateProto.getData().toByteArray());
  }

}