package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.FogReportException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TransactionBuilderTest {

    @Test
    public void testBlockVersionCompatibility() throws TransactionBuilderException, FogReportException {
        TransactionBuilder transactionBuilder = new TransactionBuilder(null, TxOutMemoBuilder.createDefaultRTHMemoBuilder(), 1);
    }

}
