# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
