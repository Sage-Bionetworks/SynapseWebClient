package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditorView;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;

public class FileCellEditorImplTest {
	FileCellEditorView mockView;
	FileInputWidget mockFileInputWidget;
	FileCellEditorImpl editor;
	
	@Before
	public void before(){
		mockView = Mockito.mock(FileCellEditorView.class);
		mockFileInputWidget = Mockito.mock(FileInputWidget.class);
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
		editor.uploadSuccess("123");
		verify(mockView).setValue("123");
		verify(mockView).hideCollapse();
	}
	
	@Test
	public void testUploadFailed(){
		editor.uploadFailed("Error");
		verify(mockView).showErrorMessage("Error");
		verify(mockView).resetUploadButton();
	}

}
