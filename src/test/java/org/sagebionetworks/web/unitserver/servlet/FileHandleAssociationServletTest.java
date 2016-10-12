package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.table.RowReference;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.exceptions.IllegalArgumentException;
import org.sagebionetworks.web.server.servlet.FileHandleAssociationServlet;
import org.sagebionetworks.web.server.servlet.FileHandleServlet;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.util.tools.shared.Md5Utils;
import com.google.gwt.util.tools.shared.StringUtils;

public class FileHandleAssociationServletTest {

	HttpServletRequest mockRequest;
	HttpServletResponse mockResponse;
	ServiceUrlProvider mockUrlProvider;
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
		
		mockUrlProvider = mock(ServiceUrlProvider.class);
		mockTokenProvider = mock(TokenProvider.class);

		servlet.setServiceUrlProvider(mockUrlProvider);
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
		when(mockUrlProvider.getPrivateAuthBaseUrl()).thenReturn(authBaseUrl);
		when(mockUrlProvider.getRepositoryServiceUrl()).thenReturn(repoServiceUrl);
		when(mockTokenProvider.getSessionToken()).thenReturn(sessionToken);
		String xsrfToken = StringUtils.toHexString(Md5Utils.getMd5Digest(sessionToken.getBytes()));
		when(mockRequest.getParameter(WebConstants.XSRF_TOKEN_KEY)).thenReturn(xsrfToken);
		
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

