package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import android.os.Parcel;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.InvalidFogResponse;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.NetworkException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class OwnedTxOutTest {

    @Test
    public void testParcelable() throws InvalidUriException, AttestationException, InvalidFogResponse, NetworkException {
        MobileCoinClient client = MobileCoinClientBuilder.newBuilder().build();
        AccountActivity activity = client.getAccountActivity();
        for(OwnedTxOut parcelInput : activity.getAllTxOuts()) {
            Parcel parcel = Parcel.obtain();
            parcelInput.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            OwnedTxOut parcelOutput = OwnedTxOut.CREATOR.createFromParcel(parcel);
            assertEquals(parcelInput, parcelOutput);
            parcel.recycle();
        }
    }

}
