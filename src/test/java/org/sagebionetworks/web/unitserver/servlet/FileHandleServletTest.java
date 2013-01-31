package org.sagebionetworks.web.unitserver.servlet;

import static org.mockito.Matchers.any;
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
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.servlet.FileHandleServlet;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;

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

		when(mockRequest.getParameter(DisplayUtils.WIKI_OWNER_ID_PARAM_KEY)).thenReturn("syn296531");
		when(mockRequest.getParameter(DisplayUtils.WIKI_OWNER_TYPE_PARAM_KEY)).thenReturn(WidgetConstants.WIKI_OWNER_ID_ENTITY);
		when(mockRequest.getParameter(DisplayUtils.WIKI_ID_PARAM_KEY)).thenReturn("2");
		when(mockRequest.getParameter(DisplayUtils.WIKI_FILENAME_PARAM_KEY)).thenReturn("file.png");
	}

	@Test
	public void testDoGetLoggedIn() throws Exception {
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		when(mockRequest.getParameter(DisplayUtils.WAIT_FOR_URL)).thenReturn("true");
		servlet.doGet(mockRequest, mockResponse);

		verify(mockResponse).sendRedirect(anyString());
	}

	@Test
	public void testDoGetLoggedOut() throws Exception {
		Cookie[] cookies = {};
		when(mockRequest.getCookies()).thenReturn(cookies);
		when(mockRequest.getParameter(DisplayUtils.WAIT_FOR_URL)).thenReturn("true");
		servlet.doGet(mockRequest, mockResponse);

		verify(mockResponse).sendRedirect(anyString());
	}
}	//
