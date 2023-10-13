package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.table.MaterializedView;
import org.sagebionetworks.repo.model.table.VirtualTable;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard;
import org.sagebionetworks.web.client.widget.table.modal.fileview.SqlDefinedTableEditor;
import org.sagebionetworks.web.client.widget.table.modal.fileview.SqlDefinedTableEditorView;

@RunWith(MockitoJUnitRunner.class)
public class SqlDefinedTableEditorViewTest {

  @Mock
  SqlDefinedTableEditorView mockView;

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

  SqlDefinedTableEditor widget;

  public static final String MATERIALIZED_VIEW_ID = "syn42";

  @Before
  public void before() {
    widget =
      new SqlDefinedTableEditor(
        mockView,
        mockSynapseJavascriptClient,
        mockSynapseAlert,
        mockGlobalAppState
      );
    when(mockMaterializedView.getId()).thenReturn(MATERIALIZED_VIEW_ID);
    when(mockSynapseJavascriptClient.createEntity(any(Entity.class)))
      .thenReturn(getDoneFuture(mockMaterializedView));
    when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
  }

  @After
  public void validate() {
    validateMockitoUsage();
  }

  @Test
  public void testConstruction() {
    verify(mockView).setPresenter(widget);
    verify(mockView).setSynAlert(mockSynapseAlert);
  }

  @Test
  public void testHappyPathMaterializedView() {
    String projectId = "syn112358";
    widget.configure(projectId, EntityType.materializedview).show();

    verify(mockSynapseAlert).clear();
    verify(mockView).reset();
    verify(mockView).setModalTitle("Create Materialized View");
    verify(mockView)
      .setHelp(
        SqlDefinedTableEditor.MATERIALIZED_VIEW_HELP_MARKDOWN,
        CreateTableViewWizard.VIEW_URL
      );
    verify(mockView).show();

    String name = "a new view";
    String definingSql = "select * from this join that";
    String description = "this describes my new materialized view";
    when(mockView.getName()).thenReturn(name);
    when(mockView.getDefiningSql()).thenReturn(definingSql);
    when(mockView.getDescription()).thenReturn(description);

    widget.onSave();

    verify(mockSynapseJavascriptClient).createEntity(entityCaptor.capture());
    MaterializedView newMaterializedView = (MaterializedView) entityCaptor.getValue();
    assertEquals(name, newMaterializedView.getName());
    assertEquals(definingSql, newMaterializedView.getDefiningSQL());
    assertEquals(description, newMaterializedView.getDescription());
    verify(mockView).hide();
    verify(mockPlaceChanger).goTo(any(Synapse.class));
  }

  @Test
  public void testHappyPathVirtualTable() {
    String projectId = "syn112358";
    widget.configure(projectId, EntityType.virtualtable).show();

    verify(mockSynapseAlert).clear();
    verify(mockView).reset();
    verify(mockView).setModalTitle("Create Virtual Table");
    verify(mockView)
      .setHelp(
        SqlDefinedTableEditor.VIRTUAL_TABLE_HELP_MARKDOWN,
        CreateTableViewWizard.VIEW_URL
      );
    verify(mockView).show();

    String name = "a new view";
    String definingSql = "select * from this join that";
    String description = "this describes my new virtual table";
    when(mockView.getName()).thenReturn(name);
    when(mockView.getDefiningSql()).thenReturn(definingSql);
    when(mockView.getDescription()).thenReturn(description);

    widget.onSave();

    verify(mockSynapseJavascriptClient).createEntity(entityCaptor.capture());
    VirtualTable newVirtualTable = (VirtualTable) entityCaptor.getValue();
    assertEquals(name, newVirtualTable.getName());
    assertEquals(definingSql, newVirtualTable.getDefiningSQL());
    assertEquals(description, newVirtualTable.getDescription());
    verify(mockView).hide();
    verify(mockPlaceChanger).goTo(any(Synapse.class));
  }

  @Test
  public void testFailedToSave() {
    Throwable error = new Exception("something went wrong");
    when(mockSynapseJavascriptClient.createEntity(any(Entity.class)))
      .thenReturn(getFailedFuture(error));
    widget.configure("", EntityType.materializedview);

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
