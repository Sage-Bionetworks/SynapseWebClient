package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.IsWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.*;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EntityMetadataTest {

  @Mock
  EntityMetadataView mockView;

  @Mock
  VersionHistoryWidget mockFileHistoryWidget;

  @Mock
  EntityActionMenu mockActionMenuWidget;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  EntityModalWidget mockEntityModalWidget;

  String entityId = "syn123";
  String entityName = "testEntity";
  EntityMetadata widget;
  Folder folderEntity = new Folder();
  TableEntity tableEntity = new TableEntity();

  @Before
  public void before() {
    when(mockGinInjector.getVersionHistoryWidget())
      .thenReturn(mockFileHistoryWidget);
    widget =
      new EntityMetadata(mockView, mockGinInjector, mockEntityModalWidget);
  }

  @Test
  public void testConstruction() {
    verify(mockView, never()).setVersionHistoryWidget(any(IsWidget.class)); // lazily created
  }

  @Test
  public void testSetEntityBundleProject() {
    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = false;
    boolean canCertifiedUserEdit = true;
    when(permissions.getCanChangePermissions())
      .thenReturn(canChangePermissions);
    when(permissions.getCanCertifiedUserEdit())
      .thenReturn(canCertifiedUserEdit);
    Project project = new Project();
    project.setName(entityName);
    project.setId(entityId);
    EntityBundle bundle = new EntityBundle();
    bundle.setEntity(project);
    bundle.setPermissions(permissions);
    widget.configure(bundle, null, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(true);
  }

  @Test
  public void testSetEntityBundleDockerRepo() {
    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = true;
    boolean canCertifiedUserEdit = false;
    when(permissions.getCanChangePermissions())
      .thenReturn(canChangePermissions);
    when(permissions.getCanCertifiedUserEdit())
      .thenReturn(canCertifiedUserEdit);
    DockerRepository dockerRepo = new DockerRepository();
    dockerRepo.setName(entityName);
    dockerRepo.setId(entityId);
    EntityBundle bundle = new EntityBundle();
    bundle.setEntity(dockerRepo);
    bundle.setPermissions(permissions);
    Long versionNumber = null;
    widget.configure(bundle, versionNumber, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(false);
    verify(mockFileHistoryWidget, never())
      .setEntityBundle(bundle, versionNumber);
  }

  @Test
  public void testSetEntityBundleFileEntityMostRecent() {
    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = true;
    boolean canCertifiedUserEdit = false;
    boolean isCurrentVersion = true;
    when(permissions.getCanChangePermissions())
      .thenReturn(canChangePermissions);
    when(permissions.getCanCertifiedUserEdit())
      .thenReturn(canCertifiedUserEdit);
    FileEntity fileEntity = new FileEntity();
    fileEntity.setName(entityName);
    fileEntity.setId(entityId);
    fileEntity.setIsLatestVersion(isCurrentVersion);
    EntityBundle bundle = new EntityBundle();
    bundle.setEntity(fileEntity);
    bundle.setPermissions(permissions);
    Long versionNumber = null;
    widget.configure(bundle, versionNumber, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(false);
    verify(mockFileHistoryWidget).setEntityBundle(bundle, versionNumber);
    verify(mockFileHistoryWidget).setVisible(false);
  }

  @Test
  public void testSetEntityBundleFileEntityNotMostRecentVersion() {
    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = true;
    boolean canCertifiedUserEdit = false;
    boolean isCurrentVersion = false;
    when(permissions.getCanChangePermissions())
      .thenReturn(canChangePermissions);
    when(permissions.getCanCertifiedUserEdit())
      .thenReturn(canCertifiedUserEdit);
    Long versionNumber = -122L;
    FileEntity fileEntity = new FileEntity();
    fileEntity.setName(entityName);
    fileEntity.setId(entityId);
    fileEntity.setVersionNumber(versionNumber);
    fileEntity.setIsLatestVersion(isCurrentVersion);
    EntityBundle bundle = new EntityBundle();
    bundle.setEntity(fileEntity);
    bundle.setPermissions(permissions);
    widget.configure(bundle, versionNumber, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(false);
    verify(mockFileHistoryWidget).setEntityBundle(bundle, versionNumber);
  }

  @Test
  public void testSetEntityBundleFolder() {
    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = false;
    boolean canCertifiedUserEdit = true;
    when(permissions.getCanChangePermissions())
      .thenReturn(canChangePermissions);
    when(permissions.getCanCertifiedUserEdit())
      .thenReturn(canCertifiedUserEdit);
    EntityBundle bundle = new EntityBundle();
    bundle.setEntity(folderEntity);
    folderEntity.setId(entityId);
    bundle.setPermissions(permissions);
    widget.configure(bundle, null, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(false);
  }

  @Test
  public void testSetDescriptionForTable() {
    String description = "A description for a table";

    EntityBundle bundle = new EntityBundle();
    bundle.setEntity(tableEntity);
    tableEntity.setDescription(description);
    tableEntity.setId(entityId);
    tableEntity.setIsLatestVersion(true);

    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = false;
    boolean canCertifiedUserEdit = true;
    when(permissions.getCanChangePermissions())
      .thenReturn(canChangePermissions);
    when(permissions.getCanCertifiedUserEdit())
      .thenReturn(canCertifiedUserEdit);
    bundle.setPermissions(permissions);

    widget.configure(bundle, null, mockActionMenuWidget);

    verify(mockView).setDetailedMetadataVisible(false);
    verify(mockView).setDescription(description);
    verify(mockView).setDescriptionVisible(false);
  }

  @Test
  public void testNoDescriptionForNonTables() {
    String description = "A description for a FOLDER";

    EntityBundle bundle = new EntityBundle();
    bundle.setEntity(folderEntity);
    folderEntity.setDescription(description);
    folderEntity.setId(entityId);
    tableEntity.setIsLatestVersion(true);

    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = false;
    boolean canCertifiedUserEdit = true;
    when(permissions.getCanChangePermissions())
      .thenReturn(canChangePermissions);
    when(permissions.getCanCertifiedUserEdit())
      .thenReturn(canCertifiedUserEdit);
    bundle.setPermissions(permissions);

    widget.configure(bundle, null, mockActionMenuWidget);

    verify(mockView).setDescriptionVisible(false);
  }

  @Test
  public void testSetAnnotationsVisible() {
    widget.setAnnotationsVisible(true);

    verify(mockEntityModalWidget).setOpen(true);
  }
}
