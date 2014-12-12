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
import org.sagebionetworks.web.shared.WikiPageKey;
public class ImageConfigEditorTest {
		
	ImageConfigEditor editor;
	ImageConfigView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	
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
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).configure(any(WikiPageKey.class), any(DialogCallback.class));
		when(mockView.getUploadedFileHandleName()).thenReturn("a test file name");
		
		when(mockView.isExternal()).thenReturn(false);
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getUploadedFileHandleName();
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
