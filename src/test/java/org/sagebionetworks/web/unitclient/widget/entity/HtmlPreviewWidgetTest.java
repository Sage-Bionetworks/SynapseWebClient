package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget.PreviewFileType;
import org.sagebionetworks.web.client.widget.entity.PreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.HtmlPreviewView;
import org.sagebionetworks.web.client.widget.entity.renderer.HtmlPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.PDFPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.VideoWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * Unit test for the preview widget.
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
	
	@Captor
	ArgumentCaptor<String> stringCaptor;
	
	public static final String TEST_ENTITY_ID = "syn20923";
	public static final String TEST_FILE_HANDLE_ID = "8883";
	public static final String TEST_ENTITY_MAIN_FILE_CREATED_BY = "8992983";
	@Before
	public void before() throws Exception{
		MockitoAnnotations.initMocks(this);
		previewWidget = new HtmlPreviewWidget(mockView, mockPresignedURLAsyncHandler, mockSynapseJSNIUtils, mockRequestBuilder, mockSynapseAlert, mockSynapseClient);
		String mainFileId = "MAIN_FILE";
		mockResponse = mock(Response.class);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		when(mockResponse.getText()).thenReturn("html response");
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse).when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
	}
//	
//	@Test
//	public void testGetFullFileContents() {
//		mainFileHandle.setContentType("text/html");
//		mainFileHandle.setFileName("test.html");
//		previewWidget.configure(testBundle);
//		
//		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), stringCaptor.capture());
//		assertTrue(stringCaptor.getValue().contains("preview=false"));
//		verify(mockView).showLoading();
//		verify(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));
//	}
//	
//	
//	@Test
//	public void testRenderHTMLBlocked() {
//		boolean isUserAllowedToRenderHTML = false;
//		String userId = "56765";
//		String html = "<html><script></script></html>";
//		String sanitizedHtml = "<html></html>";
//		when(mockSynapseJSNIUtils.sanitizeHtml(anyString())).thenReturn(sanitizedHtml);
//		AsyncMockStubber.callSuccessWith(isUserAllowedToRenderHTML).when(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));
//		
//		previewWidget.renderHTML(userId, html);
//		
//		verify(mockSynapseClient).isUserAllowedToRenderHTML(eq(userId), any(AsyncCallback.class));
//		//attempts to sanitize the html and compare
//		verify(mockSynapseJSNIUtils).sanitizeHtml(html);
//		//html not set, showing text instead
//		verify(mockView).setTextPreview(anyString());
//	}
//	
//
//	@Test
//	public void testRenderSimpleHTMLAllowed() {
//		boolean isUserAllowedToRenderHTML = false;
//		String userId = "56765";
//		String html = "<html></html>";
//		String sanitizedHtml = "<html></html>";
//		when(mockSynapseJSNIUtils.sanitizeHtml(anyString())).thenReturn(sanitizedHtml);
//		AsyncMockStubber.callSuccessWith(isUserAllowedToRenderHTML).when(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));
//		
//		previewWidget.renderHTML(userId, html);
//		
//		verify(mockSynapseClient).isUserAllowedToRenderHTML(eq(userId), any(AsyncCallback.class));
//		verify(mockSynapseJSNIUtils).sanitizeHtml(html);
//		verify(mockView).setHTML(html);
//	}
//	
//
//	@Test
//	public void testRenderDangerousHTML() {
//		boolean isUserAllowedToRenderHTML = true;
//		String userId = "56765";
//		mainFileHandle.setCreatedBy(userId);
//		String html = "<html><script></script></html>";
//		AsyncMockStubber.callSuccessWith(isUserAllowedToRenderHTML).when(mockSynapseClient).isUserAllowedToRenderHTML(anyString(), any(AsyncCallback.class));
//		when(mockResponse.getText()).thenReturn(html);
//		mainFileHandle.setContentType("text/html");
//		mainFileHandle.setFileName("index.html");
//		previewWidget.configure(testBundle);
//		verify(mockSynapseClient).isUserAllowedToRenderHTML(eq(userId), any(AsyncCallback.class));
//		verify(mockSynapseJSNIUtils, never()).sanitizeHtml(anyString());
//		verify(mockView).setHTML(html);
//	}

}
