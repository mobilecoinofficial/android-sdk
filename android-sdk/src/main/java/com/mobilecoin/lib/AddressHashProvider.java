package com.mobilecoin.lib;

import androidx.annotation.NonNull;

/**
 *
 */
public interface AddressHashProvider {

    /**
     * Returns the {@link AddressHash} of a {@link PublicAddress}.
     * @return an {@link AddressHash}
     * @since 1.2.2
     */
    @NonNull
    public AddressHash getAddressHash();//TODO: documentation

}
