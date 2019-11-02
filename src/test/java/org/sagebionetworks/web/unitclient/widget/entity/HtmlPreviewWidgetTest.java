package org.sagebionetworks.web.unitclient.widget.entity;

import static com.google.gwt.http.client.RequestBuilder.GET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.shared.WebConstants.TEXT_HTML_CHARSET_UTF8;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.HtmlPreviewView;
import org.sagebionetworks.web.client.widget.entity.renderer.HtmlPreviewWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the preview widget.
 * 
 * @author jayhodgson
 *
 */
public class HtmlPreviewWidgetTest {
	HtmlPreviewWidget previewWidget;
	@Mock
	HtmlPreviewView mockView;
	@Mock
	RequestBuilderWrapper mockRequestBuilder;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	PresignedURLAsyncHandler mockPresignedURLAsyncHandler;

	@Mock
	Response mockResponse;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	FileResult mockFileResult;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	FileHandle mockFileHandle;
	@Captor
	ArgumentCaptor<FileHandleAssociation> fhaCaptor;
	@Captor
	ArgumentCaptor<Throwable> exceptionCaptor;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Captor
	ArgumentCaptor<String> stringCaptor;

	public static final String HTML = "<html><body><img src=a onerror=\"javascript:alert('running my js')\" /><p>hello</p></body></html>";
	public static final String SANITIZED_HTML = "<html><body><p>hello</p></body></html>";

	public static final String ENTITY_ID = "syn20923";
	public static final String FILE_HANDLE_ID = "9992782";
	public static final String CREATED_BY = "8992983";
	public static final String PRESIGNED_URL = "https://s3.path/test.html";
	public static final String FRIENDLY_FILE_SIZE = "a friendly file size";

	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockGWT.getFriendlySize(anyDouble(), anyBoolean())).thenReturn(FRIENDLY_FILE_SIZE);
		previewWidget = new HtmlPreviewWidget(mockView, mockPresignedURLAsyncHandler, mockSynapseJSNIUtils, mockRequestBuilder, mockSynapseAlert, mockSynapseClient, mockPopupUtils, mockGWT);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		when(mockResponse.getText()).thenReturn(HTML);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse).when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		when(mockSynapseJSNIUtils.sanitizeHtml(HTML)).thenReturn(SANITIZED_HTML);
		AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockFileResult).when(mockPresignedURLAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		when(mockFileResult.getPreSignedURL()).thenReturn(PRESIGNED_URL);
		when(mockFileHandle.getContentSize()).thenReturn(2L);
		when(mockFileHandle.getId()).thenReturn(FILE_HANDLE_ID);
		when(mockFileHandle.getCreatedBy()).thenReturn(CREATED_BY);
	}

	@Test
	public void testFileHandleAssociation() {
		previewWidget.configure(ENTITY_ID, mockFileHandle);

		verify(mockPresignedURLAsyncHandler).getFileResult(fhaCaptor.capture(), any(AsyncCallback.class));
		FileHandleAssociation fha = fhaCaptor.getValue();
		assertEquals(ENTITY_ID, fha.getAssociateObjectId());
		assertEquals(FileHandleAssociateType.FileEntity, fha.getAssociateObjectType());
		assertEquals(FILE_HANDLE_ID, fha.getFileHandleId());
	}

	@Test
	public void testRequestBuilderConfigure() {
		previewWidget.configure(ENTITY_ID, mockFileHandle);

		verify(mockRequestBuilder).configure(GET, PRESIGNED_URL);
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, TEXT_HTML_CHARSET_UTF8);
	}

	@Test
	public void testGetFileResultFailure() {
		Exception ex = new Exception("error");
		AsyncMockStubber.callFailureWith(ex).when(mockPresignedURLAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));

		previewWidget.configure(ENTITY_ID, mockFileHandle);

		verify(mockPresignedURLAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		verify(mockSynapseAlert).handleException(ex);
	}

	@Test
	public void testDownloadFailure() {
		String error = "no connection";
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_GATEWAY_TIMEOUT);
		when(mockResponse.getStatusText()).thenReturn(error);

		previewWidget.configure(ENTITY_ID, mockFileHandle);

		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
		verify(mockSynapseAlert).handleException(exceptionCaptor.capture());
		Throwable th = exceptionCaptor.getValue();
		assertTrue(th.getMessage().contains(error));
	}

	@Test
	public void testRenderHtmlTrusted() {
		previewWidget.configure(ENTITY_ID, mockFileHandle);

		verify(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));
		// user is allowed to render html, so raw html is rendered
		verify(mockView).setSanitizedWarningVisible(false);
		verify(mockView).setHtml(HTML);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}

	@Test
	public void testRenderHtmlUntrustedButSafe() {
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));
		when(mockSynapseJSNIUtils.sanitizeHtml(HTML)).thenReturn(HTML);

		previewWidget.configure(ENTITY_ID, mockFileHandle);

		verify(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));
		// user is not allowed to render, but sanitized version is the same as raw
		verify(mockView).setSanitizedWarningVisible(false);
		verify(mockSynapseJSNIUtils).sanitizeHtml(HTML);
		verify(mockView).setHtml(HTML);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}

	@Test
	public void testBlockRenderHtmlUntrusted() {
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));

		previewWidget.configure(ENTITY_ID, mockFileHandle);

		verify(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));
		// user is not allowed to render. show sanitized version
		verify(mockView).setSanitizedWarningVisible(false);
		verify(mockSynapseJSNIUtils).sanitizeHtml(HTML);
		verify(mockView).setHtml(SANITIZED_HTML);
		verify(mockView).setRawHtml(HTML);
		verify(mockView).setSanitizedWarningVisible(true);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}

	@Test
	public void testIsTrustedCheckFailure() {
		String errorMessage = "unable to determine if user is on the team";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));

		previewWidget.configure(ENTITY_ID, mockFileHandle);

		verify(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));
		// user is not allowed to render. show sanitized version
		verify(mockView).setSanitizedWarningVisible(false);
		verify(mockSynapseJSNIUtils).sanitizeHtml(HTML);
		verify(mockView).setHtml(SANITIZED_HTML);
		verify(mockView).setRawHtml(HTML);
		verify(mockView).setSanitizedWarningVisible(true);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
		verify(mockSynapseJSNIUtils).consoleError(errorMessage);
	}

	@Test
	public void testOnShowFullContent() {
		previewWidget.onShowFullContent();

		verify(mockPopupUtils).showConfirmDialog(eq(""), eq(HtmlPreviewWidget.CONFIRM_OPEN_HTML_MESSAGE), callbackCaptor.capture());

		callbackCaptor.getValue().invoke();
		verify(mockView).openRawHtmlInNewWindow();
	}

	@Test
	public void testMaxFileSizeExceeded() {
		when(mockFileHandle.getContentSize()).thenReturn(new Double(HtmlPreviewWidget.MAX_HTML_FILE_SIZE + 10).longValue());

		previewWidget.configure(ENTITY_ID, mockFileHandle);

		// verify an error was shown, relating to the file size
		verify(mockSynapseAlert).showError(stringCaptor.capture());
		assertTrue(stringCaptor.getValue().contains(FRIENDLY_FILE_SIZE));
	}
}
