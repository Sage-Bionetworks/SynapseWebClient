package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
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
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.server.servlet.FileHandleAssociationServlet;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;

public class FileHandleAssociationServletTest {

	HttpServletRequest mockRequest;
	HttpServletResponse mockResponse;
	SynapseProvider mockSynapseProvider;
	TokenProvider mockTokenProvider;
	SynapseClient mockSynapse;
	ServletOutputStream responseOutputStream;
	FileHandleAssociationServlet servlet;

	String objectId = "22";
	String objectType = FileHandleAssociateType.VerificationSubmission.toString();
	String fileHandleId = "333";
	
	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		servlet = new FileHandleAssociationServlet();

		// Mock synapse and provider so we don't need to worry about
		// unintentionally testing those classes
		mockSynapse = mock(SynapseClient.class);
		mockSynapseProvider = mock(SynapseProvider.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);

		URL resolvedUrl = new URL("http://localhost/file.png");
		when(mockSynapse.getFileURL(any(FileHandleAssociation.class))).thenReturn(resolvedUrl);
		
		mockTokenProvider = mock(TokenProvider.class);

		servlet.setSynapseProvider(mockSynapseProvider);
		servlet.setTokenProvider(mockTokenProvider);

		// Setup output stream and response
		responseOutputStream = mock(ServletOutputStream.class);
		mockResponse = mock(HttpServletResponse.class);
		when(mockResponse.getOutputStream()).thenReturn(responseOutputStream);

		// Setup request
		mockRequest = mock(HttpServletRequest.class);
		
		when(mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_ID_PARAM_KEY)).thenReturn(objectId);
		when(mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_TYPE_PARAM_KEY)).thenReturn(objectType);
		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_ID_PARAM_KEY)).thenReturn(fileHandleId);
		when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("https://www.synapse.org/"));
		when(mockRequest.getRequestURI()).thenReturn("");
		when(mockRequest.getContextPath()).thenReturn("");
	}
	
	@Test
	public void testDoGet() throws Exception {
		String sessionToken = "fake";

		//set up general synapse client configuration test
		String authBaseUrl = "authbase";
		String repoServiceUrl = "repourl";
		System.setProperty(StackEndpoints.REPO_ENDPOINT_KEY, repoServiceUrl);
		System.setProperty(StackEndpoints.AUTH_ENDPOINT_KEY, authBaseUrl);
		when(mockTokenProvider.getSessionToken()).thenReturn(sessionToken);
		
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, sessionToken)};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		
		ArgumentCaptor<FileHandleAssociation> fhaCaptor = ArgumentCaptor.forClass(FileHandleAssociation.class);
		verify(mockSynapse).getFileURL(fhaCaptor.capture());
		verify(mockResponse).sendRedirect(anyString());
		FileHandleAssociation fha = fhaCaptor.getValue();
		assertEquals(objectId, fha.getAssociateObjectId());
		assertEquals(FileHandleAssociateType.VerificationSubmission, fha.getAssociateObjectType());
		assertEquals(fileHandleId, fha.getFileHandleId());
		
		//as an additional test, verify that synapse client is set up
		verify(mockSynapse).setAuthEndpoint(authBaseUrl);
		verify(mockSynapse).setRepositoryEndpoint(repoServiceUrl);
		verify(mockSynapse).setFileEndpoint(anyString());
		verify(mockSynapse).setSessionToken(sessionToken);
		
		//look for no cache headers
		verify(mockResponse).setHeader(eq(WebConstants.CACHE_CONTROL_KEY), eq(WebConstants.CACHE_CONTROL_VALUE_NO_CACHE));
		verify(mockResponse).setHeader(eq(WebConstants.PRAGMA_KEY), eq(WebConstants.NO_CACHE_VALUE));
		verify(mockResponse).setDateHeader(eq(WebConstants.EXPIRES_KEY), eq(0L));
	}
	
	@Test
	public void testDoGetError() throws Exception {
		String errorMessage = "An error from the service call";
		when(mockSynapse.getFileURL(any(FileHandleAssociation.class))).thenThrow(new SynapseForbiddenException(errorMessage));
		servlet.doGet(mockRequest, mockResponse);
		
		//redirects to an error place
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockResponse).sendRedirect(captor.capture());
		String v = captor.getValue();
		assertTrue(v.contains("#!Error:"));
	}

	
}	

