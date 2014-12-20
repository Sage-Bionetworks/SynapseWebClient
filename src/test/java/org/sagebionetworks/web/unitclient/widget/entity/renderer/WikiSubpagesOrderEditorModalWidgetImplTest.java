package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditorModalWidgetImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditorModalWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget.UpdateOrderHintCallback;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetView;

import com.google.gwt.user.client.ui.Tree;

public class WikiSubpagesOrderEditorModalWidgetImplTest {
	WikiSubpagesOrderEditorModalWidgetView mockView;
	WikiSubpagesOrderEditor mockEditor;
	UpdateOrderHintCallback mockCallback;
	
	Tree tree;
	
	WikiSubpagesOrderEditorModalWidgetImpl modal;
	
	@Before
	public void before(){
		mockView = Mockito.mock(WikiSubpagesOrderEditorModalWidgetView.class);
		mockEditor = Mockito.mock(WikiSubpagesOrderEditor.class);
		mockCallback = Mockito.mock(UpdateOrderHintCallback.class);
		
		modal = new WikiSubpagesOrderEditorModalWidgetImpl(mockView, mockEditor);
	}
	
	@Test
	public void testConfigure() {
		List<JSONEntity> headers = new ArrayList<JSONEntity>();
		modal.configure(headers, "A");
		verify(mockEditor).configure(headers,  "A", modal);
	}
	
	
	@Test
	public void testOnChange(){
		modal.configure(new ArrayList<JSONEntity>(), "A");
		modal.hasChanges(true);
		verify(mockView).setLoading(false);
		verify(mockView).setPrimaryButtonEnabled(true);
	}
	
	@Test
	public void testOnChangeNoChange(){
		modal.configure(new ArrayList<JSONEntity>(), "A");
		modal.hasChanges(false);
		verify(mockView).setLoading(false);
		verify(mockView).setPrimaryButtonEnabled(false);
	}
	
	@Test
	public void testShow(){
		modal.configure(new ArrayList<JSONEntity>(), "A");
		modal.show(mockCallback);
		verify(mockView).setLoading(false);
		verify(mockView).showDialog();
	}
	
	@Test
	public void testOnPrimary(){
		Mockito.doNothing().when(mockCallback).updateOrderHint(any(List.class));
		WikiSubpageOrderEditorTree mockTree = Mockito.mock(WikiSubpageOrderEditorTree.class);
		when(mockTree.getIdListOrderHint()).thenReturn(new ArrayList<String>());
		when(mockEditor.getTree()).thenReturn(mockTree);
		// Invoke the callback.
		modal.configure(new ArrayList<JSONEntity>(), "A");
		modal.show(mockCallback);
		modal.onPrimary();
		verify(mockView).hideDialog();
		verify(mockCallback).updateOrderHint(any(List.class));
	}
	
	@Test
	public void testGetTree() {
		modal.configure(new ArrayList<JSONEntity>(), "A");
		modal.getTree();
		verify(mockEditor).getTree();
	}
	
}
