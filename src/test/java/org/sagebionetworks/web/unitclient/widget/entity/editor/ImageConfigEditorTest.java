package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigView;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.shared.WikiPageKey;
public class ImageConfigEditorTest {
		
	ImageConfigEditor editor;
	ImageConfigView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	FileInputWidget mockFileInputWidget;
	
	@Before
	public void setup(){
		mockFileInputWidget = mock(FileInputWidget.class);
		mockView = mock(ImageConfigView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		editor = new ImageConfigEditor(mockView, mockFileInputWidget);
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[]{new FileMetadata("testing.png", "image/png")});
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		Map<String,String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).configure(any(WikiPageKey.class), any(DialogCallback.class));
		
		when(mockView.isExternal()).thenReturn(false);
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
	}
	
	@Test
	public void testTextToInsert() {
		//the case when there is an external image
		when(mockView.isExternal()).thenReturn(true);
		String textToInsert = editor.getTextToInsert();
		verify(mockView).getImageUrl();
		assertTrue(textToInsert != null && textToInsert.length() > 0);
	}

}
