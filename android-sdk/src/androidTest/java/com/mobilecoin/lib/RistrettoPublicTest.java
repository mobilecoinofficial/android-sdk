package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import android.os.Parcel;

import com.mobilecoin.lib.exceptions.SerializationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RistrettoPublicTest {

    @Test
    public void testParcelable() throws SerializationException {
        RistrettoPublic parcelInput = RistrettoPublic.fromBytes(new byte[RistrettoPublic.PUBLIC_KEY_SIZE]);
        Parcel parcel = Parcel.obtain();
        parcelInput.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        RistrettoPublic parcelOutput = RistrettoPublic.CREATOR.createFromParcel(parcel);
        assertEquals(parcelInput, parcelOutput);
    }

}
