package com.mobilecoin.lib;

import android.net.Uri;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.network.TransportProtocol;

import java.util.List;

/** Builds a {@link MobileCoinClient} using supplied arguments. */
public final class MobileCoinClientBuilder {

  private AccountKey accountKey;
  private Uri fogUri;
  private List<Uri> consensusUris;
  private ClientConfig clientConfig;
  private String username;
  private String password;
  private TransportProtocol transportProtocol;

  /** Returns a {@link MobileCoinClientBuilder} with sensible defaults. */
  public static MobileCoinClientBuilder newBuilder() {
    TestFogConfig fogConfig = TestFogConfig.getFogConfig(Environment.CURRENT_TEST_ENV);
    return new MobileCoinClientBuilder(fogConfig);
  }

  /**
   * Returns a {@link MobileCoinClientBuilder} for {@code accountKey}, with the
   * same defaults as {@link #newBuilder()} but without drawing a test account.
   *
   * <p>{@link #newBuilder()} takes the next key from {@link TestKeysManager} in
   * its constructor, which both advances the rotation and records the key as
   * issued. A caller that supplies its own account with {@link #setAccountKey}
   * afterwards discards that draw, shifting which account every later test
   * receives and naming an unused wallet in {@link FundingDiagnosticRule}'s
   * report. Tests that bring their own key should start here instead.
   */
  public static MobileCoinClientBuilder newBuilder(AccountKey accountKey) {
    TestFogConfig fogConfig = TestFogConfig.getFogConfig(Environment.CURRENT_TEST_ENV);
    return new MobileCoinClientBuilder(accountKey,
            fogConfig.getFogUri(),
            fogConfig.getConsensusUris(),
            fogConfig.getClientConfig(),
            fogConfig.getUsername(),
            fogConfig.getPassword(),
            fogConfig.getTransportProtocol());
  }

  private MobileCoinClientBuilder(TestFogConfig testFogConfig) {
    this(TestKeysManager.getNextAccountKey(),
            testFogConfig.getFogUri(),
            testFogConfig.getConsensusUris(),
            testFogConfig.getClientConfig(),
            testFogConfig.getUsername(),
            testFogConfig.getPassword(),
            testFogConfig.getTransportProtocol());
  }

  private MobileCoinClientBuilder(AccountKey accountKey,
                                  Uri fogUri,
                                  List<Uri> consensusUris,
                                  ClientConfig clientConfig,
                                  String username,
                                  String password,
                                  TransportProtocol transportProtocol) {
    this.accountKey = accountKey;
    this.fogUri = fogUri;
    this.consensusUris = consensusUris;
    this.clientConfig = clientConfig;
    this.username = username;
    this.password = password;
    this.transportProtocol = transportProtocol;
  }

  /**
   * Applies {@code testFogConfig}'s endpoints and credentials to this builder.
   *
   * <p>Mutates, like every other setter here. It used to return a fresh
   * builder, which discarded every field already set — a preceding
   * {@link #setAccountKey} was silently dropped — and drew another key from
   * {@link TestKeysManager} on the way.
   */
  public MobileCoinClientBuilder setTestFogConfig(TestFogConfig testFogConfig) {
    this.fogUri = testFogConfig.getFogUri();
    this.consensusUris = testFogConfig.getConsensusUris();
    this.clientConfig = testFogConfig.getClientConfig();
    this.username = testFogConfig.getUsername();
    this.password = testFogConfig.getPassword();
    this.transportProtocol = testFogConfig.getTransportProtocol();
    return this;
  }

  public MobileCoinClientBuilder setAccountKey(AccountKey accountKey) {
    this.accountKey = accountKey;
    return this;
  }

  public MobileCoinClientBuilder setFogUri(Uri fogUri) {
    this.fogUri = fogUri;
    return this;
  }

  public MobileCoinClientBuilder setConsensusUris(List<Uri> consensusUris) {
    this.consensusUris = consensusUris;
    return this;
  }

  public MobileCoinClientBuilder setClientConfig(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
    return this;
  }

  public MobileCoinClientBuilder setUsername(String username) {
    this.username = username;
    return this;
  }

  public MobileCoinClientBuilder setPassword(String password) {
    this.password = password;
    return this;
  }

  public MobileCoinClientBuilder setTransportProtocol(TransportProtocol transportProtocol) {
    this.transportProtocol = transportProtocol;
    return this;
  }

  /** Constructs a {@link MobileCoinClient} based on the supplied arguments. */
  public MobileCoinClient build() throws InvalidUriException {
    MobileCoinClient mobileCoinClient = new MobileCoinClient(
        this.accountKey,
        this.fogUri,
        this.consensusUris,
        this.clientConfig,
        this.transportProtocol
    );
    mobileCoinClient.setFogBasicAuthorization(
        this.username,
        this.password
    );
    mobileCoinClient.setConsensusBasicAuthorization(
        this.username,
        this.password
    );

    return mobileCoinClient;
  }

  public AccountKey getAccountKey() {
    return this.accountKey;
  }

  public Uri getFogUri() {
    return this.fogUri;
  }

  public List<Uri> getConsensusUris() {
    return this.consensusUris;
  }

  public ClientConfig getClientConfig() {
    return this.clientConfig;
  }

  public String getUsername() {
    return this.username;
  }

  public String getPassword() {
    return this.password;
  }

  public TransportProtocol getTransportProtocol() {
    return this.transportProtocol;
  }

}
