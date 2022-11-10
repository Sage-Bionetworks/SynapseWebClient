package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.MaterializedView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.widget.entity.VersionHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.file.TableTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.TableTitleBarView;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;

@RunWith(MockitoJUnitRunner.class)
public class TableTitleBarTest {

  TableTitleBar tableTitleBar;

  @Mock
  TableTitleBarView mockView;

  @Mock
  EntityBundle mockBundle;

  @Mock
  TableEntity mockTable;

  @Mock
  Dataset mockDataset;

  @Mock
  MaterializedView mockMaterializedView;

  @Mock
  ActionMenuWidget mockActionMenuWidget;

  @Mock
  UserEntityPermissions mockPermissions;

  @Mock
  VersionHistoryWidget mockVersionHistoryWidget;

  @Captor
  ArgumentCaptor<Consumer<Boolean>> versionHistoryVisibilityListenerCaptor;

  public static final String ENTITY_ID = "syn123";
  public static final Long TABLE_VERSION = 3L;
  public static final String TABLE_NAME = "My Table";

  @Before
  public void setup() {
    tableTitleBar = new TableTitleBar(mockView);
    when(mockTable.getId()).thenReturn(ENTITY_ID);
    when(mockTable.getName()).thenReturn(TABLE_NAME);
    when(mockTable.getVersionNumber()).thenReturn(TABLE_VERSION);
    when(mockTable.getIsLatestVersion()).thenReturn(true);

    when(mockDataset.getId()).thenReturn(ENTITY_ID);
    when(mockDataset.getName()).thenReturn(TABLE_NAME);
    when(mockDataset.getVersionNumber()).thenReturn(TABLE_VERSION);
    when(mockDataset.getIsLatestVersion()).thenReturn(true);

    when(mockBundle.getEntity()).thenReturn(mockTable);
    when(mockBundle.getPermissions()).thenReturn(mockPermissions);
    when(mockPermissions.getCanDownload()).thenReturn(true);
  }

  @Test
  public void testConstruction() {
    verify(mockView).setPresenter(tableTitleBar);
  }

  @Test
  public void testAsWidget() {
    tableTitleBar.asWidget();
  }

  @Test
  public void testConfigure() {
    tableTitleBar.configure(
      mockBundle,
      mockActionMenuWidget,
      mockVersionHistoryWidget
    );

    verify(mockView).createTitlebar(mockTable);
    verify(mockView).setEntityName(TABLE_NAME);
    verify(mockView).setVersionUIToggleVisible(true);
  }

  @Test
  public void testGetTableCurrentVersion() {
    tableTitleBar.configure(
      mockBundle,
      mockActionMenuWidget,
      mockVersionHistoryWidget
    );

    verify(mockView).setVersionLabel("Current");
  }

  @Test
  public void testGetTableSnapshotVersion() {
    when(mockTable.getIsLatestVersion()).thenReturn(false);

    tableTitleBar.configure(
      mockBundle,
      mockActionMenuWidget,
      mockVersionHistoryWidget
    );

    verify(mockView).setVersionLabel(TABLE_VERSION + " (Snapshot)");
  }

  @Test
  public void testGetDatasetCurrentVersion() {
    when(mockBundle.getEntity()).thenReturn(mockDataset);

    tableTitleBar.configure(
      mockBundle,
      mockActionMenuWidget,
      mockVersionHistoryWidget
    );

    verify(mockView).setVersionLabel("Draft");
    verify(mockView).setVersionUIToggleVisible(true);
  }

  @Test
  public void testGetDatasetSnapshotVersion() {
    when(mockBundle.getEntity()).thenReturn(mockDataset);
    when(mockDataset.getIsLatestVersion()).thenReturn(false);

    tableTitleBar.configure(
      mockBundle,
      mockActionMenuWidget,
      mockVersionHistoryWidget
    );

    verify(mockView).setVersionLabel(TABLE_VERSION + " (Stable)");
  }

  @Test
  public void testConfigureMaterializedView() {
    when(mockBundle.getEntity()).thenReturn(mockMaterializedView);

    tableTitleBar.configure(
      mockBundle,
      mockActionMenuWidget,
      mockVersionHistoryWidget
    );

    verify(mockView).setVersionUIToggleVisible(false);
  }

  @Test
  public void testOnVersionHistoryVisibilityUpdate() {
    tableTitleBar.configure(
      mockBundle,
      mockActionMenuWidget,
      mockVersionHistoryWidget
    );

    verify(mockVersionHistoryWidget)
      .registerVisibilityChangeListener(
        versionHistoryVisibilityListenerCaptor.capture()
      );

    // Version history updated to be visible
    versionHistoryVisibilityListenerCaptor.getValue().accept(true);
    verify(mockView).setVersionHistoryLinkText(eq("Hide Version History"));

    // Version history updated to not be visible
    versionHistoryVisibilityListenerCaptor.getValue().accept(false);
    verify(mockView).setVersionHistoryLinkText(eq("Show Version History"));
  }
}
