package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.v2.DoiAssociation;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreUploadDestination;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.ExternalUploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2;
import org.sagebionetworks.web.client.widget.entity.DoiWidget;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView;
import org.sagebionetworks.web.client.widget.entity.FileHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class EntityMetadataTest {
	@Mock
	EntityMetadataView mockView;
	@Mock
	PortalGinInjector mockInjector;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	AnnotationsRendererWidget mockAnnotationsWidget;
	@Mock
	DoiWidget mockDoiWidget;
	@Mock
	DoiWidgetV2 mockDoiWidgetV2;
	@Mock
	RestrictionWidget mockRestrictionWidgetV2;
	@Mock
	FileHistoryWidget mockFileHistoryWidget;
	@Mock
	Doi mockDoi;
	@Mock
	DoiAssociation mockDoiAssociation;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	SynapseJSNIUtils mockJSNI;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	@Mock
	CookieProvider mockCookies;
	String entityId = "syn123";
	String entityName = "testEntity";
	Entity en = new Folder();
	EntityMetadata widget;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new EntityMetadata(mockView, mockDoiWidget, mockDoiWidgetV2, mockAnnotationsWidget,
				mockFileHistoryWidget, mockJsClient, mockJSNI, mockRestrictionWidgetV2, mockCookies);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setDoiWidget(any(IsWidget.class));
		verify(mockView).setAnnotationsRendererWidget(any(IsWidget.class));
		verify(mockView).setFileHistoryWidget(any(IsWidget.class));
		verify(mockView).setRestrictionWidgetV2(any(IsWidget.class));
		verify(mockRestrictionWidgetV2).setShowChangeLink(true);
		verify(mockRestrictionWidgetV2).setShowIfProject(false);
		verify(mockRestrictionWidgetV2).setShowFlagLink(true);
		verify(mockView).setRestrictionWidgetV2Visible(true);
	}
	
	@Test
	public void testSetEntityBundleProject() {
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn(null);
		UserEntityPermissions permissions = mock(UserEntityPermissions.class);
		boolean canChangePermissions = false;
		boolean canCertifiedUserEdit = true;
		boolean isCurrentVersion = true;
		when(permissions.getCanChangePermissions()).thenReturn(canChangePermissions);
		when(permissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		Project project = new Project();
		project.setName(entityName);
		project.setId(entityId);
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(project);
		bundle.setPermissions(permissions);
		bundle.setDoi(mockDoi);
		bundle.setDoiAssociation(mockDoiAssociation);
		en.setId(entityId);
		widget.configure(bundle, null, mockActionMenuWidget);
		verify(mockView).setRestrictionPanelVisible(false);
		verify(mockDoiWidget).configure(mockDoi, entityId); // Remove when replaced
		verify(mockDoiWidgetV2, never()).configure(mockDoiAssociation); // Remove when out of alpha mode
		verify(mockAnnotationsWidget).configure(bundle, canCertifiedUserEdit, isCurrentVersion);
		verify(mockRestrictionWidgetV2).configure(project, canChangePermissions);
		verify(mockView, never()).setRestrictionWidgetV2Visible(false);
	}

	@Test
	public void testSetEntityBundleProjectInAlphaMode() {
		// Use this test to verify alpha mode behavior
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		UserEntityPermissions permissions = mock(UserEntityPermissions.class);
		boolean canChangePermissions = false;
		boolean canCertifiedUserEdit = true;
		boolean isCurrentVersion = true;
		when(permissions.getCanChangePermissions()).thenReturn(canChangePermissions);
		when(permissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		Project project = new Project();
		project.setName(entityName);
		project.setId(entityId);
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(project);
		bundle.setPermissions(permissions);
		bundle.setDoi(mockDoi);
		bundle.setDoiAssociation(mockDoiAssociation);
		en.setId(entityId);
		widget.configure(bundle, null, mockActionMenuWidget);
		verify(mockView).setRestrictionPanelVisible(false);
		verify(mockDoiWidget, never()).configure(mockDoi, entityId); // Remove line when widget is removed
		verify(mockDoiWidgetV2).configure(mockDoiAssociation); // This is currently in alpha mode
		verify(mockAnnotationsWidget).configure(bundle, canCertifiedUserEdit, isCurrentVersion);
		verify(mockRestrictionWidgetV2).configure(project, canChangePermissions);
		verify(mockView, never()).setRestrictionWidgetV2Visible(false);
	}


	@Test
	public void testSetEntityBundleDockerRepo() {
		UserEntityPermissions permissions = mock(UserEntityPermissions.class);
		boolean canChangePermissions = true;
		boolean canCertifiedUserEdit = false;
		boolean isCurrentVersion = true;
		when(permissions.getCanChangePermissions()).thenReturn(canChangePermissions);
		when(permissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		DockerRepository dockerRepo = new DockerRepository();
		dockerRepo.setName(entityName);
		dockerRepo.setId(entityId);
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(dockerRepo);
		bundle.setPermissions(permissions);
		bundle.setDoi(mockDoi);
		bundle.setDoiAssociation(mockDoiAssociation);
		Long versionNumber = null;
		widget.configure(bundle, versionNumber, mockActionMenuWidget);
		verify(mockFileHistoryWidget, never()).setEntityBundle(bundle, versionNumber);
		verify(mockDoiWidget).configure(mockDoi, entityId);
		verify(mockAnnotationsWidget).configure(bundle, canCertifiedUserEdit, isCurrentVersion);
	}
	
	@Test
	public void testSetEntityBundleFileEntityMostRecent() {
		UserEntityPermissions permissions = mock(UserEntityPermissions.class);
		boolean canChangePermissions = true;
		boolean canCertifiedUserEdit = false;
		boolean isCurrentVersion = true;
		when(permissions.getCanChangePermissions()).thenReturn(canChangePermissions);
		when(permissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		FileEntity fileEntity = new FileEntity();
		fileEntity.setName(entityName);
		fileEntity.setId(entityId);
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(fileEntity);
		bundle.setPermissions(permissions);
		bundle.setDoi(mockDoi);
		bundle.setDoiAssociation(mockDoiAssociation);
		Long versionNumber = null;
		widget.configure(bundle, versionNumber, mockActionMenuWidget);
		verify(mockFileHistoryWidget).setEntityBundle(bundle, versionNumber);
		verify(mockDoiWidget).configure(mockDoi, entityId);
		verify(mockAnnotationsWidget).configure(bundle, canCertifiedUserEdit, isCurrentVersion);
	}
	
	@Test
	public void testSetEntityBundleFileEntityNotMostRecentVersion() {
		UserEntityPermissions permissions = mock(UserEntityPermissions.class);
		boolean canChangePermissions = true;
		boolean canCertifiedUserEdit = false;
		boolean isCurrentVersion = false;
		when(permissions.getCanChangePermissions()).thenReturn(canChangePermissions);
		when(permissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		Long versionNumber = -122L;
		FileEntity fileEntity = new FileEntity();
		fileEntity.setName(entityName);
		fileEntity.setId(entityId);
		fileEntity.setVersionNumber(versionNumber);
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(fileEntity);
		bundle.setPermissions(permissions);
		bundle.setDoi(mockDoi);
		bundle.setDoiAssociation(mockDoiAssociation);
		widget.configure(bundle, versionNumber, mockActionMenuWidget);
		verify(mockFileHistoryWidget).setEntityBundle(bundle, versionNumber);
		verify(mockDoiWidget).configure(mockDoi, entityId);
		verify(mockAnnotationsWidget).configure(bundle, canCertifiedUserEdit, isCurrentVersion);
	}
	
	@Test
	public void testSetDetailedMetadataVisible() {
		widget.setVisible(true);
		verify(mockView).setDetailedMetadataVisible(true);
		widget.setVisible(false);
		verify(mockView).setDetailedMetadataVisible(false);
	}
	
	@Test
	public void testConfigureStorageLocationExternalS3() {
		List<UploadDestination> uploadDestinations = new ArrayList<UploadDestination>();
		ExternalS3UploadDestination exS3Destination = new ExternalS3UploadDestination();
		exS3Destination.setBucket("testBucket");
		exS3Destination.setBaseKey("testBaseKey");
		uploadDestinations.add(exS3Destination);
		AsyncMockStubber.callSuccessWith(uploadDestinations).when(mockJsClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		widget.configureStorageLocation(en);
		verify(mockView).setUploadDestinationText("s3://testBucket/testBaseKey");
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
		AsyncMockStubber.callSuccessWith(uploadDestinations).when(mockJsClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		widget.configureStorageLocation(en);
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
		AsyncMockStubber.callSuccessWith(uploadDestinations).when(mockJsClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		widget.configureStorageLocation(en);
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
		AsyncMockStubber.callSuccessWith(uploadDestinations).when(mockJsClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		widget.configureStorageLocation(en);
		verify(mockView).setUploadDestinationText(endpointUrl + "/" + bucket);
		verify(mockView).setUploadDestinationPanelVisible(false);
		verify(mockView).setUploadDestinationPanelVisible(true);
	}
	
	@Test
	public void testConfigureStorageLocationSynapseStorage() {
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		widget.configureStorageLocation(en);
		verify(mockView).setUploadDestinationText("Synapse Storage");
		verify(mockView).setUploadDestinationPanelVisible(false);
		verify(mockView).setUploadDestinationPanelVisible(true);
	}
	
	@Test
	public void testConfigureStorageLocationFailure() {
		AsyncMockStubber.callFailureWith(new Exception("This is an exception!")).when(mockJsClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		widget.configureStorageLocation(en);
		verify(mockJSNI).consoleLog("This is an exception!");
		verify(mockView).setUploadDestinationPanelVisible(false);
		verify(mockView, Mockito.never()).setUploadDestinationPanelVisible(true);
	}
	
	@Test
	public void testConfigureStorageLocationFile() {
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		widget.configureStorageLocation(new FileEntity());
		verify(mockView).setUploadDestinationPanelVisible(false);
		verify(mockView, Mockito.never()).setUploadDestinationPanelVisible(true);
	}
	
}