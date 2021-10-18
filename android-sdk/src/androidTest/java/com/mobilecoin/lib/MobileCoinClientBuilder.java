package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.network.TransportProtocol;

/** Builds a {@link MobileCoinClient} using supplied arguments. */
public final class MobileCoinClientBuilder {

  private AccountKey accountKey;
  private TestFogConfig testFogConfig;

  /** Returns a {@link MobileCoinClientBuilder} with sensible defaults. */
  public static MobileCoinClientBuilder newBuilder() {
    return new MobileCoinClientBuilder(TestKeysManager.getNextAccountKey(),
        TestFogConfig.getFogConfig(Environment.CURRENT_TEST_ENV));
  }

  private MobileCoinClientBuilder(AccountKey accountKey, TestFogConfig testFogConfig) {
    this.accountKey = accountKey;
    this.testFogConfig = testFogConfig;
  }

  public MobileCoinClientBuilder setTestFogConfig(TestFogConfig testFogConfig) {
    this.testFogConfig = testFogConfig;
    return this;
  }

  public MobileCoinClientBuilder setAccountKey(AccountKey accountKey) {
    this.accountKey = accountKey;
    return this;
  }

  /** Constructs a {@link MobileCoinClient} based on the supplied arguments. */
  public MobileCoinClient build() throws InvalidUriException {
    MobileCoinClient mobileCoinClient = new MobileCoinClient(
        accountKey,
        testFogConfig.getFogUri(),
        testFogConfig.getConsensusUris(),
        testFogConfig.getClientConfig()
    );
    mobileCoinClient.setFogBasicAuthorization(
        testFogConfig.getUsername(),
        testFogConfig.getPassword()
    );
    mobileCoinClient.setConsensusBasicAuthorization(
        testFogConfig.getUsername(),
        testFogConfig.getPassword()
    );
    mobileCoinClient.setTransportProtocol(TransportProtocol.forHTTP(new SimpleRequester()));

    return mobileCoinClient;
  }
}
