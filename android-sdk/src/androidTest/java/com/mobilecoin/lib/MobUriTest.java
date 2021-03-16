// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.uri.MobUri;

import org.junit.Assert;
import org.junit.Test;

public class MobUriTest {
    private final static String TAG = MobUriTest.class.getName();

    @Test
    public void test_bad_input() {
        String[] badUriStrings = new String[]{
                "mob:///bad/payload",
                "meow:///b58/payload",
                "mob://b58/payload",
                "mob://b58/payload1/payload2",
        };
        for (String badUriString : badUriStrings) {
            try {
                MobUri.fromUri(Uri.parse(badUriString));
                Logger.w(TAG, "Bad uri passed the validation: " + badUriString);
                Assert.fail("Bad uri payload should fail");
            } catch (InvalidUriException ignored) {
                /* Success */
            }
        }
    }

    @Test
    public void test_valid_input() {
        String[] goodUriStrings = new String[]{
                "mob:///b58/payload",
                "mob://mobilecoin.com/b58/payload",
                "mob://mobilecoin.com:1234/b58/payload",
        };
        for (String goodUriString : goodUriStrings) {
            try {
                MobUri.fromUri(Uri.parse(goodUriString));
                /* Success */
            } catch (InvalidUriException ignored) {
                Logger.w(TAG, "Good uri failed the test: " + goodUriString);
                Assert.fail("Good uri should pass test");
            }
        }
    }
}
