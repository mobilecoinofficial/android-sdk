# Transaction receipts (for recipient)

### Motivation

Similar to checking [transaction statuses](check-transaction-status.md), users of a MobileCoin wallet
application will need to check the status of payments received.

### Implementation

Using the MobileCoin Android SDK, the transaction status can be checked by the recipient using the
following code:

```java
Receipt.Status status = recipientClient.getReceiptStatus(receipt);
```
