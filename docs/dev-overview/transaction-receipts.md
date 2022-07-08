# Transaction receipts (for recipient)

### Motivation

While senders may check the status of their transaction using
[transaction status](check-transaction-status.md), receivers of a transaction must use a different
method to check status. The receiver of a transaction must use the `Receipt` status.

### Implementation

Using the MobileCoin Android SDK, the transaction status can be checked by the recipient using the
following code:

```java
Receipt.Status status = recipientClient.getReceiptStatus(receipt);
```
