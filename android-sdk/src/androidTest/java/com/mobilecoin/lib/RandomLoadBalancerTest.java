package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.mobilecoin.lib.network.uri.ConsensusUri;
import com.mobilecoin.lib.network.uri.MobileCoinUri;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class RandomLoadBalancerTest {

  @Test
  public void getNextServiceUri_singleUri_returnsThatUri() throws Exception {
    MobileCoinUri serviceUri = new ConsensusUri(Uri.parse("mc://example.com"));
    RandomLoadBalancer randomLoadBalancer = RandomLoadBalancer.create(serviceUri);

    MobileCoinUri retrievedUri = randomLoadBalancer.getNextServiceUri();

    assertEquals(serviceUri, retrievedUri);
  }

  @Test
  public void getNextUrl_multipleUris_calledTwice_returnsNewUri() throws Exception {
    MobileCoinUri serviceUri1 = new ConsensusUri(Uri.parse("mc://example1.com"));
    MobileCoinUri serviceUri2 = new ConsensusUri(Uri.parse("mc://example2.com"));
    MobileCoinUri serviceUri3 = new ConsensusUri(Uri.parse("mc://example3.com"));
    List<MobileCoinUri> serviceUris = Arrays.asList(serviceUri1, serviceUri2, serviceUri3);
    RandomLoadBalancer randomLoadBalancer = RandomLoadBalancer.create(serviceUris);

    MobileCoinUri retrievedUri1 = randomLoadBalancer.getNextServiceUri();
    MobileCoinUri retrievedUri2 = randomLoadBalancer.getNextServiceUri();

    assertNotEquals(retrievedUri1, retrievedUri2);
  }
}