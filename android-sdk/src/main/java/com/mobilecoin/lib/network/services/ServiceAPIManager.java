package com.mobilecoin.lib.network.services;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.services.transport.Transport;

/**
 * ServiceAPIManager defines the interface for communication with the remote services
 */
public interface ServiceAPIManager {
    @NonNull
    FogViewService getFogViewService(@NonNull Transport transport);

    @NonNull
    FogUntrustedService getFogUntrustedService(@NonNull Transport transport);

    @NonNull
    FogReportService getFogReportService(@NonNull Transport transport);

    @NonNull
    FogKeyImageService getFogKeyImageService(@NonNull Transport transport);

    @NonNull
    FogMerkleProofService getFogMerkleProofService(@NonNull Transport transport);

    @NonNull
    FogBlockService getFogBlockService(@NonNull Transport transport);

    @NonNull
    ConsensusClientService getConsensusClientService(@NonNull Transport transport);

    @NonNull
    BlockchainService getBlockchainService(@NonNull Transport transport);

    @NonNull
    AttestedService getAttestedService(@NonNull Transport transport);

    void setAuthorization(@NonNull String username, @NonNull String password);
}
