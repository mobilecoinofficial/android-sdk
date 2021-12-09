package com.mobilecoin.lib;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.GRPCServiceAPIManager;
import com.mobilecoin.lib.network.services.RestServiceAPIManager;

/** Builds a {@link MobileCoinClient} using supplied arguments. */
public final class MobileCoinClientBuilder {

  private AccountKey accountKey;
  private TestFogConfig testFogConfig;
  private TransportProtocol transportProtocol;
  private GRPCServiceAPIManager grpcApiManager;
  private RestServiceAPIManager restApiManager;

  /** Returns a {@link MobileCoinClientBuilder} with sensible defaults. */
  public static MobileCoinClientBuilder newBuilder() {
    return new MobileCoinClientBuilder(
            TestKeysManager.getNextAccountKey(),
            TestFogConfig.getFogConfig(Environment.CURRENT_TEST_ENV),
            TransportProtocol.forGRPC(),
            new GRPCServiceAPIManager(),
            new RestServiceAPIManager());
  }

  private MobileCoinClientBuilder(AccountKey accountKey,
                                  TestFogConfig testFogConfig,
                                  TransportProtocol transportProtocol,
                                  GRPCServiceAPIManager grpcApiManager,
                                  RestServiceAPIManager restApiManager) {
    this.accountKey = accountKey;
    this.testFogConfig = testFogConfig;
    this.transportProtocol = transportProtocol;
    this.grpcApiManager = grpcApiManager;
    this.restApiManager = restApiManager;
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
