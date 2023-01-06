package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.doi.v2.DoiAssociation;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.ExternalGoogleCloudUploadDestination;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreUploadDestination;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.ExternalUploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2;
import org.sagebionetworks.web.client.widget.entity.ContainerItemCountWidget;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView;
import org.sagebionetworks.web.client.widget.entity.VersionHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.class)
public class EntityMetadataTest {

  @Mock
  EntityMetadataView mockView;

  @Mock
  AnnotationsRendererWidget mockAnnotationsWidget;

  @Mock
  DoiWidgetV2 mockDoiWidgetV2;

  @Mock
  RestrictionWidget mockRestrictionWidgetV2;

  @Mock
  VersionHistoryWidget mockFileHistoryWidget;

  @Mock
  DoiAssociation mockDoiAssociation;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  SynapseJSNIUtils mockJSNI;

  @Mock
  EntityActionMenu mockActionMenuWidget;

  @Mock
  CookieProvider mockCookies;

  @Mock
  ContainerItemCountWidget mockItemCountWidget;

  @Mock
  PortalGinInjector mockGinInjector;

  String entityId = "syn123";
  String entityName = "testEntity";
  EntityMetadata widget;
  Folder folderEntity = new Folder();
  TableEntity tableEntity = new TableEntity();

  @Before
  public void before() {
    when(mockGinInjector.getVersionHistoryWidget())
      .thenReturn(mockFileHistoryWidget);
    when(mockGinInjector.getCookieProvider()).thenReturn(mockCookies);
    widget =
      new EntityMetadata(
        mockView,
        mockDoiWidgetV2,
        mockAnnotationsWidget,
        mockJsClient,
        mockJSNI,
        mockRestrictionWidgetV2,
        mockItemCountWidget,
        mockGinInjector
      );
  }

  @Test
  public void testConstruction() {
    verify(mockView).setDoiWidget(any(IsWidget.class));
    verify(mockView).setAnnotationsRendererWidget(any(IsWidget.class));
    verify(mockView, never()).setVersionHistoryWidget(any(IsWidget.class)); // lazily created
    verify(mockView).setRestrictionWidgetV2(any(IsWidget.class));
    verify(mockRestrictionWidgetV2).setShowChangeLink(true);
    verify(mockView).setRestrictionWidgetV2Visible(true);
  }

  @Test
  public void testSetEntityBundleProject() {
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn(null);
    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = false;
    boolean canCertifiedUserEdit = true;
    boolean isCurrentVersion = true;
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
    bundle.setDoiAssociation(mockDoiAssociation);
    widget.configure(bundle, null, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(true);
    verify(mockView).setRestrictionPanelVisible(false);
    verify(mockDoiWidgetV2).configure(mockDoiAssociation);
    verify(mockAnnotationsWidget)
      .configure(bundle, canCertifiedUserEdit, isCurrentVersion);
    verify(mockRestrictionWidgetV2).configure(project, canChangePermissions);
    verify(mockView, never()).setRestrictionWidgetV2Visible(false);
    verify(mockItemCountWidget, never()).configure(anyString());
  }

  @Test
  public void testSetEntityBundleProjectInAlphaMode() {
    // Use this test to verify alpha mode behavior
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = false;
    boolean canCertifiedUserEdit = true;
    boolean isCurrentVersion = true;
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
    bundle.setDoiAssociation(mockDoiAssociation);
    widget.configure(bundle, null, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(true);
    verify(mockView).setRestrictionPanelVisible(false);
    verify(mockDoiWidgetV2).configure(mockDoiAssociation);
    verify(mockAnnotationsWidget)
      .configure(bundle, canCertifiedUserEdit, isCurrentVersion);
    verify(mockRestrictionWidgetV2).configure(project, canChangePermissions);
    verify(mockView, never()).setRestrictionWidgetV2Visible(false);
    verify(mockItemCountWidget, never()).configure(anyString());
  }

  @Test
  public void testSetEntityBundleDockerRepo() {
    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = true;
    boolean canCertifiedUserEdit = false;
    boolean isCurrentVersion = true;
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
    bundle.setDoiAssociation(mockDoiAssociation);
    Long versionNumber = null;
    widget.configure(bundle, versionNumber, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(false);
    verify(mockFileHistoryWidget, never())
      .setEntityBundle(bundle, versionNumber);
    verify(mockDoiWidgetV2).configure(mockDoiAssociation);
    verify(mockAnnotationsWidget)
      .configure(bundle, canCertifiedUserEdit, isCurrentVersion);
    verify(mockItemCountWidget, never()).configure(anyString());
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
    bundle.setDoiAssociation(mockDoiAssociation);
    Long versionNumber = null;
    widget.configure(bundle, versionNumber, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(false);
    verify(mockFileHistoryWidget).setEntityBundle(bundle, versionNumber);
    verify(mockFileHistoryWidget).setVisible(false);
    verify(mockDoiWidgetV2).configure(mockDoiAssociation);
    verify(mockAnnotationsWidget)
      .configure(bundle, canCertifiedUserEdit, isCurrentVersion);
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
    bundle.setDoiAssociation(mockDoiAssociation);
    widget.configure(bundle, versionNumber, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(false);
    verify(mockFileHistoryWidget).setEntityBundle(bundle, versionNumber);
    verify(mockDoiWidgetV2).configure(mockDoiAssociation);
    verify(mockAnnotationsWidget)
      .configure(bundle, canCertifiedUserEdit, isCurrentVersion);
  }

  @Test
  public void testSetEntityBundle_FetchUnversionedDoiForVersionableEntity() {
    // SWC-5735
    // For certain versionable entities, such as files, we may fetch the versioned entity bundle, which will contain a versioned DOI, if it exists.
    // If no versioned DOI exists, AND this is the current version of the entity, we should show the unversioned DOI, which is not contained in the versioned bundle
    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = true;
    boolean canCertifiedUserEdit = false;
    boolean isCurrentVersion = true;
    when(permissions.getCanChangePermissions())
      .thenReturn(canChangePermissions);
    when(permissions.getCanCertifiedUserEdit())
      .thenReturn(canCertifiedUserEdit);

    Long versionNumber = 5L;
    FileEntity fileEntity = new FileEntity();
    fileEntity.setName(entityName);
    fileEntity.setId(entityId);
    fileEntity.setVersionNumber(versionNumber);
    fileEntity.setIsLatestVersion(isCurrentVersion);
    EntityBundle bundle = new EntityBundle();
    bundle.setEntity(fileEntity);
    bundle.setPermissions(permissions);

    // The bundle did not contain a DOI, which can happen if the version was specified but there is no versioned DOI
    bundle.setDoiAssociation(null);
    // The unversioned DOI exists
    when(mockJsClient.getDoiAssociation(entityId, ObjectType.ENTITY, null))
      .thenReturn(getDoneFuture(mockDoiAssociation));

    widget.configure(bundle, versionNumber, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(false);
    verify(mockFileHistoryWidget).setEntityBundle(bundle, versionNumber);
    verify(mockDoiWidgetV2).configure(mockDoiAssociation); // !
    verify(mockAnnotationsWidget)
      .configure(bundle, canCertifiedUserEdit, isCurrentVersion);
  }

  @Test
  public void testSetEntityBundle_FetchUnversionedDoiForVersionableEntity_NoDoi() {
    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = true;
    boolean canCertifiedUserEdit = false;
    boolean isCurrentVersion = true;
    when(permissions.getCanChangePermissions())
      .thenReturn(canChangePermissions);
    when(permissions.getCanCertifiedUserEdit())
      .thenReturn(canCertifiedUserEdit);

    Long versionNumber = 5L;
    FileEntity fileEntity = new FileEntity();
    fileEntity.setName(entityName);
    fileEntity.setId(entityId);
    fileEntity.setVersionNumber(versionNumber);
    fileEntity.setIsLatestVersion(isCurrentVersion);
    EntityBundle bundle = new EntityBundle();
    bundle.setEntity(fileEntity);
    bundle.setPermissions(permissions);

    // The bundle did not contain a DOI
    bundle.setDoiAssociation(null);
    // No unversioned DOI exists (404 will result in failed future)
    when(mockJsClient.getDoiAssociation(entityId, ObjectType.ENTITY, null))
      .thenReturn(getFailedFuture());

    widget.configure(bundle, versionNumber, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(false);
    verify(mockFileHistoryWidget).setEntityBundle(bundle, versionNumber);
    verify(mockDoiWidgetV2, never()).configure(any()); // !
    verify(mockAnnotationsWidget)
      .configure(bundle, canCertifiedUserEdit, isCurrentVersion);
  }

  @Test
  public void testSetEntityBundle_FetchUnversionedDoiForVersionableEntity_NotLatestVersion() {
    // If this is not the latest version of the entity, then we shouldn't fetch the unversioned DOI (since it doesn't point to this version)
    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = true;
    boolean canCertifiedUserEdit = false;
    boolean isCurrentVersion = false; // !
    when(permissions.getCanChangePermissions())
      .thenReturn(canChangePermissions);
    when(permissions.getCanCertifiedUserEdit())
      .thenReturn(canCertifiedUserEdit);

    Long versionNumber = 5L;
    FileEntity fileEntity = new FileEntity();
    fileEntity.setName(entityName);
    fileEntity.setId(entityId);
    fileEntity.setVersionNumber(versionNumber);
    fileEntity.setIsLatestVersion(isCurrentVersion);
    EntityBundle bundle = new EntityBundle();
    bundle.setEntity(fileEntity);
    bundle.setPermissions(permissions);

    // The bundle did not contain a DOI
    bundle.setDoiAssociation(null);

    widget.configure(bundle, versionNumber, mockActionMenuWidget);

    // Because this isn't the latest version, we should have never requested a DOI
    verify(mockJsClient, never()).getDoiAssociation(any(), any(), any());

    verify(mockView).setDetailedMetadataVisible(false);
    verify(mockFileHistoryWidget).setEntityBundle(bundle, versionNumber);
    verify(mockDoiWidgetV2, never()).configure(any()); // !
    verify(mockAnnotationsWidget)
      .configure(bundle, canCertifiedUserEdit, isCurrentVersion);
  }

  @Test
  public void testConfigureStorageLocationExternalS3() {
    List<UploadDestination> uploadDestinations = new ArrayList<UploadDestination>();
    ExternalS3UploadDestination exS3Destination = new ExternalS3UploadDestination();
    exS3Destination.setBucket("testBucket");
    exS3Destination.setBaseKey("testBaseKey");
    uploadDestinations.add(exS3Destination);
    AsyncMockStubber
      .callSuccessWith(uploadDestinations)
      .when(mockJsClient)
      .getUploadDestinations(anyString(), any(AsyncCallback.class));
    widget.configureStorageLocation(folderEntity);
    verify(mockView).setUploadDestinationText("s3://testBucket/testBaseKey");
    verify(mockView).setUploadDestinationPanelVisible(false);
    verify(mockView).setUploadDestinationPanelVisible(true);
  }

  @Test
  public void testConfigureStorageLocationExternalGoogleCloud() {
    List<UploadDestination> uploadDestinations = new ArrayList<UploadDestination>();
    ExternalGoogleCloudUploadDestination exGCDestination = new ExternalGoogleCloudUploadDestination();
    exGCDestination.setBucket("testBucket");
    exGCDestination.setBaseKey("testBaseKey");
    uploadDestinations.add(exGCDestination);
    AsyncMockStubber
      .callSuccessWith(uploadDestinations)
      .when(mockJsClient)
      .getUploadDestinations(anyString(), any(AsyncCallback.class));
    widget.configureStorageLocation(folderEntity);
    verify(mockView).setUploadDestinationText("gs://testBucket/testBaseKey");
    verify(mockView).setUploadDestinationPanelVisible(false);
    verify(mockView).setUploadDestinationPanelVisible(true);
  }

  @Test
  public void testConfigureStorageLocationExternalSftp() {
    List<UploadDestination> uploadDestinations = new ArrayList<UploadDestination>();
    ExternalUploadDestination exS3Destination = new ExternalUploadDestination();
    exS3Destination.setUrl("sftp://testUrl.com/abcdef");
    exS3Destination.setUploadType(UploadType.SFTP);
    uploadDestinations.add(exS3Destination);
    AsyncMockStubber
      .callSuccessWith(uploadDestinations)
      .when(mockJsClient)
      .getUploadDestinations(anyString(), any(AsyncCallback.class));
    widget.configureStorageLocation(folderEntity);
    verify(mockView).setUploadDestinationText("sftp://testUrl.com");
    verify(mockView).setUploadDestinationPanelVisible(false);
    verify(mockView).setUploadDestinationPanelVisible(true);
  }

  @Test
  public void testConfigureStorageLocationExternal() {
    List<UploadDestination> uploadDestinations = new ArrayList<UploadDestination>();
    ExternalUploadDestination exS3Destination = new ExternalUploadDestination();
    exS3Destination.setUploadType(UploadType.HTTPS);
    exS3Destination.setUrl("testUrl.com");
    uploadDestinations.add(exS3Destination);
    AsyncMockStubber
      .callSuccessWith(uploadDestinations)
      .when(mockJsClient)
      .getUploadDestinations(anyString(), any(AsyncCallback.class));
    widget.configureStorageLocation(folderEntity);
    verify(mockView).setUploadDestinationText("testUrl.com");
    verify(mockView).setUploadDestinationPanelVisible(false);
    verify(mockView).setUploadDestinationPanelVisible(true);
  }

  @Test
  public void testConfigureStorageLocationExternalObjectStore() {
    String endpointUrl = "https://externalobjectstore";
    String bucket = "mybucket";
    List<UploadDestination> uploadDestinations = new ArrayList<UploadDestination>();
    ExternalObjectStoreUploadDestination exS3Destination = new ExternalObjectStoreUploadDestination();
    exS3Destination.setUploadType(UploadType.S3);
    exS3Destination.setEndpointUrl(endpointUrl);
    exS3Destination.setBucket(bucket);
    uploadDestinations.add(exS3Destination);
    AsyncMockStubber
      .callSuccessWith(uploadDestinations)
      .when(mockJsClient)
      .getUploadDestinations(anyString(), any(AsyncCallback.class));
    widget.configureStorageLocation(folderEntity);
    verify(mockView).setUploadDestinationText(endpointUrl + "/" + bucket);
    verify(mockView).setUploadDestinationPanelVisible(false);
    verify(mockView).setUploadDestinationPanelVisible(true);
  }

  @Test
  public void testConfigureStorageLocationSynapseStorage() {
    AsyncMockStubber
      .callSuccessWith(null)
      .when(mockJsClient)
      .getUploadDestinations(anyString(), any(AsyncCallback.class));
    widget.configureStorageLocation(folderEntity);
    verify(mockView).setUploadDestinationText("Synapse Storage");
    verify(mockView).setUploadDestinationPanelVisible(false);
    verify(mockView).setUploadDestinationPanelVisible(true);
  }

  @Test
  public void testConfigureStorageLocationFailure() {
    AsyncMockStubber
      .callFailureWith(new Exception("This is an exception!"))
      .when(mockJsClient)
      .getUploadDestinations(anyString(), any(AsyncCallback.class));
    widget.configureStorageLocation(folderEntity);
    verify(mockJSNI).consoleLog("This is an exception!");
    verify(mockView).setUploadDestinationPanelVisible(false);
    verify(mockView, Mockito.never()).setUploadDestinationPanelVisible(true);
  }

  @Test
  public void testConfigureStorageLocationFile() {
    AsyncMockStubber
      .callSuccessWith(null)
      .when(mockJsClient)
      .getUploadDestinations(anyString(), any(AsyncCallback.class));
    widget.configureStorageLocation(new FileEntity());
    verify(mockView).setUploadDestinationPanelVisible(false);
    verify(mockView, Mockito.never()).setUploadDestinationPanelVisible(true);
  }

  @Test
  public void testSetEntityBundleFolder() {
    UserEntityPermissions permissions = mock(UserEntityPermissions.class);
    boolean canChangePermissions = false;
    boolean canCertifiedUserEdit = true;
    boolean isCurrentVersion = true;
    when(permissions.getCanChangePermissions())
      .thenReturn(canChangePermissions);
    when(permissions.getCanCertifiedUserEdit())
      .thenReturn(canCertifiedUserEdit);
    EntityBundle bundle = new EntityBundle();
    bundle.setEntity(folderEntity);
    folderEntity.setId(entityId);
    bundle.setPermissions(permissions);
    bundle.setDoiAssociation(mockDoiAssociation);
    widget.configure(bundle, null, mockActionMenuWidget);
    verify(mockView).setDetailedMetadataVisible(false);
    verify(mockDoiWidgetV2).configure(mockDoiAssociation);
    verify(mockAnnotationsWidget)
      .configure(bundle, canCertifiedUserEdit, isCurrentVersion);
    verify(mockRestrictionWidgetV2)
      .configure(folderEntity, canChangePermissions);
    verify(mockView, never()).setRestrictionWidgetV2Visible(false);
    verify(mockItemCountWidget).configure(entityId);
  }

  @Test
  public void testSetDescriptionForTable() {
    // We currently only show the description field for Tables
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");

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
    bundle.setDoiAssociation(mockDoiAssociation);

    widget.configure(bundle, null, mockActionMenuWidget);

    verify(mockView).setDetailedMetadataVisible(false);
    verify(mockView).setDescription(description);
    // For now we don't show description in any circumstance. When we do show descriptions, this condition can be flipped
    verify(mockView).setDescriptionVisible(false);
    // verify(mockView).setDescriptionVisible(true);
  }

  @Test
  public void testNoDescriptionForNonTables() {
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");

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
    bundle.setDoiAssociation(mockDoiAssociation);

    widget.configure(bundle, null, mockActionMenuWidget);

    verify(mockView).setDescriptionVisible(false);
  }

  @Test
  public void testNoDescriptionWithoutExperimentalMode() {
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn(null);

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
    bundle.setDoiAssociation(mockDoiAssociation);

    widget.configure(bundle, null, mockActionMenuWidget);

    verify(mockView).setDescriptionVisible(false);
  }

  @Test
  public void testShowAnnotationsNoExperimentalMode() {
    when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
      .thenReturn(null);
    widget.setAnnotationsVisible(true);
    verify(mockView).setAnnotationsVisible(true);
    verify(mockView, never()).setAnnotationsModalVisible(true);
  }

  @Test
  public void testShowAnnotationModalInExperimentalMode() {
    when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
      .thenReturn("true");
    widget.setAnnotationsVisible(true);
    verify(mockView).setAnnotationsModalVisible(true);
    verify(mockView, never()).setAnnotationsVisible(true);
  }
}
