package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.util.NetworkingCall;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class NetworkingCallTest {



    @Test
    public void testAutoRetry() {
        class TestRetryPolicy extends NetworkingCall.RetryPolicy {
            TestRetryPolicy() {
                statusCodes = new int[] {200};// >:)
                retryCount = 13;
            }
        }

        List<Object> listOfFailures = new ArrayList<>();

        // Should fail without retry
        NetworkingCall<Void> nonRetryingCall = new NetworkingCall<>(
                new TestRetryPolicy(),
                () -> {
                    throw new NetworkException(NetworkResult.NOT_FOUND);
                },
                () -> listOfFailures.add(new Object())
        );
        try {
            nonRetryingCall.run();
        } catch (Exception e) {}
        assertEquals(1, listOfFailures.size());

        //Should fail with 13 retries
        NetworkingCall<Void> retryingCall = new NetworkingCall<>(
                new TestRetryPolicy(),
                () -> {
                    throw new NetworkException(NetworkResult.OK);
                },
                () -> listOfFailures.add(new Object())
        );
        try {
            retryingCall.run();
        } catch (Exception e) {}
        assertEquals(14, listOfFailures.size());

        // Should pass without Exception
        NetworkingCall<Void> passingCall = new NetworkingCall<>(
                new TestRetryPolicy(),
                () -> { return null; },
                () -> listOfFailures.add(new Object())
        );
        try {
            passingCall.run();
        } catch (Exception e) {
            fail();
        }
        assertEquals(14, listOfFailures.size());

    }

}
