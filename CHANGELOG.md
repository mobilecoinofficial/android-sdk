 # Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.0.0] - 2023-01-04

### Added
- Added `Rng` with two default implementations
- Added ability to provide `Rng` for calls to `MobileCoinClient.prepareTransaction`
- Added `SignedContingentInput`s which can be used on block version >= 3 networks to quantities of two different tokens
- Added `DestinationWithPaymentRequestMemo` and `DestinationWithPaymentRequestMemoData`
- Added `DestinationWithPaymentIntentMemo` and `DestinationWithPaymentIntentMemo`
- Added `SenderWithPaymentIntentMemo` and `SenderWithPaymentIntentMemoData`
- Added `OwnedTxOut.getSubaddressIndex`
- Added methods to get unvalidated `MemoData` of `SenderMemo`, `SenderWithPaymentRequestMemo`, and `SenderWithPaymentIntentMemo`

### Changed
- Updated bindings to version 2.0.0
- Updated okhttp to version 3.11.0

### Fixes
- Fixed insecure transport protocol using TLS
- Fixed connection reset errors in HTTP clients

### Upgrading

No code changes are *required* to upgrade from 1.2.2.4 to 4.0.0

-

## [1.2.2.4] - 2022-08-24

### Changed

- Balance checking algorithm performance improvements

### Fixes

- Query size calculation in `DefaultFogQueryScalingStrategy`

### Upgrading

No code changes are *required* to upgrade from 1.2.2.3 to 1.2.2.4

## [1.2.2.3] - 2022-08-22

### Added

- Added `getTransactionStatusQuick` to `MobileCoinClient`

### Upgrading

No code changes are *required* to upgrade from 1.2.2.2 to 1.2.2.3

## [1.2.2.2] - 2022-08-11
### Fixes

- `GRPCFogKeyImageService` properly converts `StatusRuntimeException` to `NetworkException`

### Upgrading

No code changes are *required* to upgrade from 1.2.2.1 to 1.2.2.2

## [1.2.2.1] - 2022-07-21
### Added
- Added `AccountActivity.getAllTokenTxOuts(TokenId)`

### Changed
- Changed visibility of `OwnedTxOut.getAmount()` to public

### Upgrading

No code changes are *required* to upgrade from 1.2.2 to 1.2.2.1

## [1.2.2] - 2022-07-21
### Added
- Added a `ProposeTxResult` field to `InvalidTransactionException`. This field indicates why the
`Transaction` was not accepted.
- `MobileCoinTransactionClient.submitTransaction` now returns Consensus block count at submission time.

### Changed
- Updated bindings to version 1.2.2

### Fixes
- `OwnedTxOut`s returned through the public API are copied from internal `OwnedTxOut`s. This fixes
some issues caused by `OwnedTxOut`s being updated after being fetched from the public API.
- Fixed default HttpRequester authentication
- Fixed a dependency issue introduced by some project structure changes

### Upgrading

No code changes are *required* to upgrade from 1.2.1 to 1.2.2

- To easily handle various types of transaction failure differently, code such as the following
can be used: `switch(invalidTransactionException.getResult())`
- To obtain the Consensus block index at the time of `Transaction` submission, check the return
value of `MobileCoinTransactionClient.submitTransaction`

## [1.2.1] - 2022-06-07
### Added
- Added Amount.ofMOB(BigInteger value) to create an Amount with MOB token ID

### Changed
- Updated bindings to version 1.2.1

### Upgrading

No code changes are *required* to upgrade from 1.2.0 to 1.2.1

- Calls to `new Amount(value, TokenId.MOB)` may be replaced with `Amount.ofMOB(value)`.

## [1.2.0] - 2022-06-03
### Added
- Support for multiple [token types](https://github.com/mobilecoinfoundation/mcips/blob/main/text/0025-confidential-token-ids.md)
- [Recoverable Transaction History (RTH)](https://github.com/mobilecoinfoundation/mcips/blob/main/text/0004-recoverable-transaction-history.md)
- Default HttpRequester Implementation
- [Parcelable](https://developer.android.com/reference/android/os/Parcelable) support for various SDK classes
- Internal Consensus load balancer

### Changed
- Change TxOuts are now sent to a dedicated [change subaddress](https://github.com/mobilecoinfoundation/mcips/blob/main/text/0036-reserved-subaddresses.md)
- Internal block info cache invalidated on submit transaction error, causing fees and block version to be re-fetched

### Fixes
- Fixed API level 24 support
- FogSyncException will be thrown if Fog View and Ledger are out of sync with each other or Consensus.
  This signifies that balances may temporarily be out of date or incorrect.

### Upgrading
- The constructor for `MobileCoinClient` now requires one additional parameter, a `TransportProtocol`
This can either be `TransportProtocol.forGRPC()` or `TransportProtocol.forHTTP(Requester)`.
- Some methods that interact with network services now throw a `FogSyncException`. This signifies
that the information gathered from the network may be temporarily out of date.
- With support for multiple token types, various account and transaction related methods have been
deprecated. Many of these deprecated methods have simply been parameterized for a `TokenId`. Refer
to the Javadoc of deprecated methods for instructions on what to use instead. Until they are removed,
deprecated API methods will continue to function identically to how they did in 1.1.
- For sending transactions, a `TxOutMemoBuilder` will be required to create `TxOutMemo`s. These can
be used to reconstruct
[Recoverable Transaction History (RTH)](https://github.com/mobilecoinfoundation/mcips/blob/main/text/0004-recoverable-transaction-history.md).
This will be required on network version 1.2.0. `TxOutMemoBuilder.createSenderAndDestinationRTHMemoBuilder()`
can be used to satisfy this requirement and is reverse compatible with network version 1.1.

## [1.2.0-pre0] - 2021-09-15
### Added
- Network Robustness. Host applications now have the ability to choose which transport protocols
  (HTTPS or GRPC) the SDK uses when communicating with MobileCoin services.

### Changed
- Decommissioned RNGs. The SDK no longer generates search keys from Randon Number Generator (RNG)
  seeds that have been "decommissioned," which are RNGS that are associated with an outdated
  enclave.

### Fixes
- Reset attestation state on errors.

## [1.1.0] - 2020-09-06
### Changed
- Added `minimumFeeCacheTTL` parameter to `ClientConfig` to control the duration of the minimum transaction fee caching period. The default value is set to 30 minutes.

## [1.1.0-pre2] - 2020-08-05
### Added
- Dynamic Fees

### Changed
- Root entropy for `TransferPayload` has been replaced with bip39 entropy
- Added `final` modifiers to the classes that should not be extended

## [1.0.0] - 2020-05-04
### Changed
- `setAuthorization` method has been split into `setFogBasicAuthorization` and `setConsensusBasicAuthorization`
- Report URIs in the `AccountKey` and `PublicAddress` are no loger normalized by default

### Fixes
- Incorrect balance in certain situations

## [1.0.1-pre4] - 2020-01-04
### Added
- Estimate total fee in AccountSnapshot
- SLIP-10 AccountKey derivation
- New `AccountKey` constructors: `fromBip39Entropy` and `fromMnemonicPhrase`
- Build on Apple Silicon
- Hash and Equal performance improvements
- Environments switching for Tests

### Removed
- `fromRootEntropy` constructor of `AccountKey` (use `fromBip39Entropy` instead)

### Fixes
- Setting hardening_advisory in the [android-bindings](https://github.com/mobilecoinfoundation/fog/tree/master/android-bindings)

## [1.0.1-pre3] - 2020-19-03
### Added
- Initial release

