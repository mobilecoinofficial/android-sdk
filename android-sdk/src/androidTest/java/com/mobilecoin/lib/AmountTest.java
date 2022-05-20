package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static com.mobilecoin.lib.UnsignedLongTest.TEST_BIG_INTEGERS;
import static com.mobilecoin.lib.UnsignedLongTest.TEST_LONGS;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.log.Logger;

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

    @Test
    public void testEquals() {
        // Try two equals Amounts
        assertTrue(new Amount(BigInteger.TEN, UnsignedLong.ZERO)
                .equals(new Amount(BigInteger.TEN, UnsignedLong.ZERO)));
        // Try different value
        assertFalse(new Amount(BigInteger.TEN, UnsignedLong.ZERO)
                .equals(new Amount(BigInteger.ONE, UnsignedLong.ZERO)));
        // Try different token ID
        assertFalse(new Amount(BigInteger.TEN, UnsignedLong.ZERO)
                .equals(new Amount(BigInteger.TEN, UnsignedLong.ONE)));
        // Try different value and token ID
        assertFalse(new Amount(BigInteger.TEN, UnsignedLong.ZERO)
                .equals(new Amount(BigInteger.ONE, UnsignedLong.ONE)));
        // Try same Amount object
        Amount amount = new Amount(new BigInteger("54321"), UnsignedLong.fromLongBits(12345L));
        assertTrue(amount.equals(amount));
        // Try different object
        assertFalse(amount.equals(new Object()));
    }

    @Test
    public void testCompareTo() {
        Amount biggerAmount = new Amount(new BigInteger("9001"), UnsignedLong.fromLongBits(24L));
        Amount smallerAmount = new Amount(new BigInteger("9000"), UnsignedLong.fromLongBits(24L));
        assertEquals(0, new Amount(BigInteger.TEN, UnsignedLong.ZERO)
                .compareTo(new Amount(BigInteger.TEN, UnsignedLong.ZERO)));
        assertEquals(-1, smallerAmount.compareTo(biggerAmount));
        assertEquals(1, biggerAmount.compareTo(smallerAmount));
        // Comparison of different token IDs should fail, even if value is equal
        try {
            biggerAmount.compareTo(new Amount(new BigInteger("9001"), UnsignedLong.ZERO));
            fail("Can't compare Amounts of different token IDs");
        } catch (IllegalArgumentException e) {}
        // Comparison of different token IDs should fail
        try {
            biggerAmount.compareTo(new Amount(smallerAmount.getValue(), UnsignedLong.ZERO));
            fail("Can't compare Amounts of different token IDs");
        } catch (IllegalArgumentException e) {}
    }

    @Test
    public void testAdd() {
        for(BigInteger value1 : TEST_BIG_INTEGERS) {
            for(BigInteger value2 : TEST_BIG_INTEGERS) {
                Amount a = new Amount(value1, UnsignedLong.ZERO);
                Amount b = new Amount(value2, UnsignedLong.ZERO);
                Amount c = new Amount(value1, UnsignedLong.ONE);
                assertEquals(a.add(b).getValue(), value1.add(value2));
                assertEquals(a, a.add(new Amount(BigInteger.ZERO, UnsignedLong.ZERO)));
                try {
                    a.add(c);
                    fail("Can't add Amounts with different token ID");
                } catch(IllegalArgumentException e) {}
            }
        }
    }

    @Test
    public void testSubtract() {
        for(BigInteger value1 : TEST_BIG_INTEGERS) {
            for(BigInteger value2 : TEST_BIG_INTEGERS) {
                Amount a = new Amount(value1, UnsignedLong.ZERO);
                Amount b = new Amount(value2, UnsignedLong.ZERO);
                Amount c = new Amount(value1, UnsignedLong.ONE);
                assertEquals(a.subtract(b).getValue(), value1.subtract(value2));
                assertEquals(a, a.subtract(new Amount(BigInteger.ZERO, UnsignedLong.ZERO)));
                assertEquals(new Amount(BigInteger.ZERO, UnsignedLong.ZERO), a.subtract(a));
                try {
                    a.subtract(c);
                    fail("Can't subtract Amounts with different token ID");
                } catch(IllegalArgumentException e) {}
            }
        }
    }

    @Test
    public void testMultiply() {
        for(BigInteger value1 : TEST_BIG_INTEGERS) {
            for(BigInteger value2 : TEST_BIG_INTEGERS) {
                Amount a = new Amount(value1, UnsignedLong.ZERO);
                Amount b = new Amount(value2, UnsignedLong.ZERO);
                Amount c = new Amount(value1, UnsignedLong.ONE);
                assertEquals(a.multiply(b).getValue(), value1.multiply(value2));
                assertEquals(a, a.multiply(new Amount(BigInteger.ONE, UnsignedLong.ZERO)));
                try {
                    a.multiply(c);
                    fail("Can't multiply Amounts with different token ID");
                } catch(IllegalArgumentException e) {}
            }
        }
    }

    @Test
    public void testDivide() {
        for(BigInteger value1 : TEST_BIG_INTEGERS) {
            for(BigInteger value2 : TEST_BIG_INTEGERS) {
                if(value2.equals(BigInteger.ZERO)) continue;
                Amount a = new Amount(value1, UnsignedLong.ZERO);
                Amount b = new Amount(value2, UnsignedLong.ZERO);
                Amount c = new Amount(value1, UnsignedLong.ONE);
                assertEquals(a.divide(b).getValue(), value1.divide(value2));
                assertEquals(a, a.divide(new Amount(BigInteger.ONE, UnsignedLong.ZERO)));
                try {
                    a.divide(c);
                    fail("Can't divide Amounts with different token ID");
                } catch(IllegalArgumentException e) {}
                try {
                    a.divide(new Amount(BigInteger.ZERO, UnsignedLong.ZERO));
                    fail("Can't divide Amount by zero");
                } catch(ArithmeticException e) {}
            }
        }
    }

}
