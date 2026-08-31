package com.mobilecoin.lib.network.services.http.Requester;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;

import androidx.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class HttpRequesterTest {

    @Mock
    private HttpURLConnection connection;
    @Mock
    private Uri uri;

    private static final byte[] response = new byte[]{10, 32, 78, -85, -53, -1};
    private HttpRequester spy = null;
    private static final int SUCCESS_RESPONSE_CODE = 200;
    private static final int ERROR_RESPONSE_CODE = 500;
    private static final String METHOD_NAME = "POST";
    private static final String HEADER_KEY = "set-cookie";
    private static final String HEADER_VALUE = "VIEW=5f174fe74af1675d; path=/";
    private static final String AUTHORIZATION_KEY = "Authorization";
    private static final String CONFIGURED_HOST = "fog.test.mobilecoin.com";
    private static final String COUNTERPARTY_HOST = "attacker.example.com";

    @Before
    public void setup() {
        HttpRequester requester = new HttpRequester("", "");
        spy = spy(requester);
    }

    @Test
    public void successResponseCode() throws IOException {
        // Given
        doReturn(connection).when(spy).createConnection(any(), any(), any(), any(), any());
        when(connection.getResponseCode()).thenReturn(SUCCESS_RESPONSE_CODE);
        InputStream stream = new ByteArrayInputStream(response);
        when(connection.getInputStream()).thenReturn(stream);
        // When
        Requester.HttpResponse response = spy.httpRequest(METHOD_NAME, uri, new HashMap<>(), new byte[]{ }, "");
        // Then
        assertEquals(SUCCESS_RESPONSE_CODE, response.getResponseCode());
    }

    @Test
    public void errorResponseCode() throws IOException {
        // Given
        doReturn(connection).when(spy).createConnection(any(), any(), any(), any(), any());
        when(connection.getResponseCode()).thenReturn(ERROR_RESPONSE_CODE);
        InputStream stream = new ByteArrayInputStream(response);
        when(connection.getErrorStream()).thenReturn(stream);
        // When
        Requester.HttpResponse response = spy.httpRequest(METHOD_NAME, uri, new HashMap<>(), new byte[]{ }, "");
        // Then
        assertEquals(ERROR_RESPONSE_CODE, response.getResponseCode());
    }

    @Test
    public void disconnectCalled() throws IOException {
        // Given
        doReturn(connection).when(spy).createConnection(any(), any(), any(), any(), any());
        when(connection.getResponseCode()).thenReturn(ERROR_RESPONSE_CODE);
        InputStream stream = new ByteArrayInputStream(response);
        when(connection.getErrorStream()).thenReturn(stream);
        // When
        spy.httpRequest(METHOD_NAME, uri, new HashMap<>(), new byte[]{ }, "");
        // Then
        verify(connection, times(1)).disconnect();
    }

    @Test
    public void parseSuccessResponse() throws IOException {
        // Given
        doReturn(connection).when(spy).createConnection(any(), any(), any(), any(), any());
        when(connection.getResponseCode()).thenReturn(SUCCESS_RESPONSE_CODE);
        InputStream stream = new ByteArrayInputStream(response);
        when(connection.getInputStream()).thenReturn(stream);
        // When
        Requester.HttpResponse response = spy.httpRequest(METHOD_NAME, uri, new HashMap<>(), new byte[]{ }, "");
        // Then
        assertEquals(SUCCESS_RESPONSE_CODE, response.getResponseCode());
        verify(connection, times(1)).disconnect();
        assertArrayEquals(this.response, response.getResponseData());
    }

    @Test(expected = IOException.class)
    public void callDisconnectOnException() throws IOException {
        // Given
        doReturn(connection).when(spy).createConnection(any(), any(), any(), any(), any());
        when(connection.getResponseCode()).thenReturn(SUCCESS_RESPONSE_CODE);
        when(connection.getInputStream()).thenThrow(new IOException());
        // When
        Requester.HttpResponse response = spy.httpRequest(METHOD_NAME, uri, new HashMap<>(), new byte[]{ }, "");
        // Then
        verify(connection, times(1)).disconnect();
    }

    @Test
    public void authorizationHeaderIsScopedToAuthorizedHosts() throws IOException {
        // Given a requester scoped to the configured fog host
        doReturn(connection).when(spy).createConnection(any(), any(), any(), any(), any());
        when(connection.getResponseCode()).thenReturn(SUCCESS_RESPONSE_CODE);
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(response));
        spy.addAuthorizedHosts(Collections.singleton(CONFIGURED_HOST));

        // When the configured host is contacted
        Map<String, String> configuredHostHeaders = new HashMap<>();
        spy.httpRequest(METHOD_NAME, uriWithHost(CONFIGURED_HOST), configuredHostHeaders,
                new byte[]{ }, "");

        // And when a host taken from a counterparty's fog report url is contacted
        Map<String, String> counterpartyHostHeaders = new HashMap<>();
        spy.httpRequest(METHOD_NAME, uriWithHost(COUNTERPARTY_HOST), counterpartyHostHeaders,
                new byte[]{ }, "");

        // Then only the configured host receives the credentials
        assertTrue(configuredHostHeaders.containsKey(AUTHORIZATION_KEY));
        assertFalse(counterpartyHostHeaders.containsKey(AUTHORIZATION_KEY));
    }

    @NonNull
    private static Uri uriWithHost(@NonNull String host) {
        Uri uri = mock(Uri.class);
        when(uri.getHost()).thenReturn(host);
        return uri;
    }

    @Test
    public void parseResponseHeaders() throws IOException {
        // Given
        Map<String, String> expectedHeader = new HashMap<>();
        expectedHeader.put(HEADER_KEY, HEADER_VALUE);
        doReturn(connection).when(spy).createConnection(any(), any(), any(), any(), any());
        when(connection.getResponseCode()).thenReturn(SUCCESS_RESPONSE_CODE);
        InputStream stream = new ByteArrayInputStream(response);
        when(connection.getInputStream()).thenReturn(stream);
        Map<String, List<String>> responseHeaders = new HashMap<>();
        List<String> headerValue = new ArrayList<>();
        headerValue.add(HEADER_VALUE);
        responseHeaders.put(HEADER_KEY, headerValue);
        when(connection.getHeaderFields()).thenReturn(responseHeaders);
        // When
        Requester.HttpResponse response = spy.httpRequest(METHOD_NAME, uri, new HashMap<>(), new byte[]{ }, "");
        // Then
        assertEquals(expectedHeader, response.getResponseHeaders());
    }
}