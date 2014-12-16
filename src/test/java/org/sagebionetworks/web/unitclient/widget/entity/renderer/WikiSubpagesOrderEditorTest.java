package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor.HasChangesHandler;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditorView;

public class WikiSubpagesOrderEditorTest {
	
	WikiSubpagesOrderEditor editor;
	
	WikiSubpagesOrderEditorView mockView;
	
	Callback mockCallback;
	HasChangesHandler mockHasChangesHandler;
	
	@Before
	public void before(){
		mockView = Mockito.mock(WikiSubpagesOrderEditorView.class);
		mockCallback = Mockito.mock(Callback.class);
		mockHasChangesHandler = Mockito.mock(HasChangesHandler.class);
		
		editor = new WikiSubpagesOrderEditor(mockView);
	}
	
	@Test
	public void testConfigure() {
		editor.configure(null, mockHasChangesHandler);
		verify(mockView).configure(null, mockHasChangesHandler);
	}
	
	@Test
	public void testPushChangesToSynapse() {
		editor.pushChangesToSynapse(mockCallback);
		verify(mockCallback).invoke();
	}
	
}
