# Getting started

Whether you are an independent software engineer/developer or a software engineer at a company that would like to utilize the MobileCoin SDK, you will need to understand the three main steps below to successfully implement a MobileCoin wallet app on a messaging service.

This guide is specifically for developers who have chosen to enable their customers to use MOB cryptocurrency transactions through their custom mobile app. This Integration Guide focuses on the iOS platform, but we've compiled a separate Integration Guide aimed at Android developers as well.&#x20;

The MobileCoin SDK is the building block to integrate all MobileCoin functionality in your custom app. This chapter focuses on the requirements that messaging services must meet in order to implement a MobileCoin wallet app, as well as how the MobileCoin SDK works.

## MobileCoin integration requirements

Before implementing the MobileCoin SDK, your messaging service must meet a certain number of requirements. These include the ability to:

1. Connect to the MobileCoin Fog Services, which provide connections to the two MobileCoin servers, the **View Server** and the **Ledger Server**.

The _View Server_ enables users to view their transactions and get their balances.

The _Ledger Server_ allows users to get “materials” to construct new private transactions. These materials include other _transaction outputs_ ([**TxOuts** or “_mixins_](glossary.md)) and [**Merkle Proofs of Membership**](glossary.md). Each TxOut is like a coin, where every transaction uses TxOuts as inputs to create new TxOuts as the outputs of a transaction.

{% hint style="info" %}
MobileCoin preserves the privacy of the sender by mixing in random coins from the ledger ([_mixins_](glossary.md)), with the coins the user wants to spend. A _Merkle Proof of Membership_ is provided with each _mixin_ as proof that the input is included in the MobileCoin Ledger, so that new coins cannot be forged.
{% endhint %}

1. Connect to the MobileCoin **Consensus Server**, which enables users to send new transactions. New transactions must be validated by the Consensus Server in order for the transactions to be sent.
2. Set up **account keys**, which are uniquely owned by the users. Setting up account keys provides the ability for users to securely create and receive MOB.

![The MobileCoin SDK Architecture.](images/mobilecoin-sdk-architecture.png)

View, Ledger, and Consensus services use Intel’s SGX enclaves, so clients must establish attested connections to these services. The MobileCoin SDK handles the logic for attestation.
