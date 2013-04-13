package org.sagebionetworks.web.unitserver.servlet;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.repo.model.attachment.PresignedUrl;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.server.servlet.FileAttachmentServlet;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;

public class FileAttachmentServletTest {

	HttpServletRequest mockRequest;
	HttpServletResponse mockResponse;
	ServiceUrlProvider mockUrlProvider;
	SynapseProvider mockSynapseProvider;
	TokenProvider mockTokenProvider;
	PresignedUrl mockUrl;
	Synapse mockSynapse;
	ServletOutputStream responseOutputStream;
	FileAttachmentServlet servlet;

	@Before
	public void setup() throws IOException {
		servlet = new FileAttachmentServlet();

		// Mock synapse and provider so we don't need to worry about
		// unintentionally testing those classes
		mockSynapse = mock(Synapse.class);
		mockSynapseProvider = mock(SynapseProvider.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);

		mockUrl = mock(PresignedUrl.class);
		when(mockUrl.getPresignedUrl()).thenReturn("http://presigned.url.com");

		try {
			when(mockSynapse.waitForPreviewToBeCreated(anyString(), anyString(), anyInt())).thenReturn(mockUrl);
			when(mockSynapse.createAttachmentPresignedUrl(anyString(), anyString())).thenReturn(mockUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}

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

		when(mockRequest.getParameter(WebConstants.ENTITY_PARAM_KEY)).thenReturn("syn296531");	//
		when(mockRequest.getParameter(WebConstants.TOKEN_ID_PARAM_KEY)).thenReturn("296533/ToolsOfConvivialityBreakout.pdf");

	}

	@Test
	public void testDoGetLoggedIn() throws Exception {
		Cookie[] cookies = {new Cookie(CookieKeys.USER_LOGIN_TOKEN, "fake")};
		when(mockRequest.getCookies()).thenReturn(cookies);
		when(mockRequest.getParameter(WebConstants.WAIT_FOR_URL)).thenReturn("true");
		servlet.doGet(mockRequest, mockResponse);

		verify(mockResponse).sendRedirect(anyString());
	}

	@Test
	public void testDoGetLoggedOut() throws Exception {
		Cookie[] cookies = {};
		when(mockRequest.getCookies()).thenReturn(cookies);
		when(mockRequest.getParameter(WebConstants.WAIT_FOR_URL)).thenReturn("true");
		servlet.doGet(mockRequest, mockResponse);

		verify(mockResponse).sendRedirect(anyString());
	}

	@Test
	public void testDoGetWaitString() throws Exception {
		Cookie[] cookies = {};
		when(mockRequest.getCookies()).thenReturn(cookies);
		when(mockRequest.getParameter(WebConstants.WAIT_FOR_URL)).thenReturn("false");

		servlet.doGet(mockRequest, mockResponse);

		verify(mockResponse).sendRedirect(anyString());

	}
}	//
