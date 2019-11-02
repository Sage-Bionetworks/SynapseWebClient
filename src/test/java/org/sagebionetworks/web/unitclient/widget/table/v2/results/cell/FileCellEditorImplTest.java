package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditorView;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUpload;

public class FileCellEditorImplTest {
	@Mock
	FileCellEditorView mockView;
	@Mock
	FileHandleUploadWidget mockFileInputWidget;
	FileCellEditor editor;
	@Mock
	FileUpload mockFileUpload;
	FileMetadata[] fileMetadata;
	@Mock
	CallbackP<FileUpload> mockFinishedUploadingCallback;

	String fileHandleId = "222";
	String testFileName = "testing.txt";
	double fileSize = 10;
	@Mock
	PortalGinInjector mockGinInjector;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		editor = new FileCellEditor(mockView, mockGinInjector);
		when(mockGinInjector.getFileHandleUploadWidget()).thenReturn(mockFileInputWidget);
		fileMetadata = new FileMetadata[] {new FileMetadata(testFileName, ContentTypeDelimiter.TEXT.getContentType(), fileSize)};
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(fileMetadata);
		when(mockFileUpload.getFileMeta()).thenReturn(fileMetadata[0]);
		when(mockFileUpload.getFileHandleId()).thenReturn(fileHandleId);
	}

	@Test
	public void testOnToggleCollapse() {
		// call under test
		editor.onToggleCollapse();
		verify(mockView).hideErrorMessage();
		verify(mockView).toggleCollapse();
		verify(mockFileInputWidget).reset();
	}

	@Test
	public void testUploadSuccess() {
		// show upload UI
		editor.onToggleCollapse();
		ArgumentCaptor<CallbackP> argumentCaptor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockFileInputWidget).configure(anyString(), argumentCaptor.capture());
		// invoke callback after file successfully uploaded
		argumentCaptor.getValue().invoke(mockFileUpload);
		verify(mockView, never()).showErrorMessage(anyString());
		verify(mockView, times(2)).hideErrorMessage();
		verify(mockView).setValue(fileHandleId);
		verify(mockView).hideCollapse();
	}

}
