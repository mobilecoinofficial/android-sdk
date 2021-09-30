package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.SecureRandom;

@RunWith(AndroidJUnit4.class)
public class KeyImageTest {

    @Test
    public void toAndFromByteTest() {
        byte[] bytes = new byte[4];
        new SecureRandom().nextBytes(bytes);
        KeyImage image1 = KeyImage.fromBytes(bytes);
        KeyImage image2 = KeyImage.fromBytes(image1.getData());
        assertEquals(image1, image2);
    }

}
