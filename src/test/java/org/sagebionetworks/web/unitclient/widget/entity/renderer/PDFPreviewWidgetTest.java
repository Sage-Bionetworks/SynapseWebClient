package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.renderer.IFrameView;
import org.sagebionetworks.web.client.widget.entity.renderer.PDFPreviewWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class PDFPreviewWidgetTest {
	PDFPreviewWidget widget;
	@Mock
	IFrameView mockView;
	@Mock
	PresignedURLAsyncHandler mockPresignedURLAsyncHandler;
	@Mock
	GWTWrapper mockGWT;
	@Captor
	ArgumentCaptor<FileHandleAssociation> fhaCaptor;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	@Mock
	FileResult mockFileResult;
	@Mock
	FileHandle mockFileHandle;
	
	public static final String FRIENDLY_FILE_SIZE = "a friendly file size";
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		when(mockGWT.getFriendlySize(anyDouble(), anyBoolean())).thenReturn(FRIENDLY_FILE_SIZE);
		widget = new PDFPreviewWidget(
				mockView,
				mockPresignedURLAsyncHandler,
				mockGWT);
		AsyncMockStubber.callSuccessWith(mockFileResult).when(mockPresignedURLAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testConstructor() {
		verify(mockView).addAttachHandler(any());
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		int parentOffsetHeight = 200;
		String synId = "syn1996";
		String fileHandleId = "1812";
		String presignedUrl = "http://path/to/file.pdf";
		String encodedPresignedUrl = "http%3A%2F%2Fpath%2Fto%2Ffile.pdf";
		when(mockFileResult.getPreSignedURL()).thenReturn(presignedUrl);
		when(mockView.getParentOffsetHeight()).thenReturn(parentOffsetHeight);
		when(mockGWT.encodeQueryString(presignedUrl)).thenReturn(encodedPresignedUrl);
		when(mockFileHandle.getId()).thenReturn(fileHandleId);
		when(mockFileHandle.getContentSize()).thenReturn(1L);
		widget.configure(synId, mockFileHandle);
		
		verify(mockPresignedURLAsyncHandler).getFileResult(fhaCaptor.capture(), any(AsyncCallback.class));
		FileHandleAssociation fha = fhaCaptor.getValue();
		assertEquals(synId, fha.getAssociateObjectId());
		assertEquals(FileHandleAssociateType.FileEntity, fha.getAssociateObjectType());
		assertEquals(fileHandleId, fha.getFileHandleId());
		
		verify(mockView).configure(PDFPreviewWidget.PDF_JS_VIEWER_PREFIX + encodedPresignedUrl, parentOffsetHeight);
	}
	
	@Test
	public void testConfigureFailure() {
		String error = "an error";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockPresignedURLAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		when(mockFileHandle.getId()).thenReturn("23");
		when(mockFileHandle.getContentSize()).thenReturn(1L);

		widget.configure("syn23",mockFileHandle);
		
		verify(mockView).showError(error);
	}
	
	@Test
	public void testMaxFileSizeExceeded() {
		when(mockFileHandle.getId()).thenReturn("23");
		when(mockFileHandle.getContentSize()).thenReturn(new Double(PDFPreviewWidget.MAX_PDF_FILE_SIZE + 10).longValue());

		widget.configure("syn23",mockFileHandle);
		
		//verify an error was shown, relating to the file size
		verify(mockView).showError(stringCaptor.capture());
		assertTrue(stringCaptor.getValue().contains(FRIENDLY_FILE_SIZE));
	}
}
