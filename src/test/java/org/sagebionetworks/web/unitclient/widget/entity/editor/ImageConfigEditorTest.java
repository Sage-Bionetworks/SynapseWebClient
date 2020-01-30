package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
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
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.client.widget.upload.ImageUploadWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;

public class ImageConfigEditorTest {

	ImageConfigEditor editor;
	ImageConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	ImageUploadWidget mockFileInputWidget;
	DialogCallback mockCallback;
	WikiAttachments mockAttachments;
	CallbackP mockFinishedCallback;
	FileUpload mockFileUpload;
	FileMetadata mockFileMeta;
	Map<String, String> descriptor;
	String fileHandleId = "222";
	String testFileName = "testing.png";
	String testAttachmentName = "attachment1.png";
	double fileSize = 10;


	@Before
	public void setup() {
		mockFileInputWidget = mock(ImageUploadWidget.class);
		mockView = mock(ImageConfigView.class);
		mockCallback = mock(DialogCallback.class);
		mockAttachments = mock(WikiAttachments.class);
		mockFileUpload = mock(FileUpload.class);
		mockFileMeta = mock(FileMetadata.class);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY, WidgetConstants.FLOAT_LEFT);
		editor = new ImageConfigEditor(mockView, mockFileInputWidget, mockAttachments);
		// leave map as null?
		editor.configure(wikiKey, descriptor, mockCallback);
		when(mockAttachments.isValid()).thenReturn(true);
		when(mockAttachments.getSelectedFilename()).thenReturn(testAttachmentName);
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata(testFileName, "image/png", fileSize));
		when(mockView.isSynapseEntity()).thenReturn(false);
		when(mockFileUpload.getFileMeta()).thenReturn(mockFileMeta);
		when(mockFileUpload.getFileHandleId()).thenReturn(fileHandleId);
		when(mockFileMeta.getFileName()).thenReturn(testFileName);
		// will look at the file name if the content type is null
		when(mockFileMeta.getContentType()).thenReturn(null);
		ArgumentCaptor<CallbackP> captor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockFileInputWidget).configure(captor.capture());
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
		verify(mockFileInputWidget).configure(any(CallbackP.class));
		verify(mockAttachments).configure(any(WikiPageKey.class));
	}

	@Test
	public void testConstructionWithAlignment() {
		String alignment = "alignment";
		reset(mockView);
		descriptor.put(WidgetConstants.ALIGNMENT_KEY, alignment);
		editor.configure(wikiKey, descriptor, mockCallback);
		verify(mockView).setAlignment(alignment);
	}

	@Test
	public void testScale() {
		String scale = "70";
		reset(mockView);
		descriptor.put(WidgetConstants.IMAGE_WIDGET_SCALE_KEY, scale);
		editor.configure(wikiKey, descriptor, mockCallback);
		verify(mockView).setScale(70);
	}

	@Test
	public void testEditAttachmentBased() {
		String fileName = "test.png";
		reset(mockView);
		descriptor.clear();
		descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, fileName);
		editor.configure(wikiKey, descriptor, mockCallback);
		verify(mockView, never()).setWikiFilesTabVisible(anyBoolean());
		verify(mockView).showWikiFilesTab();
		verify(mockView).setWikiAttachmentsWidgetVisible(true);
		verify(mockAttachments).setSelectedFilename(fileName);
	}

	@Test
	public void testSynapseIdBased() {
		String synId = "syn9876";
		reset(mockView);
		descriptor.put(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY, synId);
		editor.configure(wikiKey, descriptor, mockCallback);
		verify(mockView).setWikiFilesTabVisible(false);
		verify(mockView).showSynapseTab();
		verify(mockView).setSynapseId(synId);
	}

	@Test
	public void testSynapseIdWithVersionBased() {
		String synId = "syn9876";
		String version = "4";
		reset(mockView);
		descriptor.put(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY, synId);
		descriptor.put(WidgetConstants.WIDGET_ENTITY_VERSION_KEY, version);
		editor.configure(wikiKey, descriptor, mockCallback);
		verify(mockView).setWikiFilesTabVisible(false);
		verify(mockView).showSynapseTab();
		verify(mockView).setSynapseId(synId + WebConstants.ENTITY_VERSION_STRING + version);
	}

	@Test
	public void testUploadFileClickedSuccess() {
		verify(mockView).initView();
		mockFinishedCallback.invoke(mockFileUpload);
		verify(mockView).showUploadSuccessUI(testFileName);
		verify(mockView).setWikiAttachmentsWidgetVisible(false);
		verify(mockCallback).setPrimaryEnabled(true);
		assertTrue(editor.getNewFileHandleIds().contains(fileHandleId));
	}

	@Test
	public void testConfigureWithImageLinkOnly() {
		reset(mockView);
		reset(mockFileInputWidget);
		descriptor.clear();
		editor.configureWithoutUpload(wikiKey, descriptor, mockCallback);
		verify(mockView).initView();
		verify(mockView).setWikiFilesTabVisible(false);
		verify(mockView).showExternalTab();
		verify(mockFileInputWidget).reset();
	}

	@Test
	public void testConfigureWithImageLinkToSynapseEntity() {
		reset(mockView);
		reset(mockFileInputWidget);
		editor.configureWithoutUpload(wikiKey, descriptor, mockCallback);
		verify(mockView).initView();
		verify(mockView, atLeastOnce()).setWikiFilesTabVisible(false);
		verify(mockView, never()).showExternalTab();
		verify(mockView).setSynapseId(descriptor.get(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY));
		verify(mockFileInputWidget).reset();
	}

	@Test
	public void testConfigure() {
		when(mockView.isExternal()).thenReturn(false);
		mockFinishedCallback.invoke(mockFileUpload);
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConfigureFileNotUploaded() {
		when(mockAttachments.isValid()).thenReturn(false);
		editor.configure(wikiKey, new HashMap<String, String>(), mockCallback);
		editor.updateDescriptorFromView();
	}

	@Test
	public void testTextToInsert() {
		// the case when there is an external image
		when(mockView.isExternal()).thenReturn(true);
		String textToInsert = editor.getTextToInsert();
		verify(mockView).getImageUrl();
		assertTrue(textToInsert != null && textToInsert.length() > 0);
	}

	@Test
	public void testTextToInsertNotExternal() {
		// the case when there is an external image
		when(mockView.isExternal()).thenReturn(false);
		assertNull(editor.getTextToInsert());
	}

	@Test
	public void testIsFromSynapseId() {
		when(mockView.isExternal()).thenReturn(false);
		when(mockView.isSynapseEntity()).thenReturn(true);
		String synId = "syn1293";
		Long version = 92L;
		when(mockView.getSynapseId()).thenReturn(synId);
		when(mockView.getVersion()).thenReturn(version);
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, mockCallback);
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		assertEquals(synId, descriptor.get(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY));
		assertEquals(version.toString(), descriptor.get(WidgetConstants.WIDGET_ENTITY_VERSION_KEY));
	}

	@Test
	public void testIsFromAttachments() {
		when(mockView.isSynapseEntity()).thenReturn(false);
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, mockCallback);
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockAttachments).isValid();
		assertEquals(testAttachmentName, descriptor.get(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY));
	}


	@Test(expected = IllegalArgumentException.class)
	public void testIsFromAttachmentsFailure() {
		when(mockView.isSynapseEntity()).thenReturn(false);
		Map<String, String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, mockCallback);
		when(mockAttachments.isValid()).thenReturn(false);
		editor.updateDescriptorFromView();
	}
}
