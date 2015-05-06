package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.widget.entity.WikiAttachments;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigView;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
public class ImageConfigEditorTest {
		
	ImageConfigEditor editor;
	ImageConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	FileInputWidget mockFileInputWidget;
	String testFileName = "testing.png";
	String testAttachmentName = "attachment1.png";
	DialogCallback mockCallback;
	WikiAttachments mockAttachments;
	
	@Before
	public void setup(){
		mockFileInputWidget = mock(FileInputWidget.class);
		mockView = mock(ImageConfigView.class);
		mockCallback = mock(DialogCallback.class);
		mockAttachments = mock(WikiAttachments.class);
		editor = new ImageConfigEditor(mockView, mockFileInputWidget, mockAttachments);
		when(mockAttachments.isValid()).thenReturn(true);
		when(mockAttachments.getSelectedFilename()).thenReturn(testAttachmentName);
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[]{new FileMetadata(testFileName, "image/png")});
		when(mockView.isSynapseEntity()).thenReturn(false);
		when(mockView.isFromAttachments()).thenReturn(false);
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(editor);
		verify(mockView).setFileInputWidget(any(Widget.class));
		verify(mockView).setWikiAttachmentsWidget(any(Widget.class));
	}


	@Test
	public void testValidateSelectedFile() {
		assertTrue(editor.validateSelectedFile());
		
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[]{});
		assertFalse(editor.validateSelectedFile());
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(null);
		assertFalse(editor.validateSelectedFile());
	}
	
	

	@Test
	public void testUploadFileClickedSuccess() {
		Map<String,String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, mockCallback);
		verify(mockView).initView();
		editor.uploadFileClicked();
		verify(mockView).setUploadButtonEnabled(false);
		
		ArgumentCaptor<FileUploadHandler> captor = ArgumentCaptor.forClass(FileUploadHandler.class);
		verify(mockFileInputWidget).uploadSelectedFile(captor.capture());
		String fileHandleId = "222";
		captor.getValue().uploadSuccess(fileHandleId);
		verify(mockView).showUploadSuccessUI();
		verify(mockCallback).setPrimaryEnabled(true);
		assertTrue(editor.getNewFileHandleIds().contains(fileHandleId));
	}
	

	@Test
	public void testUploadFileClickedFailure() {
		Map<String,String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, mockCallback);
		
		editor.uploadFileClicked();
		verify(mockView).setUploadButtonEnabled(false);
		reset(mockView);
		
		ArgumentCaptor<FileUploadHandler> captor = ArgumentCaptor.forClass(FileUploadHandler.class);
		verify(mockFileInputWidget).uploadSelectedFile(captor.capture());
		String error = "this is my error";
		captor.getValue().uploadFailed(error);
		verify(mockView).showUploadFailureUI(error);
		verify(mockView).setUploadButtonEnabled(true);
	}
	
	@Test
	public void testConfigure() {
		Map<String,String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, mockCallback);
		verify(mockView).configure(any(WikiPageKey.class), any(DialogCallback.class));
		
		when(mockView.isExternal()).thenReturn(false);
		editor.addFileHandleId("123");
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testConfigureFileNotUploaded() {
		editor.configure(wikiKey, new HashMap<String, String>(), mockCallback);
		editor.updateDescriptorFromView();
	}
	
	@Test
	public void testTextToInsert() {
		//the case when there is an external image
		when(mockView.isExternal()).thenReturn(true);
		String textToInsert = editor.getTextToInsert();
		verify(mockView).getImageUrl();
		assertTrue(textToInsert != null && textToInsert.length() > 0);
	}
	
	@Test
	public void testTextToInsertNotExternal() {
		//the case when there is an external image
		when(mockView.isExternal()).thenReturn(false);
		assertNull(editor.getTextToInsert());
	}

	@Test
	public void testIsFromAttachments() {
	        when(mockView.isSynapseEntity()).thenReturn(false);
	        when(mockView.isFromAttachments()).thenReturn(true);
	        Map<String,String> descriptor = new HashMap<String, String>();
	        editor.configure(wikiKey, descriptor, mockCallback);
	        
	        editor.addFileHandleId("123");
	        editor.updateDescriptorFromView();
	        verify(mockView).checkParams();
	        verify(mockAttachments).isValid();
	        assertEquals(testAttachmentName, descriptor.get(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY));
	}


	@Test (expected=IllegalArgumentException.class)
	public void testIsFromAttachmentsFailure() {
	        when(mockView.isSynapseEntity()).thenReturn(false);
	        when(mockView.isFromAttachments()).thenReturn(true);
	        Map<String,String> descriptor = new HashMap<String, String>();
	        editor.configure(wikiKey, descriptor, mockCallback);
	        editor.addFileHandleId("123");
	        when(mockAttachments.isValid()).thenReturn(false);
	        editor.updateDescriptorFromView();
	}
}
