![MobileCoin logo](https://raw.githubusercontent.com/mobilecoinofficial/mobilecoin/master/img/mobilecoin_logo.png)

# Android SDK [![CircleCI](https://img.shields.io/circleci/build/gh/mobilecoinofficial/android-sdk?token=eaa920ba2ba6916857aec7ef3c1a9d217a128717)](https://circleci.com/gh/mobilecoinofficial/android-sdk/tree/master) [![Documentation](https://img.shields.io/badge/docs-latest-blue)](https://mobilecoinofficial.github.io/android-sdk/)

MobileCoin is a privacy-preserving payments network designed for use on mobile devices.

The MobileCoin Android SDK is a library to access the MobileCoin blockchain from Android phones.

### Sending your First Payment

* You must read and accept the [Terms of Use for MobileCoins and MobileCoin Wallets](./TERMS-OF-USE.md) to use MobileCoin Software.

### Note to Developers

* The MobileCoin Android SDK is a prototype. Expect substantial changes before the release.
* Please see [*CONTRIBUTING.md*](./CONTRIBUTING.md) for notes on contributing bug reports and code.

# Table of Contents

- [License](#license)
- [Cryptography Notice](#cryptography-notice)
- [Repository Structure](#repository-structure)
- [Overview](#overview)
- [Binary Distribution](#binary-distribution)
- [Build Instructions](#build-instructions)
- [Running Tests](#running-tests)
- [Usage](#usage)
- [Support](#support)
- [Trademarks](#trademarks)

## License

MobileCoin is available under open-source licenses. Please read the [*LICENSE.md*](./LICENSE.md) and corresponding [*LICENSE*](./LICENSE).

## Cryptography Notice

This distribution includes cryptographic software. Your country may have restrictions on the use of encryption software.
Please check your country's laws before downloading or using this software.

## Repository Structure

|Directory |Description |
| :-- | :-- |
| [main](./android-sdk/src/main/java/com/mobilecoin) | Sources for the MobileCoin Android SDK. |
| [androidTest](./android-sdk/src/androidTest/java/com/mobilecoin) | Tests. |
| [testApp](./testApp) | Test application using the Android SDK. |

## Overview

MobileCoin is a payment network with no central authority. The fundamental goal of the network is to safely and
efficiently enable the exchange of value, represented as fractional ownership of the total value of the network.
Like most cryptocurrencies, MobileCoin maintains a permanent and immutable record of all successfully completed
payments in a blockchain data structure. Cryptography is used extensively to establish ownership, control transfers,
and to preserve cash-like privacy for users.

For more information about the cryptocurrency, see [MobileCoinFoundation/MobileCoin](https://github.com/mobilecoinfoundation/mobilecoin).

## Binary Distribution

The MobileCoin Android SDK will be available on JCenter and Maven Central after the public release. Meanwhile, add 
the MobileCoin repository to the list of repositories in the root `build.gradle` file.

```
repositories {
    maven {
        url "https://dl.cloudsmith.io/qxAgwaeEE1vN8aLU/mobilecoin/mobilecoin/maven/"
    }
    ....
}
```

Then insert library dependency in the dependecies section of the target's `build.gradle`

```
dependencies {
    implementation 'com.mobilecoin:android-sdk:VERSION'
	....
}
```

## Build Instructions

The project uses a dockerized gradle build system with the main targets wrapped into a convenient to use **makefile targets**:<br />

* **`setup`** build a docker image for the build container
* **`build`** build the project inside the docker container
* **`deployLocal`** build the project and deploy the artifact into a local maven repo. Resulting artifact will be deployed into the host's local maven repo (~/.m2)
* **`clean`** remove intermediate build files
* **`all`** same as building the following targets: **setup** => **clean** => **build** => **deployLocal**

Run `make TARGET` to execute any of the above targets.

Note: To build android native bindings (libmobilecoin.so), clone [Fog](https://github.com/mobilecoinfoundation/fog.git) and run **make** in the android-bindings directory.

## Running Tests

Run **`gradle cAT`** to run the tests on the physically connected device or an emulator.

\**Tests require a connected Android device or a running emulator as most of the tests use features of the android itself and cannot be run on the host OS without emulation.*


## Usage

### Get Balance

Existing accounts can be created with entropy and new accounts generated with `Account.createNew`

```java
private static final Uri FOG_URI = Uri.parse("fog://FOR-URI");
private static final Uri CONSENSUS_URI = Uri.parse("mc://CONSENSUS-URI");


String bip39Entropy = { /*
                      32 bytes of randomness generated using a
                      cryptographically strong random number 
                      generator like SecureRandom.
                      ACCOUNT ENTROPY IS A SENSITIVE INFORMATION AND 
                      MUST NOT BE SHARED WITH ANYONE, AS USER'S PRIVATE
                      KEYS ARE DETERMINISTICALLY GENERATED FROM IT
                 */ }
// fogAuthoritySpki and fogReportId will be provided by the Fog operator
AccountKey accountKey = AccountKey.fromBip39Entropy(bip39Entropy, FOG_URI, fogReportId,
fogAuthorityKey);
MobileCoinClient mobileCoinClient = new MobileCoinClient(
	account,
	FOG_URI,
	CONSENSUS_URI
);
try {
    Balance balance = mobileCoinClient.getBalance();
    Log.d(TAG, "Balance: " + balance.toString());
} catch (InvalidFogResponse | NetworkException | AttestationException e) {
    // process error
} finally {
    mobileCoinClient.shutdown();
}
```

### Post Transaction

Recipient obtains own public address using `accountKey.getPublicAddress()` and then shares it with the sender by serializing it with publicAddress.toByteArray() method

```java
try {
    BigInteger amount = BigInteger.valueOf(YOUR_AMOUNT);
    PublicAddress recipient = PublicAdress.fromBytes(serializedBytes);
    BigInteger fee = mobileCoinClient.estimateTotalFee(amount);
    PendingTransaction pending = mobileCoinClient.prepareTransaction(recipient, amount, fee);
    mobileCoinClient.submitTransaction(pending.getTransaction());
} catch (InsufficientFundsException | FeeRejectedException | InvalidFogResponse e) {
    // process error
} catch (FragmentedAccountException e) {
    // run defragmentAccount() and try again
}

```

### Check Transaction Status

Transaction status for the sender

```java
Transaction.Status status = mobileCoinClient.getTransactionStatus(pending.getTransaction());

// share the receipt with the recipient
Receipt receipt = pending.getReceipt();
byte[] serializedBytes = receipt.toByteArray();
```

Transaction status for the recipient

```java
// obtain the serialized Receipt from the sender
// then deserialize it and check status
Receipt receipt = Receipt.fromBytes(serializedBytes)
Receipt.Status status = recipientMobileCoinClient.getReceiptStatus(receipt);
```

## Support

For troubleshooting help and other questions, please visit our [community forum](https://community.mobilecoin.foundation/).

You can also open a technical support ticket via [email](mailto://support@mobilecoin.com).

#### Trademarks

MobileCoin is a registered trademark of MobileCoin Inc.
