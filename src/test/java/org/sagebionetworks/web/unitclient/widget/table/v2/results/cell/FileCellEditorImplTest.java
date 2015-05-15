package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditorView;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;

public class FileCellEditorImplTest {
	FileCellEditorView mockView;
	FileHandleUploadWidget mockFileInputWidget;
	FileCellEditorImpl editor;
	
	@Before
	public void before(){
		mockView = Mockito.mock(FileCellEditorView.class);
		mockFileInputWidget = Mockito.mock(FileHandleUploadWidget.class);
		editor = new FileCellEditorImpl(mockView, mockFileInputWidget);
	}
	
	@Test
	public void testValidateInvalid(){
		when(mockView.getValue()).thenReturn("not a number");
		assertFalse(editor.isValid());
		verify(mockView).setValueError(FileCellEditorImpl.MUST_BE_A_FILE_ID_NUMBER);
	}
	
	@Test
	public void testValidateValid(){
		when(mockView.getValue()).thenReturn("123");
		assertTrue(editor.isValid());
		verify(mockView).clearValueError();
	}
	
	@Test
	public void testValidateNull(){
		when(mockView.getValue()).thenReturn(null);
		assertTrue(editor.isValid());
		verify(mockView).clearValueError();
	}
	
	@Test
	public void testValidateEmpty(){
		when(mockView.getValue()).thenReturn("");
		assertTrue(editor.isValid());
		verify(mockView).clearValueError();
	}
	
	@Test
	public void testOnToggleCollapse(){
		// call under test
		editor.onToggleCollapse();
		verify(mockView).hideErrorMessage();
		verify(mockView).toggleCollapse();
		verify(mockView).resetUploadButton();
		verify(mockFileInputWidget).reset();
	}
	
	@Test
	public void testUploadNothingSelectedNull(){
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(null);
		editor.onUploadFile();
		verify(mockView).showErrorMessage(FileCellEditorImpl.PLEASE_SELECT_A_FILE_TO_UPLOAD);
		verify(mockView, never()).hideErrorMessage();
		verify(mockView, never()).setUploadButtonLoading();
		verify(mockFileInputWidget, never()).uploadSelectedFile(any(FileUploadHandler.class));
	}
	
	@Test
	public void testUploadNothingSelectedEmpty(){
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[0]);
		editor.onUploadFile();
		verify(mockView).showErrorMessage(FileCellEditorImpl.PLEASE_SELECT_A_FILE_TO_UPLOAD);
		verify(mockView, never()).hideErrorMessage();
		verify(mockView, never()).setUploadButtonLoading();
		verify(mockFileInputWidget, never()).uploadSelectedFile(any(FileUploadHandler.class));
	}
	
	@Test
	public void testUploadHappy(){
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[]{new FileMetadata("foo", "text/plain")});
		editor.onUploadFile();
		verify(mockView, never()).showErrorMessage(anyString());
		verify(mockView).hideErrorMessage();
		verify(mockView).setUploadButtonLoading();
		verify(mockFileInputWidget).uploadSelectedFile(editor);
	}
	
	@Test
	public void testUploadSuccess(){
		String fileHandleId = "123";
		FileUpload uploadedFile = new FileUpload(null, fileHandleId);
		editor.uploadSuccess(uploadedFile);
		verify(mockView).setValue(fileHandleId);
		verify(mockView).hideCollapse();
	}
	
	@Test
	public void testUploadFailed(){
		editor.uploadFailed("Error");
		verify(mockView).showErrorMessage("Error");
		verify(mockView).resetUploadButton();
	}

}
