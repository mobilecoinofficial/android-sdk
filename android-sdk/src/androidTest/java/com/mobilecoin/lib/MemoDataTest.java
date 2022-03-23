package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MemoDataTest {

    @Test
    public void testSenderMemoParcelable() {
        SenderMemoData senderMemoData = SenderMemoData.create(AddressHash.createAddressHash(new byte[4]));
        Parcel parcel = Parcel.obtain();
        senderMemoData.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        SenderMemoData deserializedData = SenderMemoData.CREATOR.createFromParcel(parcel);
        assertEquals(senderMemoData, deserializedData);
        parcel.recycle();
    }

    @Test
    public void testWithPaymentRequestMemoParcelable() {
        SenderWithPaymentRequestMemoData senderWithPaymentRequestMemoData = SenderWithPaymentRequestMemoData
                .create(AddressHash.createAddressHash(new byte[4]), UnsignedLong.fromLongBits(240L));
        Parcel parcel = Parcel.obtain();
        senderWithPaymentRequestMemoData.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        SenderWithPaymentRequestMemoData deserializedData = SenderWithPaymentRequestMemoData.CREATOR.createFromParcel(parcel);
        assertEquals(senderWithPaymentRequestMemoData, deserializedData);
        parcel.recycle();
    }

    @Test
    public void testDestinationMemoParcelable() {
        DestinationMemoData destinationMemoData = DestinationMemoData
                .create(AddressHash.createAddressHash(new byte[4]), 1, UnsignedLong.TEN, UnsignedLong.fromLongBits(101L));
        Parcel parcel = Parcel.obtain();
        destinationMemoData.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        DestinationMemoData deserializedData = DestinationMemoData.CREATOR.createFromParcel(parcel);
        assertEquals(destinationMemoData, deserializedData);
        parcel.recycle();
    }

}
