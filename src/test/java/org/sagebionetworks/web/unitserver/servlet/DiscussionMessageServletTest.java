package org.sagebionetworks.web.unitserver.servlet;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseBadRequestException;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.server.servlet.DiscussionMessageServlet;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;
import org.springframework.test.util.ReflectionTestUtils;

public class DiscussionMessageServletTest {
	@Mock
	HttpServletRequest mockRequest;
	@Mock
	HttpServletResponse mockResponse;
	@Mock
	ServiceUrlProvider mockUrlProvider;
	@Mock
	SynapseProvider mockSynapseProvider;
	@Mock
	TokenProvider mockTokenProvider;
	@Mock
	SynapseClient mockSynapse;
	ServletOutputStream responseOutputStream;
	DiscussionMessageServlet servlet;

	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		servlet = new DiscussionMessageServlet();
		servlet.setServiceUrlProvider(mockUrlProvider);
		ReflectionTestUtils.setField(servlet, "synapseProvider", mockSynapseProvider);
		ReflectionTestUtils.setField(servlet, "tokenProvider", mockTokenProvider);

		URL resolvedUrl = new URL("http://localhost/file.png");
		when(mockSynapse.getThreadMessageUrl(anyString())).thenReturn(resolvedUrl);
		when(mockSynapse.getReplyMessageUrl(anyString())).thenReturn(resolvedUrl);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		when(mockResponse.getOutputStream()).thenReturn(responseOutputStream);
		when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("https://www.synapse.org/"));
	}
	
	@Test
	public void testDoGetThreadMessage() throws Exception {
		String sessionToken = "fake";

		//set up general synapse client configuration test
		String authBaseUrl = "authbase";
		String repoServiceUrl = "repourl";
		when(mockUrlProvider.getPrivateAuthBaseUrl()).thenReturn(authBaseUrl);
		when(mockUrlProvider.getRepositoryServiceUrl()).thenReturn(repoServiceUrl);
		when(mockTokenProvider.getSessionToken()).thenReturn(sessionToken);

		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, sessionToken)};
		when(mockRequest.getCookies()).thenReturn(cookies);
		when(mockRequest.getParameter(WebConstants.MESSAGE_KEY_PARAM)).thenReturn("key");
		when(mockRequest.getParameter(WebConstants.TYPE_PARAM)).thenReturn(WebConstants.THREAD_TYPE);
		servlet.doGet(mockRequest, mockResponse);

		verify(mockSynapse).getThreadMessageUrl(anyString());
		verify(mockResponse).sendRedirect(anyString());

		//as an additional test, verify that synapse client is set up
		verify(mockSynapse).setAuthEndpoint(authBaseUrl);
		verify(mockSynapse).setRepositoryEndpoint(repoServiceUrl);
		verify(mockSynapse).setFileEndpoint(anyString());
		verify(mockSynapse).setSessionToken(sessionToken);
	}

	@Test
	public void testDoGetReplyMessage() throws Exception {
		String sessionToken = "fake";

		//set up general synapse client configuration test
		String authBaseUrl = "authbase";
		String repoServiceUrl = "repourl";
		when(mockUrlProvider.getPrivateAuthBaseUrl()).thenReturn(authBaseUrl);
		when(mockUrlProvider.getRepositoryServiceUrl()).thenReturn(repoServiceUrl);
		when(mockTokenProvider.getSessionToken()).thenReturn(sessionToken);

		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, sessionToken)};
		when(mockRequest.getCookies()).thenReturn(cookies);
		when(mockRequest.getParameter(WebConstants.MESSAGE_KEY_PARAM)).thenReturn("key");
		when(mockRequest.getParameter(WebConstants.TYPE_PARAM)).thenReturn(WebConstants.REPLY_TYPE);
		servlet.doGet(mockRequest, mockResponse);

		verify(mockSynapse).getReplyMessageUrl(anyString());
		verify(mockResponse).sendRedirect(anyString());

		//as an additional test, verify that synapse client is set up
		verify(mockSynapse).setAuthEndpoint(authBaseUrl);
		verify(mockSynapse).setRepositoryEndpoint(repoServiceUrl);
		verify(mockSynapse).setFileEndpoint(anyString());
		verify(mockSynapse).setSessionToken(sessionToken);
	}

	@Test (expected=IllegalArgumentException.class)
	public void testDoGetUnsupportedType() throws Exception {
		String sessionToken = "fake";

		//set up general synapse client configuration test
		String authBaseUrl = "authbase";
		String repoServiceUrl = "repourl";
		when(mockUrlProvider.getPrivateAuthBaseUrl()).thenReturn(authBaseUrl);
		when(mockUrlProvider.getRepositoryServiceUrl()).thenReturn(repoServiceUrl);
		when(mockTokenProvider.getSessionToken()).thenReturn(sessionToken);

		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, sessionToken)};
		when(mockRequest.getCookies()).thenReturn(cookies);
		when(mockRequest.getParameter(WebConstants.MESSAGE_KEY_PARAM)).thenReturn("key");
		when(mockRequest.getParameter(WebConstants.TYPE_PARAM)).thenReturn("type");
		servlet.doGet(mockRequest, mockResponse);
	}

	@Test
	public void testDoGetError() throws Exception {
		String sessionToken = "fake";

		//set up general synapse client configuration test
		String authBaseUrl = "authbase";
		String repoServiceUrl = "repourl";
		when(mockUrlProvider.getPrivateAuthBaseUrl()).thenReturn(authBaseUrl);
		when(mockUrlProvider.getRepositoryServiceUrl()).thenReturn(repoServiceUrl);
		when(mockTokenProvider.getSessionToken()).thenReturn(sessionToken);

		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, sessionToken)};
		when(mockRequest.getCookies()).thenReturn(cookies);
		when(mockRequest.getParameter(WebConstants.MESSAGE_KEY_PARAM)).thenReturn("key");
		when(mockRequest.getParameter(WebConstants.TYPE_PARAM)).thenReturn(WebConstants.THREAD_TYPE);
		when(mockSynapse.getThreadMessageUrl(anyString())).thenThrow(new SynapseBadRequestException());
		servlet.doGet(mockRequest, mockResponse);

		verify(mockResponse).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
	}

}
