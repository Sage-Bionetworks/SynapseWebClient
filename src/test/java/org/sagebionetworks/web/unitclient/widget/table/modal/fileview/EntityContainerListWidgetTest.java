package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidgetView;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.SelfReturningAnswer;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityContainerListWidgetTest {

	@Mock
	EntityContainerListWidgetView mockView;
	EntityFinderWidget.Builder mockEntityFinderBuilder;
	@Mock
    EntityFinderWidget mockEntityFinder;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	SynapseAlert mockSynapseAlert;

	EntityContainerListWidget widget;
	ArrayList<EntityHeader> entityHeaders;
	@Mock
	EntityHeader mockEntityHeader;
	String headerId = "963";
	Reference entityReference;
	String headerName = "project area 52";

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		mockEntityFinderBuilder = mock(EntityFinderWidget.Builder.class, new SelfReturningAnswer());
		when(mockEntityFinderBuilder.build()).thenReturn(mockEntityFinder);
		widget = new EntityContainerListWidget(mockView, mockEntityFinderBuilder, mockSynapseJavascriptClient, mockSynapseAlert);
		when(mockEntityHeader.getId()).thenReturn(headerId);
		when(mockEntityHeader.getName()).thenReturn(headerName);
		entityHeaders = new ArrayList<EntityHeader>();
		entityHeaders.add(mockEntityHeader);
		entityReference = new Reference();
		entityReference.setTargetId(headerId);
	}

	@After
	public void validate() {
		validateMockitoUsage();
	}


	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
	}

	@Test
	public void testConfigureHappyCase() {
		// configure with pre-defined entity id list
		AsyncMockStubber.callSuccessWith(entityHeaders).when(mockSynapseJavascriptClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		boolean canEdit = true;
		widget.configure(Collections.singletonList(entityReference), canEdit, TableType.files);
		verify(mockSynapseJavascriptClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		verify(mockView).setAddButtonVisible(true);
		verify(mockView).setNoContainers(false);
		verify(mockSynapseAlert).clear();
		boolean showDeleteButton = true;
		verify(mockView).addEntity(headerId, headerName, showDeleteButton);

		assertTrue(widget.getEntityIds().contains(headerId));
		widget.onRemoveEntity(headerId);
		assertTrue(widget.getEntityIds().isEmpty());

		boolean showVersions = false;
		verify(mockEntityFinderBuilder).setMultiSelect(true);
		verify(mockEntityFinderBuilder).setSelectedMultiHandler(any(EntityFinderWidget.SelectedHandler.class));
		verify(mockEntityFinderBuilder).setSelectableTypes(EntityFilter.CONTAINER);
		verify(mockEntityFinderBuilder).setShowVersions(showVersions);
		verify(mockEntityFinderBuilder).build();
	}

	@Test
	public void testMultipleConfigure() {
		// SWC-3562: old ids should be cleared when widget is re-configured
		AsyncMockStubber.callSuccessWith(entityHeaders).when(mockSynapseJavascriptClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		boolean canEdit = true;
		widget.configure(Collections.singletonList(entityReference), canEdit, TableType.projects);
		assertTrue(widget.getEntityIds().contains(headerId));
		assertEquals(1, widget.getEntityIds().size());

		boolean showVersions = false;
		verify(mockEntityFinderBuilder).setMultiSelect(true);
		verify(mockEntityFinderBuilder).setSelectedMultiHandler(any(EntityFinderWidget.SelectedHandler.class));
		verify(mockEntityFinderBuilder).setSelectableTypes(EntityFilter.PROJECT);
		verify(mockEntityFinderBuilder).setShowVersions(showVersions);

		widget.configure(Collections.singletonList(entityReference), canEdit, TableType.files);
		assertTrue(widget.getEntityIds().contains(headerId));
		assertEquals(1, widget.getEntityIds().size());
	}

	@Test
	public void testConfigureNoIdsNoEdit() {
		entityHeaders.clear();
		AsyncMockStubber.callSuccessWith(entityHeaders).when(mockSynapseJavascriptClient).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		boolean canEdit = false;
		widget.configure(Collections.EMPTY_LIST, canEdit, TableType.files);
		verify(mockSynapseJavascriptClient, never()).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		verify(mockView).setAddButtonVisible(false);
		verify(mockView).setNoContainers(true);
		verify(mockSynapseAlert).clear();
		verify(mockView, never()).addEntity(anyString(), anyString(), anyBoolean());

		assertTrue(widget.getEntityIds().isEmpty());
	}


	@Test
	public void testConfigureError() {
		Exception ex = new Exception("error!");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		boolean canEdit = false;
		widget.configure(Collections.singletonList(entityReference), canEdit, TableType.files);
		verify(mockSynapseJavascriptClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		verify(mockSynapseAlert).clear();
		verify(mockSynapseAlert).handleException(ex);
		verify(mockView, never()).addEntity(anyString(), anyString(), anyBoolean());
	}

	@Test
	public void testOnAddProject() {
		widget.configure(Collections.singletonList(entityReference), true, TableType.projects);
		widget.onAddEntity();
		verify(mockEntityFinder).show();
	}

	@Test
	public void testOnAddProjectId() {
		ArrayList<EntityHeader> returnList = new ArrayList<EntityHeader>();
		returnList.add(mockEntityHeader);
		widget.configure(Collections.singletonList(entityReference), true, TableType.projects);

		AsyncMockStubber.callSuccessWith(returnList).when(mockSynapseJavascriptClient).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		widget.onAddEntity(headerId);

		verify(mockView, times(2)).setNoContainers(false);
		verify(mockEntityFinder).hide();
		verify(mockView).addEntity(headerId, headerName, true);

		assertTrue(widget.getEntityIds().contains(headerId));
	}

	@Test
	public void testOnAddProjectIdFailure() {
		String error = "error during lookup!";
		Exception ex = new Exception(error);
		widget.configure(Collections.singletonList(entityReference), true, TableType.projects);

		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		widget.onAddEntity(headerId);

		verify(mockEntityFinder).showError(error);
	}

	@Test
	public void testSetValueInvalidResponse() {
		widget.configure(Collections.singletonList(entityReference), true, TableType.projects);

		AsyncMockStubber.callSuccessWith(new ArrayList<EntityHeader>()).when(mockSynapseJavascriptClient).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
		widget.onAddEntity(headerId);

		verify(mockEntityFinder).showError(DisplayConstants.ERROR_LOADING);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
