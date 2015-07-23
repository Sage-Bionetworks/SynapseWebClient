package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditorView;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUpload;

public class FileCellEditorImplTest {
	FileCellEditorView mockView;
	FileHandleUploadWidget mockFileInputWidget;
	FileCellEditorImpl editor;
	FileUpload mockFileUpload;
	FileMetadata[] mockMetadata;
	CallbackP<FileUpload> mockFinishedUploadingCallback;
	
	String fileHandleId = "222";
	String testFileName = "testing.txt";
	double fileSize = 10;
	
	@Before
	public void before(){
		mockFinishedUploadingCallback = mock(CallbackP.class);
		mockView = mock(FileCellEditorView.class);
		mockFileInputWidget = mock(FileHandleUploadWidget.class);
		mockFileUpload = mock(FileUpload.class);
		editor = new FileCellEditorImpl(mockView, mockFileInputWidget);
		mockMetadata = new FileMetadata[]{new FileMetadata(testFileName, ContentTypeDelimiter.TEXT.getContentType(), fileSize)};
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(mockMetadata);
		when(mockFileUpload.getFileMeta()).thenReturn(mockMetadata[0]);
		when(mockFileUpload.getFileHandleId()).thenReturn(fileHandleId);
	}
	
	@Test
	public void testOnToggleCollapse(){
		// call under test
		editor.onToggleCollapse();
		verify(mockView).hideErrorMessage();
		verify(mockView).toggleCollapse();
		verify(mockFileInputWidget).reset();
	}
	
	@Test
	public void testUploadSuccess(){
		ArgumentCaptor<CallbackP> argumentCaptor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockFileInputWidget).configure(anyString(), argumentCaptor.capture());
		argumentCaptor.getValue().invoke(mockFileUpload);
		verify(mockView, never()).showErrorMessage(anyString());
		verify(mockView).hideErrorMessage();
		verify(mockView).setValue(fileHandleId);
		verify(mockView).hideCollapse();
	}

}
