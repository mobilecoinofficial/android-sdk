# Check transaction status (for sender)

### Motivation

Applications enabling users to send MobileCoin transactions will need to provide the ability to check
the status of those transactions. This process is different depending on whether the status is being
checked by the sender or the recipient.

### Implementation

Using the MobileCoin Android SDK, the transaction status can be checked by the sender using the
following code:

```java
Transaction transaction = pendingTransaction.getTransaction();
Transaction.Status txStatus = senderClient.getTransactionStatus(transaction);
```

[^note]:
    The code provided by the MobileCoin SDK enables the users to access a secure, pre-processed
    version of their data on the MobileCoin Ledger in MobileCoin Fog Services through their mobile
    wallet app.
