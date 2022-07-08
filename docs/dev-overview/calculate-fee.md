# Calculate fee

### Motivation

In order for users to send transactions, they must pay the **transaction fee**. Developers of a
MobileCoin wallet application will need to assist users in calculating the required fee.

### Implementation

The following code can be used to estimate the transaction fee to send a certain `Amount`.

```java
Amount amountToSend = Amount.ofMOB(valueToSend);
try {
    Amount minimalFee = mobileCoinClient.calculateMinimalFee(amountToSend);
        ...
} catch (FragmentedAccountException ex) {
    // notify user of an increased fee
    mobileCoinClient.defragmentAccount(txAmount, defragmentationDelegate);
} catch (...) {}
```
