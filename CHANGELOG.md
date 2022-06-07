# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.1] - 2022-06-07
### Added
- Added Amount.ofMOB(BigInteger value) to create an Amount with MOB token ID

### Changed
- Updated bindings to version 1.2.1

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

