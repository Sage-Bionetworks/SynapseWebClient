package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditorView;
import org.sagebionetworks.web.shared.WikiPageKey;

public class WikiSubpagesOrderEditorTest {
	
	WikiSubpagesOrderEditor editor;

	@Mock
	WikiSubpagesOrderEditorView mockView;
	@Mock
	WikiSubpageOrderEditorTree mockEditorTree;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseClientAsync mockSynapseClient;
	
	public static final String OWNER_OBJECT_NAME = "project a";
	@Mock
	WikiPageKey mockPageKey;
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		
		editor = new WikiSubpagesOrderEditor(
				mockView, 
				mockEditorTree,
				mockSynAlert,
				mockSynapseClient);
	}
	
	@Test
	public void testConfigure() {
		editor.configure(mockPageKey, OWNER_OBJECT_NAME);
	}
	
	@Test
	public void testGetTree() {
		editor.configure(mockPageKey, OWNER_OBJECT_NAME);
		assertEquals(mockEditorTree, editor.getTree());
	}
	
}
