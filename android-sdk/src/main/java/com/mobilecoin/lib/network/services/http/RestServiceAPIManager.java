package com.mobilecoin.lib.network.services.http;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.network.services.AttestedService;
import com.mobilecoin.lib.network.services.BlockchainService;
import com.mobilecoin.lib.network.services.ConsensusClientService;
import com.mobilecoin.lib.network.services.FogBlockService;
import com.mobilecoin.lib.network.services.FogKeyImageService;
import com.mobilecoin.lib.network.services.FogMerkleProofService;
import com.mobilecoin.lib.network.services.FogReportService;
import com.mobilecoin.lib.network.services.FogUntrustedService;
import com.mobilecoin.lib.network.services.FogViewService;
import com.mobilecoin.lib.network.services.ServiceAPIManager;
import com.mobilecoin.lib.network.services.http.clients.RestClient;
import com.mobilecoin.lib.network.services.transport.RestTransport;
import com.mobilecoin.lib.network.services.transport.Transport;

public class RestServiceAPIManager implements ServiceAPIManager {

    @NonNull
    RestClient restClientFromTransport(@NonNull Transport transport) {
        if (transport.getTransportType() == Transport.TransportType.HTTP) {
            RestTransport restTransport = (RestTransport) transport;
            return restTransport.getRestClient();
        }
        throw new IllegalArgumentException("BUG: should not be reachable");
    }

    @NonNull
    @Override
    public FogViewService getFogViewService(@NonNull Transport transport) {
        return new RestFogViewService(restClientFromTransport(transport));
    }

    @NonNull
    @Override
    public FogUntrustedService getFogUntrustedService(@NonNull Transport transport) {
        return new RestFogUntrustedService(restClientFromTransport(transport));
    }

    @NonNull
    @Override
    public FogReportService getFogReportService(@NonNull Transport transport) {
        return new RestFogReportService(restClientFromTransport(transport));
    }

    @NonNull
    @Override
    public FogKeyImageService getFogKeyImageService(@NonNull Transport transport) {
        return new RestFogKeyImageService(restClientFromTransport(transport));
    }

    @NonNull
    @Override
    public FogMerkleProofService getFogMerkleProofService(@NonNull Transport transport) {
        return new RestFogMerkleProofService(restClientFromTransport(transport));
    }

    @NonNull
    @Override
    public FogBlockService getFogBlockService(@NonNull Transport transport) {
        return new RestFogBlockService(restClientFromTransport(transport));
    }

    @NonNull
    @Override
    public ConsensusClientService getConsensusClientService(@NonNull Transport transport) {
        return new RestConsensusClientService(restClientFromTransport(transport));
    }

    @NonNull
    @Override
    public BlockchainService getBlockchainService(@NonNull Transport transport) {
        return new RestBlockchainService(restClientFromTransport(transport));
    }

    @NonNull
    @Override
    public AttestedService getAttestedService(@NonNull Transport transport) {
        return new RestAttestedService(restClientFromTransport(transport));
    }

    @Override
    public void setAuthorization(@NonNull String username, @NonNull String password) {
        // TODO: currently this is handled in the requester, need to move it here
    }
}
