package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Parcel;

import com.google.protobuf.ByteString;
import com.mobilecoin.lib.exceptions.BadBip39EntropyException;
import com.mobilecoin.lib.exceptions.SerializationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import fog_view.View;

@RunWith(JUnit4.class)
public class OwnedTxOutTest {

    @Test
    public void testParcelable() throws SerializationException, BadBip39EntropyException {
        //TODO: Remove native dependence from this test by mocking AccountKey
        AccountTest.AccountTestData accountData = AccountTest.loadAccountTestData().get(0);
        AccountKey accountWithoutFog = AccountKeyDeriver.deriveAccountKeyFromMnemonic(
                accountData.mnemonic, accountData.accountIndex);
        View.TxOutRecord txOutRecord = View.TxOutRecord.newBuilder().setBlockIndex(322L)
                .setTimestamp(System.currentTimeMillis())
                .setTxOutAmountCommitmentData(ByteString.copyFrom(new byte[32]))
                .setTxOutAmountCommitmentDataCrc32(101)
                .setTxOutAmountMaskedValue(96L)
                .setTxOutEMemoData(ByteString.copyFrom(new byte[32]))
                .setTxOutPublicKeyData(ByteString.copyFrom(new byte[32]))
                .setTxOutTargetKeyData(ByteString.copyFrom(new byte[32]))
                .build();
        AccountKey accountKey = mock(AccountKey.class);
        when(accountKey.getViewKey()).thenReturn(RistrettoPrivate.fromBytes(new byte[32]));
        OwnedTxOut parcelInput = new OwnedTxOut(txOutRecord, accountWithoutFog);
        Parcel parcel = Parcel.obtain();
        parcelInput.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        OwnedTxOut parcelOutput = OwnedTxOut.CREATOR.createFromParcel(parcel);
        assertEquals(parcelInput, parcelOutput);
    }

}
