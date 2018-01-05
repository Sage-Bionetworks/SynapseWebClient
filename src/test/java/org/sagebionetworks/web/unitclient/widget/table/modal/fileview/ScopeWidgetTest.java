package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ScopeWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ScopeWidgetView;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
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
		when(mockEntityView.getType()).thenReturn(ViewType.file);
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
		verify(mockViewScopeWidget).configure(mockScopeIds, false, TableType.fileview);
		verify(mockView).setEditButtonVisible(true);
		verify(mockView).setVisible(true);
		
		// edit
		widget.onEdit();
		verify(mockEditScopeWidget).configure(mockScopeIds, true, TableType.fileview);
		verify(mockView).showModal();
		
		//update file view to file+table view
		widget.onSelectFilesAndTablesView();
		
		// save new scope
		widget.onSave();
		
		verify(mockSynapseAlert).clear();
		verify(mockView).setLoading(true);
		//verify view type has been updated
		verify(mockEntityView).setType(ViewType.file_and_table);
		verify(mockSynapseClient).updateEntity(any(Table.class), any(AsyncCallback.class));
		verify(mockView).setLoading(false);
		verify(mockView).hideModal();
		verify(mockEntityUpdatedHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testConfigureHappyCaseProjectView() {
		when(mockEntityView.getType()).thenReturn(ViewType.project);
		// configure with an entityview, edit the scope, and save.
		boolean isEditable = true;
		widget.configure(mockBundle, isEditable, mockEntityUpdatedHandler);
		
		// The view scope widget does not allow edit of the scope.  That occurs in the modal (with the editScopeWidget)
		verify(mockViewScopeWidget).configure(mockScopeIds, false, TableType.projectview);
		
		// edit
		widget.onEdit();
		// Do not show File View options for project view
		verify(mockView).setFileViewTypeSelectionVisible(false);
		verify(mockEditScopeWidget).configure(mockScopeIds, true, TableType.projectview);
	}
	
	@Test
	public void testConfigureFileView() {
		when(mockEntityView.getType()).thenReturn(ViewType.file);
		boolean isEditable = true;
		
		widget.configure(mockBundle, isEditable, mockEntityUpdatedHandler);
		widget.onEdit();
		
		// Show File View options for project view
		verify(mockView).setFileViewTypeSelectionVisible(true);
		verify(mockView).setIsIncludeTables(false);
	}
	
	@Test
	public void testConfigureFileAndTableView() {
		when(mockEntityView.getType()).thenReturn(ViewType.file_and_table);
		boolean isEditable = true;
		
		widget.configure(mockBundle, isEditable, mockEntityUpdatedHandler);
		widget.onEdit();
		
		// Show File View options for project view
		verify(mockView).setFileViewTypeSelectionVisible(true);
		verify(mockView).setIsIncludeTables(true);
		
		//verify update view type from file+table to file
		widget.onSelectFilesOnlyView();
		widget.onSave();
		
		verify(mockEntityView).setType(ViewType.file);
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
		
		verify(mockViewScopeWidget).configure(mockScopeIds, false, TableType.fileview);
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
