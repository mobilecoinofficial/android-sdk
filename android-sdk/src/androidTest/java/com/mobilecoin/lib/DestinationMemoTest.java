package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DestinationMemoTest {

  @Test(expected = IllegalArgumentException.class)
  public void create_memoDataIncorrectLength() {
    byte[] memoData = new byte[1];

    DestinationMemo.create(memoData);
  }

  @Test
  public void create_memoDataLength_createsDestinationMemo() {
    byte[] memoData = new byte[44];

    DestinationMemo.create(memoData);
  }

  @Test
  public void getDestinationMemoData_validMemoData_returnsDestinationMemoDataWithCorrectAddressHash() throws Exception {
    byte[] memoData = new byte[44];

    DestinationMemo destinationMemo = DestinationMemo.create(memoData);

    destinationMemo.getDestinationMemoData(null, null);
  }

}