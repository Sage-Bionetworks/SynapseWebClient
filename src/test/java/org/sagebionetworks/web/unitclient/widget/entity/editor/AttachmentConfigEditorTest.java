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
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigView;
import org.sagebionetworks.web.shared.WikiPageKey;
public class AttachmentConfigEditorTest {
		
	AttachmentConfigEditor editor;
	AttachmentConfigView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null, null);
	
	@Before
	public void setup(){
		mockView = mock(AttachmentConfigView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		editor = new AttachmentConfigEditor(mockView);
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
		
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getUploadedFileHandleName();
	}
	
	@Test
	public void testTextToInsert() {
		String textToInsert = editor.getTextToInsert();
		assertTrue(textToInsert == null);
	}

}
