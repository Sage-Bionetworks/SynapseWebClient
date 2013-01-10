package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.widget.ImageAttachmentWidgetDescriptor;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigView;

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
		editor = new ImageConfigEditor(mockView, mockSynapseClient, mockNodeModelCreator);
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		ImageAttachmentWidgetDescriptor descriptor = new ImageAttachmentWidgetDescriptor();
		AttachmentData testImage = new AttachmentData();
		testImage.setName("test name");
		testImage.setTokenId("test token");
		testImage.setMd5("test md5");
		descriptor.setFileName("test name");
		editor.configure("", descriptor);
		verify(mockView).setEntityId(anyString());
		verify(mockView).setExternalVisible(anyBoolean());
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
