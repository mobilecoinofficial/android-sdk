package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import java.util.Set;

/**
 * Implemented by classes which provide an {@link AddressHash} of a {@link PublicAddress}
 * This class can be implemented by a contact or contact-like class and used in order to utilize
 * <a href="https://github.com/mobilecoinfoundation/mcips/blob/main/text/0004-recoverable-transaction-history.md">
 *     Recoverable Transaction History
 * </a>
 * @see AddressHash
 * @see PublicAddress
 * @see AccountActivity#recoverTransactions(Set)
 * @see AccountActivity#recoverContactTransactions(AddressHashProvider)
 * @since 1.2.2
 */
public interface AddressHashProvider {

    /**
     * Returns the {@link AddressHash} of a {@link PublicAddress}.
     * The {@link AddressHash} can be used to uniquely identify a {@link PublicAddress} in a more
     * compact way. While the {@link AddressHash} cannot be used to recover a {@link PublicAddress},
     * it can be assumed that any two equivalent {@link AddressHash}es were generated using the same
     * {@link PublicAddress}.
     *
     * @return an {@link AddressHash}
     * @since 1.2.2
     */
    @NonNull
    public AddressHash getAddressHash();

}
