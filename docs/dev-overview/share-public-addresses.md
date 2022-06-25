# Share public address

### Motivation

In order for a user to receive MobileCoin transactions, they must share their `PublicAddress` with
a sender. The sender can save this `PublicAddress` for future transactions. A user's `PublicAddress`
is not considered secret and does not expose any information about the corresponding account
information,  past/future transactions, or balances.

[^note]:
    Users can share their public addresses without sending a transaction.

### Implementation

Using the MobileCoin Android SDK, a user's `PublicAddress` can be obtained using the following code:

```java
PublicAddress pubAddress = accountKey.getPublicAddress();
```

To generate the printable `String` that can be shared with another user, the following code can be used:

```java
PrintableWrapper wrapper = PrintableWrapper.fromPublicAddress(pubAddress);
String printablePubAddress = wrapper.toB58String();
```

This `String` can then be shared with a potential sender using any suitable method (Copied text, 
QR code, etc.). Subsequently, a sender may recover the `PublicAddress` using this example code:

```java
PrintableWrapper wrapper = PrintableWrapper.fromB58String(printablePubAddress);

if(wrapper.hasPublicAddress()) {
    PublicAddress pubAddress = wrapper.getPublicAddress();
        ...
}
else {
    // notify user of invalid PublicAddress
}
```
