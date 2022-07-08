# Get balances

### Motivation

To show what a user currently has in their account, applications must be able to fetch the account's 
`Balance`s. A user's account will have a `Balance` for every token/asset which the account has
received. `Balance`s for various tokens can be fetched individually or all at once.

### Implementation

#### Fetching individual balances

The code provided below can be used to fetch the MobileCoin balance of a user's account:

```java
MobileCoinClient mobileCoinClient = new MobileCoinClient(
    accountKey,
    FOG_URI,
    CONSENSUS_URI,
    TransportProtocol.forGRPC()
);

Balance balance = mobileCoinClient.getBalance(TokenId.MOB);
```

The balance of any arbitrary token ID can retrieved using:

```java
mobileCoinClient.getBalance(TokenId.from(myTokenId));
```

#### Fetching all balances

Balances of all tokens on the account can be fetched all at once using:

```java
Map<TokenId, Balance> balances = mobileCoinClient.getBalances();
```
