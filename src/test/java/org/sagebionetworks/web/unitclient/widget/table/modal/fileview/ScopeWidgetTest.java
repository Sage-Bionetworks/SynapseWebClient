package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidgetView;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ScopeWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ScopeWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ScopeWidgetTest {

	@Mock
	ScopeWidgetView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	EntityContainerListWidget mockViewScopeWidget;
	@Mock
	EntityContainerListWidget mockEditScopeWidget;
	@Mock
	EntityBundle mockBundle;
	@Mock
	List<String> mockScopeIds;
	@Mock
	List<String> mockNewScopeIds;
	ScopeWidget widget;
	@Mock
	EntityView mockEntityView;
	@Mock
	EntityView mockUpdatedEntityView;
	
	@Mock
	Table mockTable;
	
	@Mock
	EntityUpdatedHandler mockEntityUpdatedHandler;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		widget = new ScopeWidget(mockView, mockSynapseClient, mockViewScopeWidget, mockEditScopeWidget, mockSynapseAlert);
		when(mockEntityView.getId()).thenReturn("syn123");
		when(mockEntityView.getScopeIds()).thenReturn(mockScopeIds);
		when(mockBundle.getEntity()).thenReturn(mockEntityView);
		AsyncMockStubber.callSuccessWith(mockUpdatedEntityView).when(mockSynapseClient).updateEntity(any(Table.class), any(AsyncCallback.class));
	}
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setEditableEntityListWidget(any(Widget.class));
		verify(mockView).setEntityListWidget(any(Widget.class));
		verify(mockView).setSynAlert(any(Widget.class));
	}
	
	@Test
	public void testConfigureHappyCase() {
		// configure with an entityview, edit the scope, and save.
		boolean isEditable = true;
		widget.configure(mockBundle, isEditable, mockEntityUpdatedHandler);
		
		// The view scope widget does not allow edit of the scope.  That occurs in the modal (with the editScopeWidget)
		verify(mockViewScopeWidget).configure(mockScopeIds, false);
		verify(mockView).setEditButtonVisible(true);
		verify(mockView).setVisible(true);
		
		// edit
		widget.onEdit();
		verify(mockEditScopeWidget).configure(mockScopeIds, true);
		verify(mockView).showModal();
		
		// save new scope
		widget.onSave();
		verify(mockSynapseAlert).clear();
		verify(mockView).setLoading(true);
		verify(mockSynapseClient).updateEntity(any(Table.class), any(AsyncCallback.class));
		verify(mockView).setLoading(false);
		verify(mockView).hideModal();
		verify(mockEntityUpdatedHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testConfigureNotEntityView() {
		boolean isEditable = true;
		when(mockBundle.getEntity()).thenReturn(mockTable);
		widget.configure(mockBundle, isEditable, mockEntityUpdatedHandler);
		
		verify(mockView).setVisible(false);
	}
	
	@Test
	public void testConfigureNotEditable() {
		boolean isEditable = false;
		widget.configure(mockBundle, isEditable, mockEntityUpdatedHandler);
		
		verify(mockViewScopeWidget).configure(mockScopeIds, false);
		verify(mockView).setEditButtonVisible(false);
		verify(mockView).setVisible(true);
	}
	
	@Test
	public void testOnSaveFailure() {
		Exception ex = new Exception("error on save");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).updateEntity(any(Table.class), any(AsyncCallback.class));
		boolean isEditable = true;
		
		widget.configure(mockBundle, isEditable, mockEntityUpdatedHandler);
		widget.onSave();
		
		verify(mockSynapseAlert).clear();
		verify(mockView).setLoading(true);
		verify(mockSynapseClient).updateEntity(any(Table.class), any(AsyncCallback.class));
		verify(mockView).setLoading(false);
		verify(mockSynapseAlert).handleException(ex);
		verify(mockView, never()).hideModal();
		verify(mockEntityUpdatedHandler, never()).onPersistSuccess(any(EntityUpdatedEvent.class));
	}
	
	

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
