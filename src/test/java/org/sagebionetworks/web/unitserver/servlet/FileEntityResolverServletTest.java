package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.table.RowReference;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.server.servlet.FileEntityResolverServlet;
import org.sagebionetworks.web.server.servlet.FileHandleServlet;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;

public class FileEntityResolverServletTest {

	HttpServletRequest mockRequest;
	HttpServletResponse mockResponse;
	ServiceUrlProvider mockUrlProvider;
	SynapseProvider mockSynapseProvider;
	TokenProvider mockTokenProvider;
	SynapseClient mockSynapse;
	PrintWriter responseOutputWriter;
	FileEntityResolverServlet servlet;
	String resolvedUrlString = "http://localhost/file.png";
	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		servlet = new FileEntityResolverServlet();

		// Mock synapse and provider so we don't need to worry about
		// unintentionally testing those classes
		mockSynapse = mock(SynapseClient.class);
		mockSynapseProvider = mock(SynapseProvider.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);

		URL resolvedUrl = new URL(resolvedUrlString);
		when(mockSynapse.getFileEntityTemporaryUrlForVersion(anyString(), anyLong())).thenReturn(resolvedUrl);
		
		mockUrlProvider = mock(ServiceUrlProvider.class);
		mockTokenProvider = mock(TokenProvider.class);

		servlet.setServiceUrlProvider(mockUrlProvider);
		servlet.setSynapseProvider(mockSynapseProvider);
		servlet.setTokenProvider(mockTokenProvider);

		// Setup output stream and response
		responseOutputWriter = mock(PrintWriter.class);
		mockResponse = mock(HttpServletResponse.class);
		when(mockResponse.getWriter()).thenReturn(responseOutputWriter);

		// Setup request
		mockRequest = mock(HttpServletRequest.class);
	}
	
	private void setupFileEntity() {
		when(mockRequest.getParameter(WebConstants.ENTITY_PARAM_KEY)).thenReturn("syn296531");
		when(mockRequest.getParameter(WebConstants.ENTITY_VERSION_PARAM_KEY)).thenReturn("20");
	}
	

	@Test
	public void testDoGetLoggedInFileEntity() throws Exception {
		String sessionToken = "fake";

		//set up general synapse client configuration test
		String authBaseUrl = "authbase";
		String repoServiceUrl = "repourl";
		when(mockUrlProvider.getPrivateAuthBaseUrl()).thenReturn(authBaseUrl);
		when(mockUrlProvider.getRepositoryServiceUrl()).thenReturn(repoServiceUrl);
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
		
		//as an additional test, verify that synapse client is set up
		verify(mockSynapse).setAuthEndpoint(authBaseUrl);
		verify(mockSynapse).setRepositoryEndpoint(repoServiceUrl);
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
		//sends back error
		verify(mockResponse).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
	}

	
}	

