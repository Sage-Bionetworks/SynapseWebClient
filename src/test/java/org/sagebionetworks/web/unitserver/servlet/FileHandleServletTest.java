package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.table.RowReference;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.server.servlet.FileHandleServlet;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.unitserver.SynapseClientBaseTest;

public class FileHandleServletTest {

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
	FileHandleServlet servlet;

	@Before
	public void setup() throws IOException, SynapseException {
		MockitoAnnotations.initMocks(this);
		servlet = new FileHandleServlet();
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);

		WikiPage testPage = new WikiPage();
		testPage.setAttachmentFileHandleIds(new ArrayList<String>());
		when(mockSynapse.getWikiPage(any(WikiPageKey.class))).thenReturn(testPage);
		URL resolvedUrl = new URL("http://localhost/file.png");
		when(mockSynapse.getV2WikiAttachmentPreviewTemporaryUrl(any(WikiPageKey.class), anyString())).thenReturn(resolvedUrl);
		when(mockSynapse.getV2WikiAttachmentTemporaryUrl(any(WikiPageKey.class), anyString())).thenReturn(resolvedUrl);

		when(mockSynapse.getFileEntityPreviewTemporaryUrlForVersion(anyString(), anyLong())).thenReturn(resolvedUrl);
		when(mockSynapse.getFileEntityTemporaryUrlForVersion(anyString(), anyLong())).thenReturn(resolvedUrl);
		when(mockSynapse.getFileEntityPreviewTemporaryUrlForCurrentVersion(anyString())).thenReturn(resolvedUrl);
		when(mockSynapse.getFileEntityTemporaryUrlForCurrentVersion(anyString())).thenReturn(resolvedUrl);
		when(mockSynapse.getTableFileHandlePreviewTemporaryUrl(anyString(), any(RowReference.class), anyString())).thenReturn(resolvedUrl);
		when(mockSynapse.getTableFileHandleTemporaryUrl(anyString(), any(RowReference.class), anyString())).thenReturn(resolvedUrl);

		when(mockSynapse.getTeamIcon(anyString())).thenReturn(resolvedUrl);

		servlet.setSynapseProvider(mockSynapseProvider);
		servlet.setTokenProvider(mockTokenProvider);

		// Setup output stream and response
		when(mockResponse.getOutputStream()).thenReturn(responseOutputStream);
		SynapseClientBaseTest.setupTestEndpoints();
	}

	private void setupWiki() {
		when(mockRequest.getParameter(WebConstants.WIKI_OWNER_ID_PARAM_KEY)).thenReturn("syn296531");
		when(mockRequest.getParameter(WebConstants.WIKI_OWNER_TYPE_PARAM_KEY)).thenReturn(ObjectType.ENTITY.toString());
		when(mockRequest.getParameter(WebConstants.WIKI_ID_PARAM_KEY)).thenReturn("2");
		when(mockRequest.getParameter(WebConstants.WIKI_FILENAME_PARAM_KEY)).thenReturn("file.png");
	}

	private void setupFileEntity() {
		when(mockRequest.getParameter(WebConstants.ENTITY_PARAM_KEY)).thenReturn("syn296531");
		when(mockRequest.getParameter(WebConstants.ENTITY_VERSION_PARAM_KEY)).thenReturn("20");
	}

	private void setupTableEntityRow() {
		when(mockRequest.getParameter(WebConstants.ENTITY_PARAM_KEY)).thenReturn("syn296531");
		when(mockRequest.getParameter(WebConstants.TABLE_COLUMN_ID)).thenReturn("123");
		when(mockRequest.getParameter(WebConstants.TABLE_ROW_ID)).thenReturn("456");
		when(mockRequest.getParameter(WebConstants.TABLE_ROW_VERSION_NUMBER)).thenReturn("789");
	}

	private void setupTeam() {
		when(mockRequest.getParameter(WebConstants.TEAM_PARAM_KEY)).thenReturn("36");
	}

	@Test
	public void testDoGetLoggedInWikiAttachmentPreview() throws Exception {
		setupWiki();
		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY)).thenReturn("true");
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getV2WikiAttachmentPreviewTemporaryUrl(any(WikiPageKey.class), anyString());
		verify(mockResponse).sendRedirect(anyString());

		when(mockRequest.getParameter(WebConstants.WIKI_VERSION_PARAM_KEY)).thenReturn("1");
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getVersionOfV2WikiAttachmentPreviewTemporaryUrl(any(WikiPageKey.class), anyString(), anyLong());
		verify(mockResponse).sendRedirect(anyString());
	}

	@Test
	public void testDoGetLoggedInWikiAttachment() throws Exception {
		setupWiki();
		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY)).thenReturn("false");
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getV2WikiAttachmentTemporaryUrl(any(WikiPageKey.class), anyString());
		verify(mockResponse).sendRedirect(anyString());

		when(mockRequest.getParameter(WebConstants.WIKI_VERSION_PARAM_KEY)).thenReturn("1");
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getVersionOfV2WikiAttachmentTemporaryUrl(any(WikiPageKey.class), anyString(), anyLong());
		verify(mockResponse).sendRedirect(anyString());
	}


	@Test
	public void testDoGetLoggedInFileEntityPreview() throws Exception {
		String sessionToken = "fake";
		// set up general synapse client configuration test

		when(mockTokenProvider.getSessionToken()).thenReturn(sessionToken);

		setupFileEntity();

		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY)).thenReturn("true");
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, sessionToken)};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getFileEntityPreviewTemporaryUrlForVersion(anyString(), anyLong());
		verify(mockResponse).sendRedirect(anyString());

		// as an additional test, verify that synapse client is set up
		verify(mockSynapse).setAuthEndpoint(SynapseClientBaseTest.AUTH_BASE);
		verify(mockSynapse).setRepositoryEndpoint(SynapseClientBaseTest.REPO_BASE);
		verify(mockSynapse).setFileEndpoint(anyString());
		verify(mockSynapse).setSessionToken(sessionToken);
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
	public void testDoGetLoggedInTeamIcon() throws Exception {
		setupTeam();
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getTeamIcon(anyString());
		verify(mockResponse).sendRedirect(anyString());
	}


	@Test
	public void testDoGetLoggedOut() throws Exception {
		Cookie[] cookies = {};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);

		verify(mockResponse, Mockito.times(0)).sendRedirect(anyString());
	}

	@Test
	public void testDoGetLoggedInTableRowPreview() throws Exception {
		setupTableEntityRow();
		when(mockRequest.getParameter(WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY)).thenReturn("true");
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getTableFileHandlePreviewTemporaryUrl(anyString(), any(RowReference.class), anyString());
		verify(mockResponse).sendRedirect(anyString());
	}

	@Test
	public void testDoGetLoggedInTableRow() throws Exception {
		setupTableEntityRow();
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		servlet.doGet(mockRequest, mockResponse);
		verify(mockSynapse).getTableFileHandleTemporaryUrl(anyString(), any(RowReference.class), anyString());
		verify(mockResponse).sendRedirect(anyString());
	}

	@Test
	public void testDoGetLoggedInTableRowPreviewNotFullySpecifiedParams() throws Exception {
		setupTableEntityRow();
		when(mockRequest.getParameter(WebConstants.TABLE_COLUMN_ID)).thenReturn(null);
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		try {
			servlet.doGet(mockRequest, mockResponse);
			fail();
		} catch (ServletException e) {
			assertTrue(e.getMessage().contains("must be defined"));
		}
	}

	@Test
	public void testDoGetLoggedInTableRowPreviewIllegalArgument() throws Exception {
		setupTableEntityRow();
		when(mockRequest.getParameter(WebConstants.TABLE_ROW_ID)).thenReturn("a problem");
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		try {
			servlet.doGet(mockRequest, mockResponse);
			fail();
		} catch (ServletException e) {
			assertTrue(e.getMessage().contains("must be Long values"));
		}
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
		// now redirects to an error place
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockResponse).sendRedirect(captor.capture());
		String v = captor.getValue();
		assertTrue(v.contains("#!Error:"));
	}


}

