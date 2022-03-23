package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.SecureRandom;

@RunWith(AndroidJUnit4.class)
public class AddressHashTest {

    @Test
    public void testParcelable() {
        SecureRandom random = new SecureRandom();
        byte randomBytes[] = new byte[128];
        random.nextBytes(randomBytes);
        AddressHash hashes[] = {
                AddressHash.createAddressHash(randomBytes),// random hash
                AddressHash.createAddressHash(new byte[0]),// empty hash
                AddressHash.createAddressHash(new byte[4])// zero hash
        };

        for(AddressHash addressHash : hashes) {
            Parcel parcel = Parcel.obtain();
            addressHash.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            AddressHash deserializedHash = AddressHash.CREATOR.createFromParcel(parcel);
            assertEquals(addressHash, deserializedHash);
            parcel.recycle();
        }

    }

}
