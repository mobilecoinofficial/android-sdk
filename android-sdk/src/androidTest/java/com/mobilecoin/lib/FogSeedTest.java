package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.protobuf.ByteString;
import com.mobilecoin.lib.exceptions.KexRngException;
import com.mobilecoin.lib.exceptions.SerializationException;

import org.junit.Test;
import org.junit.runner.RunWith;

import fog_view.View;
import kex_rng.KexRng;

@RunWith(AndroidJUnit4.class)
public class FogSeedTest {

    @Test
    public void testParcelable() throws SerializationException, KexRngException {
        View.RngRecord rngRecord = View.RngRecord.newBuilder()
                .setPubkey(KexRng.KexRngPubkey.newBuilder()
                        .setPubkey(ByteString.copyFrom(new byte[32])))
                .setIngestInvocationId(62L)
                .setStartBlock(4234234L)
                .build();
        FogSeed uutInput = new FogSeed(RistrettoPrivate.fromBytes(new byte[32]), rngRecord);
        uutInput.markObsolete();
        Parcel parcel = Parcel.obtain();
        uutInput.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        FogSeed uutOutput = FogSeed.CREATOR.createFromParcel(parcel);
        assertEquals(uutInput, uutOutput);
    }

}
