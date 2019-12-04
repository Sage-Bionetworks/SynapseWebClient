package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.refresh.EntityRefreshAlert;
import com.google.gwt.place.shared.Place;
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
	PortalGinInjector mockPortalGinInjector;
	@Mock
	EntityBundle mockProjectEntityBundle;
	@Mock
	EntityBundle mockEntityBundle;
	@Mock
	List<FileHandle> mockFileHandles;
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
	Project mockProjectEntity;
	@Mock
	UserEntityPermissions mockPermissions;
	@Mock
	ProvenanceWidget mockProvenanceWidget;
	@Mock
	CallbackP<String> mockEntitySelectedCallback;
	@Mock
	ModifiedCreatedByWidget mockModifiedCreatedBy;
	@Mock
	EntityRefreshAlert mockEntityRefreshAlert;
	@Mock
	DiscussionThreadListWidget mockDiscussionThreadListWidget;
	@Mock
	DiscussionThreadBundle mockBundle;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	@Mock
	SynapseJavascriptClient mockJsClient;

	FilesTab tab;
	String projectEntityId = "syn9";
	String projectName = "proyecto";
	String folderEntityId = "syn1";
	String folderName = "folder 1";
	String fileEntityId = "syn4444";
	String fileName = "filename.txt";
	String entityId = "syn7777777";
	String linkEntityId = "syn333";
	Long linkEntityVersion = 3L;
	String threadId = "987";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(mockProjectEntityBundle.getEntity()).thenReturn(mockProjectEntity);
		when(mockProjectEntity.getId()).thenReturn(projectEntityId);
		when(mockProjectEntity.getName()).thenReturn(projectName);
		when(mockProjectEntityBundle.getPermissions()).thenReturn(mockPermissions);
		when(mockPortalGinInjector.getEntityRefreshAlert()).thenReturn(mockEntityRefreshAlert);
		tab = new FilesTab(mockTab, mockPortalGinInjector);

		when(mockPortalGinInjector.getFilesTabView()).thenReturn(mockView);
		when(mockPortalGinInjector.getFileTitleBar()).thenReturn(mockFileTitleBar);
		when(mockPortalGinInjector.getBasicTitleBar()).thenReturn(mockBasicTitleBar);
		when(mockPortalGinInjector.getBreadcrumb()).thenReturn(mockBreadcrumb);
		when(mockPortalGinInjector.getEntityMetadata()).thenReturn(mockEntityMetadata);
		when(mockPortalGinInjector.getFilesBrowser()).thenReturn(mockFilesBrowser);
		when(mockPortalGinInjector.getPreviewWidget()).thenReturn(mockPreviewWidget);
		when(mockPortalGinInjector.getWikiPageWidget()).thenReturn(mockWikiPageWidget);
		when(mockPortalGinInjector.getStuAlert()).thenReturn(mockSynapseAlert);
		when(mockPortalGinInjector.getGlobalApplicationState()).thenReturn(mockGlobalApplicationState);
		when(mockPortalGinInjector.getModifiedCreatedByWidget()).thenReturn(mockModifiedCreatedBy);
		when(mockPortalGinInjector.getDiscussionThreadListWidget()).thenReturn(mockDiscussionThreadListWidget);
		when(mockPortalGinInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);

		tab.setEntitySelectedCallback(mockEntitySelectedCallback);

		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockEntityBundle.getEntity()).thenReturn(mockFileEntity);
		when(mockFolderEntity.getId()).thenReturn(folderEntityId);
		when(mockFolderEntity.getName()).thenReturn(folderName);
		when(mockFileEntity.getId()).thenReturn(fileEntityId);
		when(mockFileEntity.getName()).thenReturn(fileName);
		when(mockEntityBundle.getPermissions()).thenReturn(mockPermissions);

		when(mockPortalGinInjector.getProvenanceRenderer()).thenReturn(mockProvenanceWidget);
		when(mockLinkEntity.getLinksTo()).thenReturn(mockReference);
		when(mockReference.getTargetId()).thenReturn(linkEntityId);
		when(mockReference.getTargetVersionNumber()).thenReturn(linkEntityVersion);

		when(mockBundle.getProjectId()).thenReturn(projectEntityId);
		when(mockBundle.getId()).thenReturn(threadId);

		tab.lazyInject();
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
		verify(mockBreadcrumb).setLinkClickedHandler(any(CallbackP.class));
		verify(mockView).setDiscussionThreadListWidget(any(Widget.class));
		ArgumentCaptor<CallbackP> captor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockDiscussionThreadListWidget).setThreadIdClickedCallback(captor.capture());
		captor.getValue().invoke(mockBundle);
		verify(mockGlobalApplicationState).getPlaceChanger();
		ArgumentCaptor<Place> placeCaptor = ArgumentCaptor.forClass(Place.class);
		verify(mockPlaceChanger).goTo(placeCaptor.capture());
		assertEquals(placeCaptor.getValue(), TopicUtils.getThreadPlace(projectEntityId, threadId));
	}

	@Test
	public void testSetTabClickedCallback() {
		tab.setTabClickedCallback(mockOnClickCallback);
		verify(mockTab).addTabClickedCallback(mockOnClickCallback);
	}

	@Test
	public void testConfigureUsingProject() {
		// configures using a project if the target entity is anything other than a file or folder.
		Long version = null;

		boolean canCertifiedUserAddChild = true;
		boolean isCertifiedUser = false;
		when(mockPermissions.getCanCertifiedUserAddChild()).thenReturn(canCertifiedUserAddChild);
		when(mockPermissions.getIsCertifiedUser()).thenReturn(isCertifiedUser);

		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockProjectEntityBundle, version, mockActionMenuWidget);

		verify(mockView, times(2)).setFileTitlebarVisible(false);
		verify(mockView, times(2)).setFolderTitlebarVisible(false);
		verify(mockView, times(2)).setPreviewVisible(false);
		verify(mockView, times(2)).setFileFolderUIVisible(false);
		verify(mockView, times(2)).setWikiPageWidgetVisible(false);

		// note: breadcrumbs are not shown on the project level
		// show project info
		verify(mockView, times(2)).clearActionMenuContainer();
		verify(mockView, times(2)).setProvenanceVisible(false);
		verify(mockView).clearRefreshAlert();
		verify(mockModifiedCreatedBy).configure(any(Date.class), anyString(), any(Date.class), anyString());
		verify(mockView).setFileBrowserVisible(true);
		verify(mockFilesBrowser).configure(projectEntityId);
		verify(mockFilesBrowser).setEntityClickedHandler(mockEntitySelectedCallback);
		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab, times(2)).setEntityNameAndPlace(eq(projectName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(projectEntityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.FILES, place.getArea());
		assertNull(place.getAreaToken());

		verify(mockEntityRefreshAlert).configure(anyString());

		verify(mockView, atLeastOnce()).setDiscussionThreadListWidgetVisible(false);
	}

	@Test
	public void testConfigureWithFileNoFileHandles() {
		Long version = 4L;
		when(mockEntityBundle.getEntity()).thenReturn(mockFileEntity);
		tab.configure(mockEntityBundle, version, mockActionMenuWidget);

		verify(mockView, times(2)).setPreviewVisible(false);
		verify(mockView, never()).setPreviewVisible(true);
	}

	@Test
	public void testConfigureWithFileWithFileHandles() {
		Long version = 4L;

		boolean canCertifiedUserAddChild = false;
		boolean isCertifiedUser = true;
		when(mockPermissions.getCanCertifiedUserAddChild()).thenReturn(canCertifiedUserAddChild);
		when(mockPermissions.getIsCertifiedUser()).thenReturn(isCertifiedUser);
		when(mockEntityBundle.getFileHandles()).thenReturn(mockFileHandles);
		when(mockEntityBundle.getEntity()).thenReturn(mockFileEntity);

		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockEntityBundle, version, mockActionMenuWidget);

		verify(mockView).setFileTitlebarVisible(false);
		verify(mockView).setFileTitlebarVisible(true);
		verify(mockView, times(2)).setFolderTitlebarVisible(false);
		verify(mockView).setPreviewVisible(false);
		verify(mockView).setPreviewVisible(true);
		verify(mockView).setFileFolderUIVisible(false);
		verify(mockView).setFileFolderUIVisible(true);
		verify(mockView).setWikiPageWidgetVisible(false);
		verify(mockView).setWikiPageWidgetVisible(true);

		verify(mockFileTitleBar).configure(mockEntityBundle);
		verify(mockPreviewWidget).configure(mockEntityBundle);

		verify(mockEntityMetadata).configure(mockEntityBundle, version, mockActionMenuWidget);

		verify(mockBreadcrumb).configure(any(EntityPath.class), eq(EntityArea.FILES));

		verify(mockView, times(2)).clearActionMenuContainer();
		verify(mockView).setProvenanceVisible(true);
		verify(mockModifiedCreatedBy).configure(any(Date.class), anyString(), any(Date.class), anyString());
		verify(mockView).setWikiPageWidgetVisible(true);

		verify(mockView, times(2)).setFileBrowserVisible(false);
		verify(mockPortalGinInjector).getProvenanceRenderer();

		verify(mockView).setRefreshAlert(any(Widget.class));
		verify(mockView).setDiscussionText(fileName);
		verify(mockEntityRefreshAlert).configure(fileEntityId);

		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(fileName), captor.capture());
		Synapse place = (Synapse) captor.getValue();
		assertEquals(fileEntityId, place.getEntityId());
		assertEquals(version, place.getVersionNumber());
		assertNull(place.getArea());
		assertNull(place.getAreaToken());

		verify(mockDiscussionThreadListWidget).configure(fileEntityId, null, null);
		verify(mockView).setDiscussionThreadListWidgetVisible(true);
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
		tab.configure(mockEntityBundle, version, mockActionMenuWidget);

		verify(mockView, times(2)).setFileTitlebarVisible(false);
		verify(mockView).setFolderTitlebarVisible(false);
		verify(mockView).setFolderTitlebarVisible(true);
		verify(mockView, times(2)).setPreviewVisible(false);
		verify(mockView).setFileFolderUIVisible(false);
		verify(mockView).setFileFolderUIVisible(true);
		verify(mockView).setWikiPageWidgetVisible(false);
		verify(mockView).setWikiPageWidgetVisible(true);

		verify(mockView).setRefreshAlert(any(Widget.class));
		verify(mockEntityRefreshAlert).configure(folderEntityId);

		verify(mockBasicTitleBar).configure(mockEntityBundle);

		verify(mockEntityMetadata).configure(mockEntityBundle, version, mockActionMenuWidget);


		verify(mockBreadcrumb).configure(any(EntityPath.class), eq(EntityArea.FILES));

		verify(mockView, times(2)).clearActionMenuContainer();
		verify(mockView, times(2)).setProvenanceVisible(false);
		verify(mockModifiedCreatedBy).configure(any(Date.class), anyString(), any(Date.class), anyString());
		verify(mockView).setWikiPageWidgetVisible(true);

		verify(mockView).setFileBrowserVisible(true);
		verify(mockFilesBrowser).configure(folderEntityId);

		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(folderName), captor.capture());
		Synapse place = (Synapse) captor.getValue();
		assertEquals(folderEntityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertNull(place.getArea());
		assertNull(place.getAreaToken());

		verify(mockView, atLeastOnce()).setDiscussionThreadListWidgetVisible(false);
	}

	@Test
	public void testGetLinkBundleAndDisplay() {
		when(mockEntityBundle.getEntity()).thenReturn(mockLinkEntity);
		tab.setTargetBundle(mockEntityBundle, null);
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockPlaceChanger).goTo(captor.capture());
		Synapse place = (Synapse) captor.getValue();
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
		verify(mockView).setFileFolderUIVisible(false);
		verify(mockView).setWikiPageWidgetVisible(false);
		verify(mockView).setFileBrowserVisible(false);
		verify(mockView).clearActionMenuContainer();
		verify(mockBreadcrumb).clear();
		verify(mockView).setProvenanceVisible(false);
		verify(mockModifiedCreatedBy).setVisible(false);
		verify(mockView).setDiscussionThreadListWidgetVisible(false);
		verify(mockView).clearRefreshAlert();
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
