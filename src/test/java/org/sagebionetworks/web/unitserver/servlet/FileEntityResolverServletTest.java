package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.server.servlet.FileEntityResolverServlet;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.unitserver.SynapseClientBaseTest;

public class FileEntityResolverServletTest {
	@Mock
	HttpServletRequest mockRequest;
	@Mock
	HttpServletResponse mockResponse;
	@Mock
	SynapseProvider mockSynapseProvider;
	@Mock
	TokenProvider mockTokenProvider;
	@Mock
	SynapseClient mockSynapse;
	@Mock
	PrintWriter responseOutputWriter;
	FileEntityResolverServlet servlet;
	String resolvedUrlString = "http://localhost/file.png";
	@Captor
	ArgumentCaptor<String> stringCaptor;

	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		servlet = new FileEntityResolverServlet();

		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);

		URL resolvedUrl = new URL(resolvedUrlString);
		when(mockSynapse.getFileEntityTemporaryUrlForVersion(anyString(), anyLong())).thenReturn(resolvedUrl);
		servlet.setSynapseProvider(mockSynapseProvider);
		servlet.setTokenProvider(mockTokenProvider);

		// Setup output stream and response
		when(mockResponse.getWriter()).thenReturn(responseOutputWriter);

		SynapseClientBaseTest.setupTestEndpoints();
	}

	private void setupFileEntity() {
		when(mockRequest.getParameter(WebConstants.ENTITY_PARAM_KEY)).thenReturn("syn296531");
		when(mockRequest.getParameter(WebConstants.ENTITY_VERSION_PARAM_KEY)).thenReturn("20");
	}


	@Test
	public void testDoGetLoggedInFileEntity() throws Exception {
		String sessionToken = "fake";
		when(mockTokenProvider.getSessionToken()).thenReturn(sessionToken);

		setupFileEntity();
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, sessionToken)};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getFileEntityTemporaryUrlForVersion(anyString(), anyLong());
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(responseOutputWriter).println(captor.capture());
		String responseText = captor.getValue();
		assertTrue(responseText.contains("url"));
		assertTrue(responseText.contains(resolvedUrlString));

		// as an additional test, verify that synapse client is set up
		verify(mockSynapse).setAuthEndpoint(SynapseClientBaseTest.AUTH_BASE);
		verify(mockSynapse).setRepositoryEndpoint(SynapseClientBaseTest.REPO_BASE);
		verify(mockSynapse).setFileEndpoint(anyString());
		verify(mockSynapse).setSessionToken(sessionToken);
	}



	@Test
	public void testDoGetLoggedOut() throws Exception {
		Cookie[] cookies = {};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);

		verify(responseOutputWriter, times(0)).println(anyString());
	}


	@Test
	public void testNoCacheHeaders() throws Exception {
		setupFileEntity();
		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY)).thenReturn("true");
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);

		verify(mockResponse).setHeader(eq(WebConstants.CACHE_CONTROL_KEY), eq(WebConstants.CACHE_CONTROL_VALUE_NO_CACHE));
		verify(mockResponse).setHeader(eq(WebConstants.PRAGMA_KEY), eq(WebConstants.NO_CACHE_VALUE));
		verify(mockResponse).setDateHeader(eq(WebConstants.EXPIRES_KEY), eq(0L));
	}


	@Test
	public void testDoGetError() throws Exception {
		String errorMessage = "An error from the service call";
		setupFileEntity();
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		when(mockSynapse.getFileEntityTemporaryUrlForVersion(anyString(), anyLong())).thenThrow(new SynapseForbiddenException(errorMessage));
		when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("https://www.synapse.org/"));
		when(mockRequest.getRequestURI()).thenReturn("");
		when(mockRequest.getContextPath()).thenReturn("");
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getFileEntityTemporaryUrlForVersion(anyString(), anyLong());
		// sends back error
		verify(mockResponse).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), stringCaptor.capture());
		assertTrue(stringCaptor.getValue().contains(errorMessage));
	}


}

