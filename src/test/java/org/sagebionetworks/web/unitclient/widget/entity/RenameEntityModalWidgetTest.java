package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.BUTTON_TEXT;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.LABLE_SUFFIX;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.TITLE_PREFIX;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EntityNameModalView;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class RenameEntityModalWidgetTest {

	EntityNameModalView mockView;
	SynapseClientAsync mockSynapseClient;
	Callback mockCallback;
	EntityTypeProvider mockTypeProvider;
	String startName;
	String entityDispalyType;
	String parentId;
	RenameEntityModalWidgetImpl widget;
	TableEntity entity;
	
	@Before
	public void before(){
		mockView = Mockito.mock(EntityNameModalView.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockCallback = Mockito.mock(Callback.class);
		mockTypeProvider = Mockito.mock(EntityTypeProvider.class);
		entity = new TableEntity();
		startName = "Start Name";
		entity.setName(startName);
		entityDispalyType = "Table";
		when(mockTypeProvider.getEntityDispalyName(entity)).thenReturn(entityDispalyType);
		widget = new RenameEntityModalWidgetImpl(mockView, mockSynapseClient, mockTypeProvider);
	}
	
	@Test
	public void testOnRename(){
		widget.onRename(entity, mockCallback);
		verify(mockView).clear();
		verify(mockView).show();
		verify(mockView).configure(TITLE_PREFIX+entityDispalyType, entityDispalyType+LABLE_SUFFIX, BUTTON_TEXT, startName);
	}
	
	@Test
	public void testNullName(){
		widget.onRename(entity, mockCallback);
		when(mockView.getName()).thenReturn(null);
		widget.onPrimary();
		verify(mockView).showError(NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockSynapseClient, never()).updateEntity(any(Entity.class), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}
	
	@Test
	public void testNameNotChanged(){
		widget.onRename(entity, mockCallback);
		when(mockView.getName()).thenReturn(startName);
		// Calling save with no real change just closes the dialog.
		widget.onPrimary();
		verify(mockView, never()).setLoading(true);
		verify(mockView).hide();
		verify(mockSynapseClient, never()).updateEntity(any(Entity.class), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}
	
	@Test
	public void testRenameHappy(){
		String newName = "a new name";
		widget.onRename(entity, mockCallback);
		when(mockView.getName()).thenReturn(newName);
		AsyncMockStubber.callSuccessWith(new TableEntity()).when(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		// save button
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).hide();
		verify(mockCallback).invoke();
	}
	
	@Test
	public void testRenameFailed(){
		Exception error = new Exception("an object already exists with that name");
		String newName = "a new name";
		widget.onRename(entity, mockCallback);
		when(mockView.getName()).thenReturn(newName);
		AsyncMockStubber.callFailureWith(error).when(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		// save button
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).showError(error.getMessage());
		verify(mockView).setLoading(false);
		verify(mockView, never()).hide();
		verify(mockCallback, never()).invoke();
	}

}
