package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor.HasChangesHandler;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditorView;

public class WikiSubpagesOrderEditorTest {
	
	WikiSubpagesOrderEditor editor;
	
	WikiSubpagesOrderEditorView mockView;
	WikiSubpageOrderEditorTree mockEditorTree;
	HasChangesHandler mockHandler;
	
	
	@Before
	public void before(){
		mockView = Mockito.mock(WikiSubpagesOrderEditorView.class);
		mockEditorTree = Mockito.mock(WikiSubpageOrderEditorTree.class);
		mockHandler = Mockito.mock(HasChangesHandler.class);
		
		editor = new WikiSubpagesOrderEditor(mockView, mockEditorTree);
	}
	
	@Test
	public void testConfigure() {
		List<V2WikiHeader> wikiHeaders = new ArrayList<V2WikiHeader>();
		String ownerObjectName = "A";
		editor.configure(wikiHeaders, ownerObjectName, mockHandler);
		
		verify(mockEditorTree).configure(wikiHeaders, ownerObjectName);
		verify(mockView).configure(mockEditorTree, mockHandler);
	}
	
	@Test
	public void testGetTree() {
		editor.configure(new ArrayList<V2WikiHeader>(), "A", mockHandler);
		assertEquals(mockEditorTree, editor.getTree());
	}
	
}
