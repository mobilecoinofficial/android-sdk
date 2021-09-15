# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
