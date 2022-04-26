import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import android.net.Uri;
import com.mobilecoin.lib.network.services.http.Requester.HttpRequester;
import com.mobilecoin.lib.network.services.http.Requester.Requester;
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