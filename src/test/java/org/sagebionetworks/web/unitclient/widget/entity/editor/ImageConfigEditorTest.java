package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.WikiAttachments;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigView;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
public class ImageConfigEditorTest {
		
	ImageConfigEditor editor;
	ImageConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	FileHandleUploadWidget mockFileInputWidget;
	DialogCallback mockCallback;
	WikiAttachments mockAttachments;
	CallbackP mockFinishedCallback;
	FileUpload mockFileUpload;
	FileMetadata mockFileMeta;
	Map<String, String> descriptor;
	String fileHandleId = "222";
	String testFileName = "testing.png";
	String testAttachmentName = "attachment1.png";

	
	@Before
	public void setup(){
		mockFileInputWidget = mock(FileHandleUploadWidget.class);
		mockView = mock(ImageConfigView.class);
		mockCallback = mock(DialogCallback.class);
		mockAttachments = mock(WikiAttachments.class);
		mockFileUpload = mock(FileUpload.class);
		mockFileMeta = mock(FileMetadata.class);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY, WidgetConstants.FLOAT_LEFT);
		editor = new ImageConfigEditor(mockView, mockFileInputWidget, mockAttachments);
		//leave map as null?
		editor.configure(wikiKey, descriptor, mockCallback);
		when(mockAttachments.isValid()).thenReturn(true);
		when(mockAttachments.getSelectedFilename()).thenReturn(testAttachmentName);
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[]{new FileMetadata(testFileName, "image/png")});
		when(mockView.isSynapseEntity()).thenReturn(false);
		when(mockView.isFromAttachments()).thenReturn(false);
		when(mockFileUpload.getFileMeta()).thenReturn(mockFileMeta);
		when(mockFileUpload.getFileHandleId()).thenReturn(fileHandleId);
		when(mockFileMeta.getFileName()).thenReturn(testFileName);
		// will look at the file name if the content type is null
		when(mockFileMeta.getContentType()).thenReturn(null);
		ArgumentCaptor<CallbackP> captor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockFileInputWidget).configure(anyString(), captor.capture());
		mockFinishedCallback = captor.getValue();
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
		verify(mockFileInputWidget).configure(anyString(), any(CallbackP.class));
		verify(mockView).configure(any(WikiPageKey.class), any(DialogCallback.class));
		verify(mockAttachments).configure(any(WikiPageKey.class));
		verify(mockView).setAlignment(anyString());
	}


	@Test
	public void testUploadFileClickedSuccess() {
		verify(mockView).initView();
		mockFinishedCallback.invoke(mockFileUpload);
		verify(mockView).showUploadSuccessUI();
		verify(mockCallback).setPrimaryEnabled(true);
		assertTrue(editor.getNewFileHandleIds().contains(fileHandleId));
	}
	
	@Test
	public void testConfigure() {
		verify(mockView).configure(any(WikiPageKey.class), any(DialogCallback.class));
		when(mockView.isExternal()).thenReturn(false);
		mockFinishedCallback.invoke(mockFileUpload);
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
	        mockFinishedCallback.invoke(mockFileUpload);
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
	        mockFinishedCallback.invoke(mockFileUpload);
	        when(mockAttachments.isValid()).thenReturn(false);
	        editor.updateDescriptorFromView();
	}
}
