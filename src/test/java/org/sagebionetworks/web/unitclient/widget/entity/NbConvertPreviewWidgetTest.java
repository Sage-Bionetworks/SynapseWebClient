package org.sagebionetworks.web.unitclient.widget.entity;

import static com.google.gwt.http.client.RequestBuilder.GET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.SynapseJavascriptClient.ACCEPT;
import static org.sagebionetworks.web.client.widget.entity.renderer.NbConvertPreviewWidget.HTML_PREFIX;
import static org.sagebionetworks.web.client.widget.entity.renderer.NbConvertPreviewWidget.HTML_SUFFIX;
import static org.sagebionetworks.web.shared.WebConstants.NBCONVERT_ENDPOINT_PROPERTY;
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
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.HtmlPreviewView;
import org.sagebionetworks.web.client.widget.entity.renderer.NbConvertPreviewWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the nbconvert preview widget.
 * 
 * @author jayhodgson
 *
 */
public class NbConvertPreviewWidgetTest {
	NbConvertPreviewWidget previewWidget;
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
	SynapseProperties mockSynapseProperties;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	FileHandle mockFileHandle;
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
	@Captor
	ArgumentCaptor<FileHandleAssociation> fhaCaptor;
	@Captor
	ArgumentCaptor<Throwable> exceptionCaptor;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;

	public static final String HTML = "<!--converted ipynb into html --><img src=a onerror=\"javascript:alert('running my js')\" /><p>hello</p>";
	public static final String WRAPPED_HTML = HTML_PREFIX + HTML + HTML_SUFFIX;
	public static final String SANITIZED_HTML = "<p>hello</p>";

	public static final String ENTITY_ID = "syn20923";
	public static final String FILE_HANDLE_ID = "9992782";
	public static final String CREATED_BY = "8992983";
	public static final String PRESIGNED_URL = "https://s3.path/test.ipynb";
	public static final String ENCODED_PRESIGNED_URL = "https%3A%2F%2Fs3.path%2Ftest.ipynb";

	public static final String NBCONVERT_ENDPOINT = "https://api.synapse.org/nbconvert?file=";

	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockSynapseProperties.getSynapseProperty(NBCONVERT_ENDPOINT_PROPERTY)).thenReturn(NBCONVERT_ENDPOINT);
		previewWidget = new NbConvertPreviewWidget(mockView, mockPresignedURLAsyncHandler, mockSynapseJSNIUtils, mockRequestBuilder, mockSynapseAlert, mockSynapseClient, mockPopupUtils, mockSynapseProperties, mockGwt);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		when(mockResponse.getText()).thenReturn(HTML);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse).when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		when(mockSynapseJSNIUtils.sanitizeHtml(anyString())).thenReturn(SANITIZED_HTML);
		AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockFileResult).when(mockPresignedURLAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		when(mockFileResult.getPreSignedURL()).thenReturn(PRESIGNED_URL);
		when(mockGwt.encodeQueryString(PRESIGNED_URL)).thenReturn(ENCODED_PRESIGNED_URL);
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

		verify(mockRequestBuilder).configure(GET, NBCONVERT_ENDPOINT + ENCODED_PRESIGNED_URL);
		verify(mockRequestBuilder).setHeader(ACCEPT, TEXT_HTML_CHARSET_UTF8);
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
		verify(mockView).setHtml(WRAPPED_HTML);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}

	@Test
	public void testRenderHtmlUntrustedButSafe() {
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));

		when(mockSynapseJSNIUtils.sanitizeHtml(WRAPPED_HTML)).thenReturn(WRAPPED_HTML);

		previewWidget.configure(ENTITY_ID, mockFileHandle);

		verify(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));
		// user is not allowed to render, but sanitized version is the same as raw
		verify(mockView).setSanitizedWarningVisible(false);
		verify(mockSynapseJSNIUtils).sanitizeHtml(WRAPPED_HTML);
		verify(mockView).setHtml(WRAPPED_HTML);
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
		verify(mockSynapseJSNIUtils).sanitizeHtml(WRAPPED_HTML);
		verify(mockView).setHtml(SANITIZED_HTML);
		verify(mockView).setRawHtml(WRAPPED_HTML);
		// once because user is not on the html/js team, once because this is an ipynb (need link to
		// download for fully interactive version)
		verify(mockView, times(2)).setSanitizedWarningVisible(true);
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
		verify(mockSynapseJSNIUtils).sanitizeHtml(WRAPPED_HTML);
		verify(mockView).setHtml(SANITIZED_HTML);
		verify(mockView).setRawHtml(WRAPPED_HTML);
		verify(mockView, times(2)).setSanitizedWarningVisible(true);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
		verify(mockSynapseJSNIUtils).consoleError(errorMessage);
	}

	@Test
	public void testOnShowFullContent() {
		previewWidget.onShowFullContent();

		// verify it asks for a fresh presigned url, and opens it (to download)
		verify(mockPresignedURLAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		verify(mockView).openInNewWindow(PRESIGNED_URL);
	}
}
