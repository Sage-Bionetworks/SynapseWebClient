package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.MaterializedView;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard;
import org.sagebionetworks.web.client.widget.table.modal.fileview.MaterializedViewEditor;
import org.sagebionetworks.web.client.widget.table.modal.fileview.MaterializedViewEditorView;

@RunWith(MockitoJUnitRunner.class)
public class MaterializedViewEditorTest {

	@Mock
	MaterializedViewEditorView mockView;
	@Mock
    EntityFinderWidget mockEntityFinder;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	MaterializedView mockMaterializedView;
	@Captor
	ArgumentCaptor<Entity> entityCaptor;
	MaterializedViewEditor widget;
	
	public static final String MATERIALIZED_VIEW_ID = "syn42";
	
	@Before
	public void before() {
		widget = new MaterializedViewEditor(mockView, mockSynapseJavascriptClient, mockSynapseAlert, mockGlobalAppState);
		when(mockMaterializedView.getId()).thenReturn(MATERIALIZED_VIEW_ID);
		when(mockSynapseJavascriptClient.createEntity(any(Entity.class))).thenReturn(getDoneFuture(mockMaterializedView));
		when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}

	@After
	public void validate() {
		validateMockitoUsage();
	}


	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setHelp(MaterializedViewEditor.MATERIALIZED_VIEW_HELP_MARKDOWN, CreateTableViewWizard.VIEW_URL);
		verify(mockView).setSynAlert(mockSynapseAlert);
	}

	@Test
	public void testHappyPath() {
		String projectId = "syn112358";
		widget.configure(projectId).show();

		verify(mockSynapseAlert).clear();
		verify(mockView).reset();
		verify(mockView).show();
		
		String name = "a new view";
		String definingSql = "select * from this join that";
		String description = "this describes my new materialized view";
		when(mockView.getName()).thenReturn(name);
		when(mockView.getDefiningSql()).thenReturn(definingSql);
		when(mockView.getDescription()).thenReturn(description);

		widget.onSave();

		verify(mockSynapseJavascriptClient).createEntity(entityCaptor.capture());
		MaterializedView newMaterializedView = (MaterializedView)entityCaptor.getValue();
		assertEquals(name, newMaterializedView.getName());
		assertEquals(definingSql, newMaterializedView.getDefiningSQL());
		assertEquals(description, newMaterializedView.getDescription());
		verify(mockView).hide();
		verify(mockPlaceChanger).goTo(any(Synapse.class));
	}
	
	@Test
	public void testFailedToSave() {
		Throwable error = new Exception("something went wrong");
		when(mockSynapseJavascriptClient.createEntity(any(Entity.class))).thenReturn(getFailedFuture(error));
		
		widget.onSave();

		verify(mockSynapseJavascriptClient).createEntity(any());
		verify(mockSynapseAlert).handleException(error);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
