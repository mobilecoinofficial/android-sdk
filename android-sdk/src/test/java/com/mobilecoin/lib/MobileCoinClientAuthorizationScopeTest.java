// Copyright (c) 2020-2026 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.http.Requester.HttpRequester;
import com.mobilecoin.lib.network.uri.ConsensusUri;
import com.mobilecoin.lib.network.uri.FogUri;
import com.mobilecoin.lib.network.uri.MobileCoinUri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Covers which hosts may receive the HTTP Basic credentials.
 * <p>
 * Fog report uris are read from a public address, so a counterparty picks that host. The
 * credentials must reach only the hosts this client was configured with -- including the account's
 * own report host, which is regularly a different host from the fog view/ledger uri.
 */
@RunWith(RobolectricTestRunner.class)
public class MobileCoinClientAuthorizationScopeTest {

    private static final String FOG_VIEW_HOST = "fog-view.example.com";
    private static final String OWN_REPORT_HOST = "fog-report.example.com";
    private static final String CONSENSUS_HOST = "consensus.example.com";

    @Test
    public void scopesCredentialsToConfiguredHostsAndOwnReportHost() throws InvalidUriException {
        CapturingHttpRequester requester = new CapturingHttpRequester();

        MobileCoinClient.scopeHttpAuthorization(
                TransportProtocol.forHTTP(requester),
                accountKeyWithReportUri(Uri.parse("fog://" + OWN_REPORT_HOST)),
                new FogUri("fog://" + FOG_VIEW_HOST),
                consensusUris(new ConsensusUri("mc://" + CONSENSUS_HOST)));

        // Exact set: anything extra here is a host that would receive the credentials.
        assertEquals(
                new HashSet<>(Arrays.asList(FOG_VIEW_HOST, OWN_REPORT_HOST, CONSENSUS_HOST)),
                requester.capturedHosts);
    }

    @Test
    public void toleratesAccountWithoutFogReportUri() throws InvalidUriException {
        CapturingHttpRequester requester = new CapturingHttpRequester();

        MobileCoinClient.scopeHttpAuthorization(
                TransportProtocol.forHTTP(requester),
                accountKeyWithReportUri(null),
                new FogUri("fog://" + FOG_VIEW_HOST),
                consensusUris(new ConsensusUri("mc://" + CONSENSUS_HOST)));

        assertEquals(
                new HashSet<>(Arrays.asList(FOG_VIEW_HOST, CONSENSUS_HOST)),
                requester.capturedHosts);
    }

    @NonNull
    private static AccountKey accountKeyWithReportUri(Uri reportUri) {
        AccountKey accountKey = mock(AccountKey.class);
        when(accountKey.getFogReportUri()).thenReturn(reportUri);
        return accountKey;
    }

    @NonNull
    private static List<MobileCoinUri> consensusUris(@NonNull ConsensusUri consensusUri) {
        return Collections.<MobileCoinUri>singletonList(consensusUri);
    }

    /**
     * Captures the allowlist the client hands down, without reaching into HttpRequester's state.
     */
    private static final class CapturingHttpRequester extends HttpRequester {
        private final Set<String> capturedHosts = new HashSet<>();

        CapturingHttpRequester() {
            super("username", "password");
        }

        @Override
        public void addAuthorizedHosts(@NonNull Collection<String> hosts) {
            capturedHosts.addAll(hosts);
            super.addAuthorizedHosts(hosts);
        }
    }
}
