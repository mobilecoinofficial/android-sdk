package com.mobilecoin.lib;

import com.mobilecoin.lib.network.uri.MobileCoinUri;

/** Balances server load for MobileCoin services. */
public interface LoadBalancer {

  /** Returns a new service {@link MobileCoinUri} upon each invocation. */
  MobileCoinUri getNextServiceUri();

}
