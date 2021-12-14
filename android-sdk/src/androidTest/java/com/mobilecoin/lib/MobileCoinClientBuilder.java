package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.network.TransportProtocol;

/** Builds a {@link MobileCoinClient} using supplied arguments. */
public final class MobileCoinClientBuilder {

  private AccountKey accountKey;
  private TestFogConfig testFogConfig;
  private TransportProtocol transportProtocol;

  /** Returns a {@link MobileCoinClientBuilder} with sensible defaults. */
  public static MobileCoinClientBuilder newBuilder() {
    TestFogConfig fogConfig = TestFogConfig.getFogConfig(Environment.CURRENT_TEST_ENV);
    return new MobileCoinClientBuilder(
            TestKeysManager.getNextAccountKey(),
            fogConfig,
            fogConfig.getTransportProtocol());
  }

  private MobileCoinClientBuilder(AccountKey accountKey,
                                  TestFogConfig testFogConfig,
                                  TransportProtocol transportProtocol) {
    this.accountKey = accountKey;
    this.testFogConfig = testFogConfig;
    this.transportProtocol = transportProtocol;
  }

  public MobileCoinClientBuilder setTestFogConfig(TestFogConfig testFogConfig) {
    this.testFogConfig = testFogConfig;
    return this;
  }

  public MobileCoinClientBuilder setAccountKey(AccountKey accountKey) {
    this.accountKey = accountKey;
    return this;
  }

  public MobileCoinClientBuilder setTransportProtocol(TransportProtocol transportProtocol) {
    this.transportProtocol = transportProtocol;
    return this;
  }

  /** Constructs a {@link MobileCoinClient} based on the supplied arguments. */
  public MobileCoinClient build() throws InvalidUriException {
    MobileCoinClient mobileCoinClient = new MobileCoinClient(
        accountKey,
        testFogConfig.getFogUri(),
        testFogConfig.getConsensusUris(),
        testFogConfig.getClientConfig(),
        testFogConfig.getTransportProtocol()
    );
    mobileCoinClient.setFogBasicAuthorization(
        testFogConfig.getUsername(),
        testFogConfig.getPassword()
    );
    mobileCoinClient.setConsensusBasicAuthorization(
        testFogConfig.getUsername(),
        testFogConfig.getPassword()
    );

    return mobileCoinClient;
  }
}
