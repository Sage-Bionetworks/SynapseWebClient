package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.server.servlet.InitSessionServlet;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.unitserver.SynapseClientBaseTest;

public class InitSessionServletTest {

	@Mock
	HttpServletRequest mockRequest;
	@Mock
	HttpServletResponse mockResponse;
	@Mock
	ServletOutputStream responseOutputStream;
	@Mock
	PrintWriter mockPrintWriter;
	@Captor
	ArgumentCaptor<Cookie> cookieCaptor;
	@Captor
	ArgumentCaptor<byte[]> byteArrayCaptor;
	@Mock
	TokenProvider mockTokenProvider;

	InitSessionServlet servlet;
	public static final String TEST_SESSION_TOKEN = "abc-123";

	@Before
	public void setup() throws IOException, SynapseException {
		MockitoAnnotations.initMocks(this);
		servlet = new InitSessionServlet();

		// Setup output stream and response
		when(mockResponse.getWriter()).thenReturn(mockPrintWriter);
		when(mockResponse.getOutputStream()).thenReturn(responseOutputStream);
		when(mockRequest.getScheme()).thenReturn("https");
		when(mockRequest.getServerName()).thenReturn("www.synapse.org");
		servlet.setTokenProvider(mockTokenProvider);
		SynapseClientBaseTest.setupTestEndpoints();
	}

	private void setupSessionInRequest(String token) throws JSONObjectAdapterException, IOException {
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
		Session session = new Session();
		session.setSessionToken(token);
		session.writeToJSONObject(adapter);
		BufferedReader br = new BufferedReader(new StringReader(adapter.toJSONString()));
		when(mockRequest.getReader()).thenReturn(br);
	}

	@Test
	public void testSetSessionCookie() throws Exception {
		setupSessionInRequest(TEST_SESSION_TOKEN);

		servlet.doPost(mockRequest, mockResponse);

		// verify the response Set-Cookie values
		verify(mockResponse).addCookie(cookieCaptor.capture());
		Cookie cookie = cookieCaptor.getValue();
		assertEquals(CookieKeys.USER_LOGIN_TOKEN, cookie.getName());
		assertEquals(TEST_SESSION_TOKEN, cookie.getValue());
		assertEquals(InitSessionServlet.ONE_DAY_IN_SECONDS, cookie.getMaxAge());
		assertEquals(InitSessionServlet.SYNAPSE_ORG, cookie.getDomain());
		assertTrue(cookie.isHttpOnly());
		assertTrue(cookie.getSecure());
		assertEquals(InitSessionServlet.ROOT_PATH, cookie.getPath());

		verify(mockResponse).setContentType(InitSessionServlet.JSON_CONTENT_TYPE);
		verify(mockPrintWriter).print(InitSessionServlet.EMPTY_JSON);
		verify(mockPrintWriter).flush();
	}

	@Test
	public void testClearSessionCookie() throws Exception {
		setupSessionInRequest(WebConstants.EXPIRE_SESSION_TOKEN);
		// also verify secure is not set if http
		when(mockRequest.getScheme()).thenReturn("http");
		// and verify the cookie is locked down to your domain if not a .synapse.org subdomain.
		String serverName = "www.mysynapseportal.com";
		when(mockRequest.getServerName()).thenReturn(serverName);

		servlet.doPost(mockRequest, mockResponse);

		// verify the response Set-Cookie values
		verify(mockResponse).addCookie(cookieCaptor.capture());
		Cookie cookie = cookieCaptor.getValue();
		assertEquals(CookieKeys.USER_LOGIN_TOKEN, cookie.getName());
		assertEquals(WebConstants.EXPIRE_SESSION_TOKEN, cookie.getValue());
		assertEquals(0, cookie.getMaxAge());
		assertNull(cookie.getDomain());
		assertTrue(cookie.isHttpOnly());
		assertFalse(cookie.getSecure());
		assertEquals(InitSessionServlet.ROOT_PATH, cookie.getPath());
	}

	@Test
	public void testDoGetSession() throws Exception {
		String sessionToken = "abc123-fake";
		when(mockTokenProvider.getSessionToken()).thenReturn(sessionToken);

		servlet.doGet(mockRequest, mockResponse);

		verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
		verify(responseOutputStream).write(byteArrayCaptor.capture());
		assertEquals(sessionToken, new String(byteArrayCaptor.getValue(), "UTF-8"));
		verify(responseOutputStream).flush();
	}

	@Test
	public void testDoGetSessionNull() throws Exception {
		when(mockTokenProvider.getSessionToken()).thenReturn(null);

		servlet.doGet(mockRequest, mockResponse);

		verify(mockResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		verifyZeroInteractions(responseOutputStream);
	}
}

