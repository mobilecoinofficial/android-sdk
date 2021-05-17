package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.TransportProtocol;

/**
 * Handles authorization for network requests and internal networking processes.
 */
public interface MobileCoinNetworkManager {

  /**
   * Sets HTTP authorization username and password for FOG requests.
   */
  void setFogBasicAuthorization(@NonNull String username, @NonNull String password);

  /**
   * Sets HTTP authorization username and password for consensus server requests.
   */
  void setConsensusBasicAuthorization(@NonNull String username, @NonNull String password);

  /**
   * Sets the transport protocol for the client-server communication
   * see {@link TransportProtocol} for more details
   */
  void setTransportProtocol(@NonNull TransportProtocol protocol);
  /**
   * Attempts to gracefully shutdown internal networking and threading services This is a blocking
   * call which in rare cases may take up to 10 seconds to complete.
   */
  void shutdown();

}
