package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EmptyMemoTest {

    @Test
    public void testParcelable() {
        for(TxOutMemoType memoType : TxOutMemoType.values()) {
            Parcel parcel = Parcel.obtain();
            EmptyMemo emptyMemo = new EmptyMemo(memoType);
            emptyMemo.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            EmptyMemo deserializedMemo = EmptyMemo.CREATOR.createFromParcel(parcel);
            assertEquals(emptyMemo, deserializedMemo);
            parcel.recycle();
        }
    }

}
