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

[^note]:
    The code provided by the MobileCoin Wallet SDK enables the users to select a transaction fee
    based on the number of seconds they are willing to wait for their transaction to be sent.
