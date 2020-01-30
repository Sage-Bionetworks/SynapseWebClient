package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.server.servlet.FileHandleAssociationServlet;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.unitserver.SynapseClientBaseTest;

public class FileHandleAssociationServletTest {
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
	ServletOutputStream responseOutputStream;
	FileHandleAssociationServlet servlet;

	String objectId = "22";
	String objectType = FileHandleAssociateType.VerificationSubmission.toString();
	String fileHandleId = "333";
	String sessionToken = "fake";
	URL resolvedUrl, rawFileUrl;

	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		servlet = new FileHandleAssociationServlet();

		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);

		resolvedUrl = new URL("http://localhost/file.png");
		when(mockSynapse.getFileURL(any(FileHandleAssociation.class))).thenReturn(resolvedUrl);

		rawFileUrl = new URL("http://raw.file.url/");
		when(mockSynapse.getFileHandleTemporaryUrl(anyString())).thenReturn(rawFileUrl);

		servlet.setSynapseProvider(mockSynapseProvider);
		servlet.setTokenProvider(mockTokenProvider);
		when(mockResponse.getOutputStream()).thenReturn(responseOutputStream);
		when(mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_ID_PARAM_KEY)).thenReturn(objectId);
		when(mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_TYPE_PARAM_KEY)).thenReturn(objectType);
		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_ID_PARAM_KEY)).thenReturn(fileHandleId);
		when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("https://www.synapse.org/"));
		when(mockRequest.getRequestURI()).thenReturn("");
		when(mockRequest.getContextPath()).thenReturn("");
		when(mockTokenProvider.getSessionToken()).thenReturn(sessionToken);
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, sessionToken)};
		when(mockRequest.getCookies()).thenReturn(cookies);

		SynapseClientBaseTest.setupTestEndpoints();
	}

	@Test
	public void testDoGet() throws Exception {
		servlet.doGet(mockRequest, mockResponse);

		ArgumentCaptor<FileHandleAssociation> fhaCaptor = ArgumentCaptor.forClass(FileHandleAssociation.class);
		verify(mockSynapse).getFileURL(fhaCaptor.capture());
		verify(mockResponse).sendRedirect(resolvedUrl.toString());
		FileHandleAssociation fha = fhaCaptor.getValue();
		assertEquals(objectId, fha.getAssociateObjectId());
		assertEquals(FileHandleAssociateType.VerificationSubmission, fha.getAssociateObjectType());
		assertEquals(fileHandleId, fha.getFileHandleId());

		// as an additional test, verify that synapse client is set up
		verify(mockSynapse).setAuthEndpoint(SynapseClientBaseTest.AUTH_BASE);
		verify(mockSynapse).setRepositoryEndpoint(SynapseClientBaseTest.REPO_BASE);
		verify(mockSynapse).setFileEndpoint(anyString());
		verify(mockSynapse).setSessionToken(sessionToken);

		// look for 30 second cache header
		verify(mockResponse).setHeader(WebConstants.CACHE_CONTROL_KEY, "max-age=" + FileHandleAssociationServlet.CACHE_TIME_SECONDS);
	}

	@Test
	public void testDoGetRawFileHandle() throws Exception {
		when(mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_ID_PARAM_KEY)).thenReturn(null);
		when(mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_TYPE_PARAM_KEY)).thenReturn(null);


		servlet.doGet(mockRequest, mockResponse);

		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockSynapse).getFileHandleTemporaryUrl(stringCaptor.capture());
		verify(mockResponse).sendRedirect(rawFileUrl.toString());
		assertEquals(fileHandleId, stringCaptor.getValue());

		// look for 30 second cache header
		verify(mockResponse).setHeader(WebConstants.CACHE_CONTROL_KEY, "max-age=" + FileHandleAssociationServlet.CACHE_TIME_SECONDS);
	}

	@Test
	public void testDoGetError() throws Exception {
		String errorMessage = "An error from the service call";
		when(mockSynapse.getFileURL(any(FileHandleAssociation.class))).thenThrow(new SynapseForbiddenException(errorMessage));
		servlet.doGet(mockRequest, mockResponse);

		// redirects to an error place
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockResponse).sendRedirect(captor.capture());
		String v = captor.getValue();
		assertTrue(v.contains("#!Error:"));
	}


}

