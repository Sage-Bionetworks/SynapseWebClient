package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditorModalWidgetImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.ui.Tree;

public class WikiSubpagesOrderEditorModalWidgetImplTest {
	AccessControlListModalWidgetView mockView;
	WikiSubpagesOrderEditor mockEditor;
	Entity mockEntity;
	Callback mockCallback;
	
	Tree tree;
	
	WikiSubpagesOrderEditorModalWidgetImpl modal;
	
	@Before
	public void before(){
		mockView = Mockito.mock(AccessControlListModalWidgetView.class);
		mockEditor = Mockito.mock(WikiSubpagesOrderEditor.class);
		mockEntity = Mockito.mock(Entity.class);
		mockCallback = Mockito.mock(Callback.class);
		
		modal = new WikiSubpagesOrderEditorModalWidgetImpl(mockView, mockEditor);
	}
	
	@Test
	public void testConfigure() {
		modal.configure(null, mockCallback);
		verify(mockEditor).configure(null,  modal);
	}
	
	
	@Test
	public void testOnChange(){
		modal.configure(null, mockCallback);
		modal.hasChanges(true);
		verify(mockView).setLoading(false);
		verify(mockView).setPrimaryButtonEnabled(true);
	}
	
	@Test
	public void testOnChangeNoChange(){
		modal.configure(null, mockCallback);
		modal.hasChanges(false);
		verify(mockView).setLoading(false);
		verify(mockView).setPrimaryButtonEnabled(false);
	}
	
	@Test
	public void testShowSharing(){
		modal.configure(null, mockCallback);
		modal.show(mockCallback);
		verify(mockView).setLoading(false);
		verify(mockView).showDialog();
	}
	
	@Test
	public void testOnPrimary(){
		// Invoke the callback.
		AsyncMockStubber.callWithInvoke().when(mockEditor).pushChangesToSynapse(any(Callback.class));
		modal.configure(null, mockCallback);
		modal.show(mockCallback);
		modal.onPrimary();
		verify(mockView).hideDialog();
		verify(mockCallback).invoke();
	}
	
	@Test
	public void testGetTree() {
		modal.configure(null, mockCallback);
		modal.getTree();
		verify(mockEditor).getTree();
	}
	
	@Test
	public void testSetSize() {
		modal.configure(null, mockCallback);
		modal.setSize(ModalSize.SMALL);
		verify(mockView).setSize(ModalSize.SMALL);
	}
	
}
