package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityContainerListWidgetTest {

	@Mock
	EntityContainerListWidgetView mockView;
	@Mock
	EntityFinder mockEntityFinder;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseAlert mockSynapseAlert;
	
	EntityContainerListWidget widget;
	ArrayList<EntityHeader> entityHeaders;
	@Mock
	EntityHeader mockEntityHeader;
	String headerId = "963";
	String headerName = "project area 52";
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		widget = new EntityContainerListWidget(mockView, mockEntityFinder, mockSynapseClient, mockSynapseAlert);
		when(mockEntityHeader.getId()).thenReturn(headerId);
		when(mockEntityHeader.getName()).thenReturn(headerName);
		entityHeaders = new ArrayList<EntityHeader>();
		entityHeaders.add(mockEntityHeader);
	}
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		boolean showVersions = false;
		verify(mockEntityFinder).configure(eq(EntityFilter.CONTAINER), eq(showVersions), any(SelectedHandler.class));
	}
	
	@Test
	public void testConfigureHappyCase() {
		// configure with pre-defined entity id list
		AsyncMockStubber.callSuccessWith(entityHeaders).when(mockSynapseClient).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		boolean canEdit = true;
		widget.configure(Collections.singletonList(headerId), canEdit);
		verify(mockSynapseClient).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		verify(mockView).setAddButtonVisible(true);
		verify(mockView).setNoContainers(false);
		verify(mockSynapseAlert).clear();
		boolean showDeleteButton = true;
		verify(mockView).addEntity(headerId, headerName, showDeleteButton);
		
		assertTrue(widget.getEntityIds().contains(headerId));
		widget.onRemoveProject(headerId);
		assertTrue(widget.getEntityIds().isEmpty());
	}
	
	@Test
	public void testConfigureNoIdsNoEdit() {
		entityHeaders.clear();
		AsyncMockStubber.callSuccessWith(entityHeaders).when(mockSynapseClient).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		boolean canEdit = false;
		widget.configure(Collections.EMPTY_LIST, canEdit);
		verify(mockSynapseClient, never()).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		verify(mockView).setAddButtonVisible(false);
		verify(mockView).setNoContainers(true);
		verify(mockSynapseAlert).clear();
		verify(mockView, never()).addEntity(anyString(), anyString(), anyBoolean());
		
		assertTrue(widget.getEntityIds().isEmpty());
	}
	

	@Test
	public void testConfigureError() {
		Exception ex = new Exception("error!"); 
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		boolean canEdit = false;
		widget.configure(Collections.singletonList(headerId), canEdit);
		verify(mockSynapseClient).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		verify(mockSynapseAlert).clear();
		verify(mockSynapseAlert).handleException(ex);
		verify(mockView, never()).addEntity(anyString(), anyString(), anyBoolean());
	}

	@Test
	public void testOnAddProject() {
		widget.onAddProject();
		verify(mockEntityFinder).show();
	}

	@Test
	public void testOnAddProjectId() {
		ArrayList<EntityHeader> returnList = new ArrayList<EntityHeader>();
		returnList.add(mockEntityHeader);
		AsyncMockStubber.callSuccessWith(returnList).when(mockSynapseClient).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		widget.onAddProject(headerId);
		
		verify(mockView).setNoContainers(false);
		verify(mockEntityFinder).hide();
		verify(mockView).addEntity(headerId, headerName, true);
		
		assertTrue(widget.getEntityIds().contains(headerId));
	}
	
	@Test
	public void testOnAddProjectIdFailure() {
		String error = "error during lookup!";
		Exception ex = new Exception(error); 
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		widget.onAddProject(headerId);
		
		verify(mockEntityFinder).showError(error);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
