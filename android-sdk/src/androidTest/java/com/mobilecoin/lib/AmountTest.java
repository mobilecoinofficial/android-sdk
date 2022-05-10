package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;

@RunWith(AndroidJUnit4.class)
public class AmountTest {

    @Test
    public void testParcelable() {
        Amount parcelInput = new Amount(BigInteger.TEN, KnownTokenId.MOB.getId());
        Parcel parcel = Parcel.obtain();
        parcelInput.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Amount parcelOutput = Amount.CREATOR.createFromParcel(parcel);
        assertEquals(parcelInput, parcelOutput);
    }

}
