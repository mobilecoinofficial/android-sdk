package com.mobilecoin.lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.util.Hex;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SenderMemoTest {

  // This data was generated from the rust code.
  private final String senderPublicAddressHexProtoBytes = "0a220a20269f76626e8eaa1e466fe45f57cc100b5f9ec696ce922e34095294db2581ee7012220a202683a173c59787b013c2e0a5486c6b82b8736b3411a52caa45a293e83d79b355";
  private final String senderAddressHashDataHexBytes = "af450111d92495617c547937ac4b69c5";
  private final String receiverSubaddressViewKeyHexProtoBytes = "0a203a160e2f5931213b090ad73e06cb5c90067a196ee177d4233e16982f49ea6007";
  private final String txOutPublicKeyHexProtoBytes = "0a20f64454c1e0ad6b79d0dc2c16437d6279446f15b7c601ea0467876cb04c882459";
  private final String validMemoDataHexBytes = "af450111d92495617c547937ac4b69c50000000000000000000000003425ea1bfdfbba3fd655754663cd7e30";

  @Test(expected = IllegalArgumentException.class)
  public void create_memoDataIncorrectLength() {
    byte[] memoData = new byte[1];

    SenderMemo.create(null, null, memoData);
  }

  @Test
  public void create_memoDataLength_createsSenderMemo() {
    byte[] memoData = new byte[44];

    SenderMemo.create(null, null, memoData);
  }

  @Test
  public void getSenderMemoData_validMemoData_returnsSenderMemoDataWithCorrectAddressHash() throws Exception {
    RistrettoPrivate receiverSubaddressViewKey = createRistrettoPrivate(receiverSubaddressViewKeyHexProtoBytes);
    RistrettoPublic txOutPublicKey = createRistrettoPublic(txOutPublicKeyHexProtoBytes);
    byte[] memoData = Hex.toByteArray(validMemoDataHexBytes);
    SenderMemo senderMemo = SenderMemo.create(
        receiverSubaddressViewKey,
        txOutPublicKey,
        memoData
    );

    PublicAddress senderPublicAddress =
        PublicAddress.fromBytes(Hex.toByteArray(senderPublicAddressHexProtoBytes));

    SenderMemoData senderMemoData = senderMemo.getSenderMemoData(senderPublicAddress);

    assertNotNull(senderMemoData);
    assertArrayEquals(
        senderMemoData.getAddressHash().getHashData(),
        Hex.toByteArray(senderAddressHashDataHexBytes)
    );
  }

  @Test
  public void getUnvalidatedAddressHash_returnsAddressHash() {
   SenderMemo senderMemo = SenderMemo.create(null, null, new byte[44]);

   senderMemo.getUnvalidatedAddressHash();
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