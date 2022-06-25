# Send transaction

### Motivation

Users will need the ability to send funds to other MobileCoin wallets. This enables the user to do
things such as make purchases or transfer funds to other apps/services (sending to exchanges, etc.).

### Implementation

Two things should be done before sending a transaction:
1. Obtain `PublicAddress` of recipient
2. Estimate total transaction fee (see [calculate-fee](https://github.com/mobilecoinofficial/android-sdk/blob/javadoc-updates/docs/dev-overview/calculate-fee.md))

The rest is fairly simple. The code provided below can be used to create and submit a transaction:

```java
PendingTransaction pendingTransaction = client.prepareTransaction(
        recipientPublicAddress,
        amountToSend,
        fee
);
client.submitTransaction(pendingTransaction.getTransaction());
```
