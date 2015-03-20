package org.sagebionetworks.web.unitserver.servlet.oaut;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.oauth.OAuthUrlRequest;
import org.sagebionetworks.repo.model.oauth.OAuthUrlResponse;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.oauth2.OAuth2Servlet;
import org.sagebionetworks.web.shared.WebConstants;

public class OAuth2ServletTest {
	HttpServletRequest mockRequest;
	HttpServletResponse mockResponse;
	ServiceUrlProvider mockServiceUrlProvider;
	SynapseProvider mockSynapseProvider;
	SynapseClient mockClient;
	String url;
	OAuth2Servlet servlet;
	
	@Before
	public void before(){
		mockRequest = Mockito.mock(HttpServletRequest.class);
		mockResponse = Mockito.mock(HttpServletResponse.class);
		mockServiceUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		mockSynapseProvider = Mockito.mock(SynapseProvider.class);
		mockClient = Mockito.mock(SynapseClient.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockClient);
		servlet = new OAuth2Servlet();
		servlet.setUrlProvider(mockServiceUrlProvider);
		servlet.setSynapseProvider(mockSynapseProvider);
		url = "http://127.0.0.1:8888/";
		when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(url));
	}

	@Test
	public void testAuthUrl() throws ServletException, IOException, SynapseException{
		ArgumentCaptor<OAuthUrlRequest> argument = ArgumentCaptor.forClass(OAuthUrlRequest.class);
		OAuthUrlResponse authResponse = new OAuthUrlResponse();
		authResponse.setAuthorizationUrl("http://google.com");
		when(mockClient.getOAuth2AuthenticationUrl(argument.capture())).thenReturn(authResponse);
		when(mockRequest.getParameter(WebConstants.OAUTH2_PROVIDER)).thenReturn(OAuthProvider.GOOGLE_OAUTH_2_0.name());
		// this case we have no code.
		when(mockRequest.getParameter(WebConstants.OAUTH2_CODE)).thenReturn(null);
		servlet.doGet(mockRequest, mockResponse);
	}
}
