package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
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
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.renderer.IFrameView;
import org.sagebionetworks.web.client.widget.entity.renderer.PDFPreviewWidget;


public class PDFPreviewWidgetTest {
	PDFPreviewWidget widget;
	@Mock
	IFrameView mockView;
	@Mock
	SynapseJSNIUtils mockJSNIUtils;
	@Mock
	GWTWrapper mockGWT;
	@Captor
	ArgumentCaptor<FileHandleAssociation> fhaCaptor;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	@Mock
	FileHandle mockFileHandle;
	private static final String URL = "fileHandleServlet?filehandleid=x";
	public static final String FRIENDLY_FILE_SIZE = "a friendly file size";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(mockGWT.getFriendlySize(anyDouble(), anyBoolean())).thenReturn(FRIENDLY_FILE_SIZE);
		widget = new PDFPreviewWidget(mockView, mockJSNIUtils, mockGWT);
		when(mockJSNIUtils.getFileHandleAssociationUrl(anyString(), any(FileHandleAssociateType.class), anyString())).thenReturn(URL);
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
		String encodedPresignedUrl = "http%3A%2F%2Fpath%2Fto%2Ffile.pdf";

		when(mockView.getParentOffsetHeight()).thenReturn(parentOffsetHeight);
		when(mockGWT.encodeQueryString(URL)).thenReturn(encodedPresignedUrl);
		when(mockFileHandle.getId()).thenReturn(fileHandleId);
		when(mockFileHandle.getContentSize()).thenReturn(1L);
		widget.configure(synId, mockFileHandle);

		verify(mockView).configure(PDFPreviewWidget.PDF_JS_VIEWER_PREFIX + encodedPresignedUrl, parentOffsetHeight);
	}

	@Test
	public void testMaxFileSizeExceeded() {
		when(mockFileHandle.getId()).thenReturn("23");
		when(mockFileHandle.getContentSize()).thenReturn(new Double(PDFPreviewWidget.MAX_PDF_FILE_SIZE + 10).longValue());

		widget.configure("syn23", mockFileHandle);

		// verify an error was shown, relating to the file size
		verify(mockView).showError(stringCaptor.capture());
		assertTrue(stringCaptor.getValue().contains(FRIENDLY_FILE_SIZE));
	}
}
