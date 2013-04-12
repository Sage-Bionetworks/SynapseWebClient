package org.sagebionetworks.web.unitserver.servlet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.servlet.FileHandleServlet;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;

public class FileHandleServletTest {

	HttpServletRequest mockRequest;
	HttpServletResponse mockResponse;
	ServiceUrlProvider mockUrlProvider;
	SynapseProvider mockSynapseProvider;
	TokenProvider mockTokenProvider;
	Synapse mockSynapse;
	ServletOutputStream responseOutputStream;
	FileHandleServlet servlet;

	@Before
	public void setup() throws IOException, SynapseException, JSONObjectAdapterException {
		servlet = new FileHandleServlet();

		// Mock synapse and provider so we don't need to worry about
		// unintentionally testing those classes
		mockSynapse = mock(Synapse.class);
		mockSynapseProvider = mock(SynapseProvider.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);

		FileHandleResults testResults = new FileHandleResults();
		WikiPage testPage = new WikiPage();
		testPage.setAttachmentFileHandleIds(new ArrayList<String>());
		when(mockSynapse.createFileHandles(any(List.class))).thenReturn(testResults);
		when(mockSynapse.getWikiPage(any(WikiPageKey.class))).thenReturn(testPage);
		URL resolvedUrl = new URL("http://localhost/file.png");
		when(mockSynapse.getWikiAttachmentPreviewTemporaryUrl(any(WikiPageKey.class), anyString())).thenReturn(resolvedUrl);
		when(mockSynapse.getWikiAttachmentTemporaryUrl(any(WikiPageKey.class), anyString())).thenReturn(resolvedUrl);

		when(mockSynapse.getFileEntityPreviewTemporaryUrlForVersion(anyString(), anyLong())).thenReturn(resolvedUrl);
		when(mockSynapse.getFileEntityTemporaryUrlForVersion(anyString(), anyLong())).thenReturn(resolvedUrl);
		when(mockSynapse.getFileEntityPreviewTemporaryUrlForCurrentVersion(anyString())).thenReturn(resolvedUrl);
		when(mockSynapse.getFileEntityTemporaryUrlForCurrentVersion(anyString())).thenReturn(resolvedUrl);


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
	}
	
	private void setupWiki() {
		when(mockRequest.getParameter(WebConstants.WIKI_OWNER_ID_PARAM_KEY)).thenReturn("syn296531");
		when(mockRequest.getParameter(WebConstants.WIKI_OWNER_TYPE_PARAM_KEY)).thenReturn(WidgetConstants.WIKI_OWNER_ID_ENTITY);
		when(mockRequest.getParameter(WebConstants.WIKI_ID_PARAM_KEY)).thenReturn("2");
		when(mockRequest.getParameter(WebConstants.WIKI_FILENAME_PARAM_KEY)).thenReturn("file.png");
	}
	
	private void setupFileEntity() {
		when(mockRequest.getParameter(WebConstants.ENTITY_PARAM_KEY)).thenReturn("syn296531");
		when(mockRequest.getParameter(WebConstants.ENTITY_VERSION_PARAM_KEY)).thenReturn("20");
	}

	@Test
	public void testDoGetLoggedInWikiAttachmentPreview() throws Exception {
		setupWiki();
		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY)).thenReturn("true");
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getWikiAttachmentPreviewTemporaryUrl(any(WikiPageKey.class), anyString());
		verify(mockResponse).sendRedirect(anyString());
	}
	
	@Test
	public void testDoGetLoggedInWikiAttachment() throws Exception {
		setupWiki();
		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY)).thenReturn("false");
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getWikiAttachmentTemporaryUrl(any(WikiPageKey.class), anyString());
		verify(mockResponse).sendRedirect(anyString());
	}


	@Test
	public void testDoGetLoggedInFileEntityPreview() throws Exception {
		setupFileEntity();
		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY)).thenReturn("true");
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getFileEntityPreviewTemporaryUrlForVersion(anyString(), anyLong());
		verify(mockResponse).sendRedirect(anyString());
	}
	
	@Test
	public void testDoGetLoggedInFileEntity() throws Exception {
		setupFileEntity();
		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY)).thenReturn("false");
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getFileEntityTemporaryUrlForVersion(anyString(), anyLong());
		verify(mockResponse).sendRedirect(anyString());
	}
	
	@Test
	public void testDoGetLoggedInFileEntityPreviewCurrentVersion() throws Exception {
		setupFileEntity();
		when(mockRequest.getParameter(WebConstants.ENTITY_VERSION_PARAM_KEY)).thenReturn(null);
		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY)).thenReturn("true");
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getFileEntityPreviewTemporaryUrlForCurrentVersion(anyString());
		verify(mockResponse).sendRedirect(anyString());
	}
	
	@Test
	public void testDoGetLoggedInFileEntityCurrentVersion() throws Exception {
		setupFileEntity();
		when(mockRequest.getParameter(WebConstants.ENTITY_VERSION_PARAM_KEY)).thenReturn(null);
		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY)).thenReturn("false");
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getFileEntityTemporaryUrlForCurrentVersion(anyString());
		verify(mockResponse).sendRedirect(anyString());
	}

	
	@Test
	public void testDoGetLoggedOut() throws Exception {
		Cookie[] cookies = {};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);

		verify(mockResponse, Mockito.times(0)).sendRedirect(anyString());
	}
}	//
