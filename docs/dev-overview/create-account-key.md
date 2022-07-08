# Create an account key

### Motivation

In order for a user to have a MobileCoin wallet, they must have an [**account key**](../glossary.md).
An `AccountKey` can be thought of as a username+password used to access an *account* on the
MobileCoin blockchain.

Any application integrating MobileCoin must provide the functionality to allow users to either import
an existing `AccountKey` or to create a new one.

### Implementation

Using the MobileCoin Android SDK, there are a few different methods that can be used to create an
`AccountKey`

#### Creating a new AccountKey

The simplest way to create a new `AccountKey` is to use the static `createNew` method of `AccountKey`

```java
AccountKey newAccountKey = AccountKey.createNew(
        fogReportUri,
        fogReportId,
        fogAuthoritySpki
);
```
The `fogReportUri` should be a valid Fog report service URL.

The `fogAuthoritySpki` is the Fog authority public key. This will be provided by the Fog provider.

The `fogReportId` is important when the Fog provider operates multiple Fog Ingest enclaves.
The value is used to select which ingest key should be used to create outputs.
This usually isn't necessary. It is generally fine to use a `fogReportId = ""`.

#### Importing an existing AccountKey

There are two methods that can be used to import an existing `AccountKey`
1. Importing form mnemonic phrase
2. Importing from Bip39 entropy

##### Importing from menmonic phrase

```java
AccountKey newAccountKey = AccountKey.fromMnemonicPhrase(
        mnemonicPhrase,
        accountIndex
        fogReportUri,
        fogReportId,
        fogAuthoritySpki
);
```

There are two new fields here: the mnemonic phrase and the account index. The `mnemonicPhrase` is a
space-separated `String` of the 24 words that make up the account's mnemonic phrase.

Each mnemonic phrase can be used to create multiple accounts. The `accountIndex` argument specifies
which account index to create a key for. For most applications, only one AccountKey is needed. In
such cases, it is fine to always use `accountIndex = 0`

##### Importing from Bip39 entropy

```java
AccountKey newAccountKey = AccountKey.fromBip39Entropy(
        bip39Entropy,
        accountIndex
        fogReportUri,
        fogReportId,
        fogAuthoritySpki
);
```

This approach is similar to the mnemonic phrase method. The only difference is the first argument.
Instead of using the 24 word bip49 mnemonic, the corresponding bip39 entropy is used.
