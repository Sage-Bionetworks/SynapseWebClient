package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

public class ImageConfigEditorTest {
		
	ImageConfigEditor editor;
	ImageConfigView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	@Before
	public void setup(){
		mockView = mock(ImageConfigView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		editor = new ImageConfigEditor(mockView);
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		Map<String,String> descriptor = new HashMap<String, String>();
		AttachmentData testImage = new AttachmentData();
		testImage.setName("test name");
		testImage.setTokenId("test token");
		testImage.setMd5("test md5");
		
		descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, "test name");
		editor.configure("","", descriptor);
		verify(mockView).setEntityId(anyString());
		//verify(mockView).setExternalVisible(anyBoolean());
		when(mockView.getUploadedAttachmentData()).thenReturn(testImage);
		
		when(mockView.isExternal()).thenReturn(false);
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getUploadedAttachmentData();
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
