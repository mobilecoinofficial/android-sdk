package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.uri.MobileCoinUri;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Chooses a random service {@link Uri} for each client. */
public final class RandomLoadBalancer implements LoadBalancer {

  private static final Random random = new Random();

  private final List<MobileCoinUri> serviceUris;

  private int lastUsedUriIndex;

  public static RandomLoadBalancer create(@NonNull MobileCoinUri serviceUri) {
    List<MobileCoinUri> mobileCoinUris = Collections
        .unmodifiableList(Collections.singletonList(serviceUri));

    return new RandomLoadBalancer(mobileCoinUris);
  }

  public static RandomLoadBalancer create(@NonNull List<MobileCoinUri> serviceUris) {
    if (serviceUris.isEmpty()) {
      throw new IllegalArgumentException("Service uris is empty.");
    }
    return new RandomLoadBalancer(serviceUris);
  }

  private RandomLoadBalancer(List<MobileCoinUri> serviceUris) {
    this.serviceUris = serviceUris;
  }

  @Override
  public MobileCoinUri getNextServiceUri() {
    if (serviceUris.size() == 1) {
      return serviceUris.get(0);
    }

    return getNextServiceUriForMultipleUris();
  }

  private MobileCoinUri getNextServiceUriForMultipleUris() {
    int randomIndex;
    do {
      randomIndex = random.nextInt(serviceUris.size());
    } while (randomIndex == lastUsedUriIndex);

    MobileCoinUri nextServiceUri = serviceUris.get(randomIndex);
    lastUsedUriIndex = randomIndex;

    return  nextServiceUri;
  }
}
