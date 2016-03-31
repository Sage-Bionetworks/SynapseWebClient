package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlert;
import org.sagebionetworks.web.shared.EntityBundlePlus;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class FilesTabTest {
	@Mock
	Tab mockTab;
	@Mock
	FilesTabView mockView;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	FileTitleBar mockFileTitleBar;
	@Mock
	BasicTitleBar mockBasicTitleBar;
	@Mock
	Breadcrumb mockBreadcrumb;
	@Mock
	EntityMetadata mockEntityMetadata;
	@Mock
	FilesBrowser mockFilesBrowser;
	@Mock
	PreviewWidget mockPreviewWidget;
	@Mock
	WikiPageWidget mockWikiPageWidget;
	@Mock
	StuAlert mockSynapseAlert;
	@Mock
	SynapseClientAsync mockSynapseClientAsync;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	EntityBundle mockProjectEntityBundle;
	@Mock
	EntityBundle mockEntityBundle;
	@Mock
	EntityBundlePlus mockEntityBundlePlus;
	@Mock
	FileEntity mockFileEntity;
	@Mock
	Folder mockFolderEntity;
	@Mock
	Link mockLinkEntity;
	@Mock
	Reference mockReference;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	EntityUpdatedHandler mockEntityUpdatedHandler;
	@Mock
	Project mockProjectEntity;
	@Mock
	UserEntityPermissions mockPermissions;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	@Mock
	EntityActionController mockEntityActionController;
	@Mock
	ProvenanceWidget mockProvenanceWidget;
	@Mock
	CallbackP<Boolean> mockProjectInfoCallback;
	@Mock
	ModifiedCreatedByWidget mockModifiedCreatedBy;
	@Mock
	RefreshAlert mockRefreshAlert;
	FilesTab tab;
	String projectEntityId = "syn9";
	String projectName = "proyecto";
	String folderEntityId = "syn1";
	String folderName = "folder 1";
	String fileEntityId = "syn4444";
	String fileName = "filename.txt";
	String entityId = "syn7777777";
	String linkEntityId = "syn333";
	Long linkEntityVersion=3L;
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		AccessRequirement tou = new TermsOfUseAccessRequirement();
		when(mockProjectEntityBundle.getAccessRequirements()).thenReturn(Collections.singletonList(tou));
		when(mockProjectEntityBundle.getEntity()).thenReturn(mockProjectEntity);
		when(mockProjectEntity.getId()).thenReturn(entityId);
		when(mockProjectEntity.getName()).thenReturn(projectName);
		when(mockProjectEntityBundle.getPermissions()).thenReturn(mockPermissions);
		
		tab = new FilesTab(mockView, mockTab, mockFileTitleBar, mockBasicTitleBar,
				mockBreadcrumb, mockEntityMetadata, mockFilesBrowser, mockPreviewWidget, 
				mockWikiPageWidget, mockSynapseAlert, mockSynapseClientAsync, mockPortalGinInjector,mockGlobalApplicationState, mockModifiedCreatedBy,
				mockRefreshAlert);
		tab.setShowProjectInfoCallback(mockProjectInfoCallback);
		
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockEntityBundle.getAccessRequirements()).thenReturn(Collections.singletonList(tou));
		when(mockEntityBundle.getEntity()).thenReturn(mockFileEntity);
		when(mockFolderEntity.getId()).thenReturn(folderEntityId);
		when(mockFolderEntity.getName()).thenReturn(folderName);
		when(mockFileEntity.getId()).thenReturn(fileEntityId);
		when(mockFileEntity.getName()).thenReturn(fileName);
		when(mockEntityBundle.getPermissions()).thenReturn(mockPermissions);
		when(mockEntityBundlePlus.getEntityBundle()).thenReturn(mockEntityBundle);
		when(mockEntityBundlePlus.getLatestVersionNumber()).thenReturn(null);
		
		AsyncMockStubber.callSuccessWith(mockEntityBundle).when(mockSynapseClientAsync).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockEntityBundlePlus).when(mockSynapseClientAsync).getEntityBundlePlusForVersion(anyString(), anyLong(), anyInt(), any(AsyncCallback.class));
		
		when(mockPortalGinInjector.createActionMenuWidget()).thenReturn(mockActionMenuWidget);
		when(mockPortalGinInjector.createEntityActionController()).thenReturn(mockEntityActionController);
		when(mockPortalGinInjector.getProvenanceRenderer()).thenReturn(mockProvenanceWidget);
		when(mockLinkEntity.getLinksTo()).thenReturn(mockReference);
		when(mockReference.getTargetId()).thenReturn(linkEntityId);
		when(mockReference.getTargetVersionNumber()).thenReturn(linkEntityVersion);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setFileTitlebar(any(Widget.class));
		verify(mockView).setFolderTitlebar(any(Widget.class));
		verify(mockView).setBreadcrumb(any(Widget.class));
		verify(mockView).setPreview(any(Widget.class));
		verify(mockView).setMetadata(any(Widget.class));
		verify(mockView).setWikiPage(any(Widget.class));
		verify(mockView).setSynapseAlert(any(Widget.class));
		verify(mockView).setRefreshAlert(any(Widget.class));
		verify(mockFilesBrowser).setEntityClickedHandler(any(CallbackP.class));
		verify(mockBreadcrumb).setLinkClickedHandler(any(CallbackP.class));
	}
	
	@Test
	public void testSetTabClickedCallback() {
		tab.setTabClickedCallback(mockOnClickCallback);
		verify(mockTab).addTabClickedCallback(mockOnClickCallback);
	}

	@Test
	public void testConfigureUsingProject() {
		//configures using a project if the target entity is anything other than a file or folder.
		TableEntity table = mock(TableEntity.class);
		Long version = null;
		
		boolean canCertifiedUserAddChild = true;
		boolean isCertifiedUser = false;
		when(mockPermissions.getCanCertifiedUserAddChild()).thenReturn(canCertifiedUserAddChild);
		when(mockPermissions.getIsCertifiedUser()).thenReturn(isCertifiedUser);
		
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(table, mockEntityUpdatedHandler, version);
		
		verify(mockFileTitleBar).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		verify(mockEntityMetadata).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		verify(mockFilesBrowser).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		
		verify(mockView, times(2)).setFileTitlebarVisible(false);
		verify(mockView, times(2)).setFolderTitlebarVisible(false);
		verify(mockView, times(2)).setPreviewVisible(false);
		verify(mockView, times(2)).setMetadataVisible(false);
		verify(mockView, times(2)).setWikiPageWidgetVisible(false);
		
		verify(mockBreadcrumb).configure(any(EntityPath.class), eq(EntityArea.FILES));
		//show project info
		verify(mockProjectInfoCallback).invoke(true);
		
		verify(mockView, times(2)).clearActionMenuContainer();
		verify(mockView, times(2)).setProgrammaticClientsVisible(false);
		verify(mockView, times(2)).setProvenanceVisible(false);
		verify(mockModifiedCreatedBy).configure(any(Date.class), anyString(), any(Date.class), anyString());
		verify(mockView).setFileBrowserVisible(true);
		verify(mockFilesBrowser).configure(entityId, canCertifiedUserAddChild, isCertifiedUser);
		
		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab, times(2)).setEntityNameAndPlace(eq(projectName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(projectEntityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.FILES, place.getArea());
		assertNull(place.getAreaToken());
		
		verify(mockRefreshAlert).clear();
		verify(mockRefreshAlert, never()).configure(anyString(), any(ObjectType.class));
	}
	
	@Test
	public void testConfigureWithFile() {
		Long version = 4L;
		
		boolean canCertifiedUserAddChild = false;
		boolean isCertifiedUser = true;
		when(mockPermissions.getCanCertifiedUserAddChild()).thenReturn(canCertifiedUserAddChild);
		when(mockPermissions.getIsCertifiedUser()).thenReturn(isCertifiedUser);
		
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockFileEntity, mockEntityUpdatedHandler, version);
		
		verify(mockSynapseClientAsync).getEntityBundlePlusForVersion(eq(fileEntityId), eq(version), anyInt(), any(AsyncCallback.class));
		verify(mockFileTitleBar).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		verify(mockEntityMetadata).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		verify(mockFilesBrowser).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		
		verify(mockView).setFileTitlebarVisible(false);
		verify(mockView).setFileTitlebarVisible(true);
		verify(mockView, times(2)).setFolderTitlebarVisible(false);
		verify(mockView).setPreviewVisible(false);
		verify(mockView).setPreviewVisible(true);
		verify(mockView).setMetadataVisible(false);
		verify(mockView).setMetadataVisible(true);
		verify(mockView).setWikiPageWidgetVisible(false);
		verify(mockView).setWikiPageWidgetVisible(true);
		
		verify(mockFileTitleBar).configure(mockEntityBundle);
		verify(mockPreviewWidget).configure(mockEntityBundle);
		
		verify(mockEntityMetadata).setEntityBundle(mockEntityBundle, version);
		//show file history since we are asking for a specific version
		verify(mockEntityMetadata).setFileHistoryVisible(true);
		
		
		verify(mockBreadcrumb).configure(any(EntityPath.class), eq(EntityArea.FILES));
		//hide project info
		verify(mockProjectInfoCallback).invoke(false);
		
		verify(mockView, times(2)).clearActionMenuContainer();
		verify(mockView).setProgrammaticClientsVisible(true);
		verify(mockView).configureProgrammaticClients(fileEntityId, version);
		verify(mockView).setProvenanceVisible(true);
		verify(mockModifiedCreatedBy).configure(any(Date.class), anyString(), any(Date.class), anyString());
		verify(mockView).setWikiPageWidgetVisible(true);
		
		verify(mockView, times(2)).setFileBrowserVisible(false);
		verify(mockPortalGinInjector).createActionMenuWidget();
		verify(mockPortalGinInjector).createEntityActionController();
		verify(mockPortalGinInjector).getProvenanceRenderer();

		verify(mockRefreshAlert).clear();
		verify(mockRefreshAlert).configure(fileEntityId, ObjectType.ENTITY);

		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab, times(2)).setEntityNameAndPlace(eq(fileName), captor.capture());
		Synapse place = (Synapse)captor.getValue();
		assertEquals(fileEntityId, place.getEntityId());
		assertEquals(version, place.getVersionNumber());
		assertNull(place.getArea());
		assertNull(place.getAreaToken());
		
		//quick test to verify that reconfiguring with the same entity/version should cause nothing to change (certainly should not ask for the entity bundle again).
		reset(mockSynapseClientAsync);
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockFileEntity, mockEntityUpdatedHandler, version);
		
		verifyZeroInteractions(mockSynapseClientAsync);
	}
	

	@Test
	public void testConfigureWithFolder() {
		when(mockEntityBundle.getEntity()).thenReturn(mockFolderEntity);
		Long version = null;
		
		boolean canCertifiedUserAddChild = true;
		boolean isCertifiedUser = true;
		when(mockPermissions.getCanCertifiedUserAddChild()).thenReturn(canCertifiedUserAddChild);
		when(mockPermissions.getIsCertifiedUser()).thenReturn(isCertifiedUser);
		
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockFolderEntity, mockEntityUpdatedHandler, version);
		
		verify(mockSynapseClientAsync).getEntityBundlePlusForVersion(eq(folderEntityId), anyLong(), anyInt(), any(AsyncCallback.class));
		verify(mockFileTitleBar).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		verify(mockEntityMetadata).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		verify(mockFilesBrowser).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		
		verify(mockView, times(2)).setFileTitlebarVisible(false);
		verify(mockView).setFolderTitlebarVisible(false);
		verify(mockView).setFolderTitlebarVisible(true);
		verify(mockView, times(2)).setPreviewVisible(false);
		verify(mockView).setMetadataVisible(false);
		verify(mockView).setMetadataVisible(true);
		verify(mockView).setWikiPageWidgetVisible(false);
		verify(mockView).setWikiPageWidgetVisible(true);

		verify(mockRefreshAlert).clear();
		verify(mockRefreshAlert).configure(folderEntityId, ObjectType.ENTITY);

		verify(mockBasicTitleBar).configure(mockEntityBundle);
		
		verify(mockEntityMetadata).setEntityBundle(mockEntityBundle, version);
		//show file history since we are asking for a specific version
		verify(mockEntityMetadata).setFileHistoryVisible(false);
		
		
		verify(mockBreadcrumb).configure(any(EntityPath.class), eq(EntityArea.FILES));
		//hide project info
		verify(mockProjectInfoCallback).invoke(false);
		
		verify(mockView, times(2)).clearActionMenuContainer();
		verify(mockView, times(2)).setProgrammaticClientsVisible(false);
		verify(mockView, times(2)).setProvenanceVisible(false);
		verify(mockModifiedCreatedBy).configure(any(Date.class), anyString(), any(Date.class), anyString());
		verify(mockView).setWikiPageWidgetVisible(true);
		
		verify(mockView).setFileBrowserVisible(true);
		verify(mockFilesBrowser).configure(folderEntityId, canCertifiedUserAddChild, isCertifiedUser);
		verify(mockPortalGinInjector).createActionMenuWidget();
		verify(mockPortalGinInjector).createEntityActionController();
		
		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab, times(2)).setEntityNameAndPlace(eq(folderName), captor.capture());
		Synapse place = (Synapse)captor.getValue();
		assertEquals(folderEntityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertNull(place.getArea());
		assertNull(place.getAreaToken());
	}

	@Test
	public void testGetLinkBundleAndDisplay() {
		when(mockEntityBundle.getEntity()).thenReturn(mockLinkEntity);
		tab.getTargetBundleAndDisplay("syn303033", null);
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockPlaceChanger).goTo(captor.capture());
		Synapse place = (Synapse)captor.getValue();
		assertEquals(linkEntityId, place.getEntityId());
		assertEquals(linkEntityVersion, place.getVersionNumber());
		assertNull(place.getArea());
		assertNull(place.getAreaToken());
	}

	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}
	
	@Test
	public void testResetView() {
		tab.resetView();
		verify(mockSynapseAlert).clear();
		verify(mockView).setFileTitlebarVisible(false);
		verify(mockView).setFolderTitlebarVisible(false);
		verify(mockView).setPreviewVisible(false);
		verify(mockView).setMetadataVisible(false);
		verify(mockView).setWikiPageWidgetVisible(false);
		verify(mockView).setFileBrowserVisible(false);
		verify(mockView).clearActionMenuContainer();
		verify(mockBreadcrumb).clear();
		verify(mockView).setProgrammaticClientsVisible(false);
		verify(mockView).setProvenanceVisible(false);
		verify(mockRefreshAlert).clear();
		verify(mockModifiedCreatedBy).setVisible(false);
	}
	
	@Test
	public void testShowProjectLoadError() {
		Exception projectLoadError = new Exception("error loading project");
		tab.setProject(projectEntityId, null, projectLoadError);
		tab.showProjectLevelUI();
		Synapse expectedPlace = new Synapse(projectEntityId, null, EntityArea.FILES, null);
		verify(mockTab).setEntityNameAndPlace(projectEntityId, expectedPlace);
		verify(mockSynapseAlert).handleException(projectLoadError);
	}
}
