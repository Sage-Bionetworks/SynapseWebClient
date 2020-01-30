package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
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
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigView;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;

public class AttachmentConfigEditorTest {

	AttachmentConfigEditor editor;
	AttachmentConfigView mockView;
	WikiAttachments mockAttachments;
	CallbackP mockFinishedCallback;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null, null);
	FileHandleUploadWidget mockFileInputWidget;
	String testFileName = "testing.txt";
	String testAttachmentName = "attachment1.png";
	String fileHandleId = "222";
	double fileSize = 10;
	DialogCallback mockCallback;
	CallbackP<FileUpload> mockFinishedUploadingCallback;
	FileMetadata[] mockMetadata;
	FileUpload mockFileUpload;


	@Before
	public void setup() {
		mockFileUpload = mock(FileUpload.class);
		mockFileInputWidget = mock(FileHandleUploadWidget.class);
		mockView = mock(AttachmentConfigView.class);
		mockCallback = mock(DialogCallback.class);
		mockAttachments = mock(WikiAttachments.class);
		editor = new AttachmentConfigEditor(mockView, mockFileInputWidget, mockAttachments);
		mockMetadata = new FileMetadata[] {new FileMetadata(testFileName, ContentTypeDelimiter.TEXT.getContentType(), fileSize)};
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(mockMetadata);
		when(mockAttachments.isValid()).thenReturn(true);
		when(mockAttachments.getSelectedFilename()).thenReturn(testAttachmentName);
		when(mockFileUpload.getFileMeta()).thenReturn(mockMetadata[0]);
		when(mockFileUpload.getFileHandleId()).thenReturn(fileHandleId);

	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(editor);
		verify(mockView).setFileInputWidget(any(Widget.class));
		verify(mockView).setWikiAttachmentsWidget(any(Widget.class));
	}

	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testUploadFileClickedSuccess() {
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, mockCallback);

		ArgumentCaptor<CallbackP> captor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockFileInputWidget).configure(anyString(), captor.capture());
		mockFinishedCallback = captor.getValue();
		mockFinishedCallback.invoke(mockFileUpload);

		verify(mockView).initView();
		verify(mockView).showUploadSuccessUI(anyString());
		verify(mockCallback).setPrimaryEnabled(true);
		verify(mockAttachments).configure(wikiKey);
		assertTrue(editor.getNewFileHandleIds().contains(fileHandleId));
	}

	@Test
	public void testConfigure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, mockCallback);

		ArgumentCaptor<CallbackP> captor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockFileInputWidget).configure(anyString(), captor.capture());
		mockFinishedCallback = captor.getValue();
		mockFinishedCallback.invoke(mockFileUpload);

		verify(mockFileInputWidget).reset();
		verify(mockView).clear();
		verify(mockView).configure(any(WikiPageKey.class), any(DialogCallback.class));
		editor.addFileHandleId("123");
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		assertEquals(testFileName, descriptor.get(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY));
	}

	@Test
	public void testIsFromAttachments() {
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, mockCallback);
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockAttachments).isValid();
		assertEquals(testAttachmentName, descriptor.get(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY));
	}


	@Test(expected = IllegalArgumentException.class)
	public void testIsFromAttachmentsFailure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, mockCallback);
		when(mockAttachments.isValid()).thenReturn(false);
		editor.updateDescriptorFromView();
	}

	@Test
	public void testTextToInsert() {
		String textToInsert = editor.getTextToInsert();
		assertTrue(textToInsert == null);
	}

}
