package org.sagebionetworks.web.unitserver.servlet.oaut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.client.exceptions.SynapseServiceUnavailable;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.oauth.OAuthUrlRequest;
import org.sagebionetworks.repo.model.oauth.OAuthValidationRequest;
import org.sagebionetworks.repo.model.principal.PrincipalAlias;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.oauth2.OAuth2AliasServlet;
import org.sagebionetworks.web.shared.WebConstants;

public class OAuth2AliasServletTest {
	HttpServletRequest mockRequest;
	HttpServletResponse mockResponse;
	SynapseProvider mockSynapseProvider;
	SynapseClient mockClient;
	String url;
	OAuth2AliasServlet servlet;

	@Before
	public void before() {
		mockRequest = Mockito.mock(HttpServletRequest.class);
		mockResponse = Mockito.mock(HttpServletResponse.class);
		mockSynapseProvider = Mockito.mock(SynapseProvider.class);
		mockClient = Mockito.mock(SynapseClient.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockClient);
		servlet = new OAuth2AliasServlet();
		servlet.setSynapseProvider(mockSynapseProvider);
		url = "http://127.0.0.1:8888/";
		when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(url));
		when(mockRequest.getRequestURI()).thenReturn("");
		when(mockRequest.getContextPath()).thenReturn("");
	}

	@Test
	public void testCreateRedirectUrl() throws ServletException, IOException, SynapseException {
		String url = servlet.createRedirectUrl(mockRequest, OAuthProvider.ORCID);
		assertEquals("http://127.0.0.1:8888/?oauth2provider=ORCID", url);
	}

	@Test
	public void testValidate() throws ServletException, IOException, SynapseException {
		ArgumentCaptor<OAuthValidationRequest> argument = ArgumentCaptor.forClass(OAuthValidationRequest.class);
		PrincipalAlias alias = new PrincipalAlias();
		alias.setPrincipalId(123L);
		when(mockClient.bindOAuthProvidersUserId(argument.capture())).thenReturn(alias);
		when(mockRequest.getParameter(WebConstants.OAUTH2_PROVIDER)).thenReturn(OAuthProvider.ORCID.name());
		String authCode = "authCode";
		when(mockRequest.getParameter(WebConstants.OAUTH2_CODE)).thenReturn(authCode);
		servlet.doGet(mockRequest, mockResponse);
		OAuthValidationRequest request = argument.getValue();
		assertNotNull(request);
		assertEquals(OAuthProvider.ORCID, request.getProvider());
		assertEquals(authCode, request.getAuthenticationCode());
		verify(mockResponse).sendRedirect("/#!Profile:oauth_bound");
	}

	@Test
	public void testValidateNotFound() throws ServletException, IOException, SynapseException {
		ArgumentCaptor<OAuthValidationRequest> argument = ArgumentCaptor.forClass(OAuthValidationRequest.class);
		when(mockClient.bindOAuthProvidersUserId(argument.capture())).thenThrow(new SynapseNotFoundException("error message"));
		when(mockRequest.getParameter(WebConstants.OAUTH2_PROVIDER)).thenReturn(OAuthProvider.ORCID.name());
		String authCode = "authCode";
		when(mockRequest.getParameter(WebConstants.OAUTH2_CODE)).thenReturn(authCode);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockResponse).sendRedirect(contains("/#!Error:"));
	}

	@Test
	public void testDoGetError() throws Exception {
		String errorMessage = "An error from the service call";
		ArgumentCaptor<OAuthValidationRequest> argument = ArgumentCaptor.forClass(OAuthValidationRequest.class);
		when(mockClient.bindOAuthProvidersUserId(argument.capture())).thenThrow(new SynapseNotFoundException(errorMessage));
		when(mockRequest.getParameter(WebConstants.OAUTH2_PROVIDER)).thenReturn(OAuthProvider.ORCID.name());
		String authCode = "authCode";
		when(mockRequest.getParameter(WebConstants.OAUTH2_CODE)).thenReturn(authCode);

		servlet.doGet(mockRequest, mockResponse);

		// redirects to an error place
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockResponse).sendRedirect(captor.capture());
		String v = captor.getValue();
		assertTrue(v.contains("#!Error:"));
	}

	@Test
	public void testDoGetErrorSynapseDown() throws Exception {
		SynapseServiceUnavailable exception = new SynapseServiceUnavailable("error message");
		when(mockClient.getOAuth2AuthenticationUrl(any(OAuthUrlRequest.class))).thenThrow(exception);
		when(mockRequest.getParameter(WebConstants.OAUTH2_PROVIDER)).thenReturn(OAuthProvider.ORCID.name());

		servlet.doGet(mockRequest, mockResponse);

		// redirects to an error place
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockResponse).sendRedirect(captor.capture());
		String v = captor.getValue();
		assertTrue(v.contains("#!Down:0"));
	}


}
