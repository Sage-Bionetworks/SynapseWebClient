package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTab;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.WikiTab;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.i18n.server.testing.MockMessageCatalogContext;
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
	SynapseAlert mockSynapseAlert;
	@Mock
	SynapseClientAsync mockSynapseClientAsync;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	EntityBundle mockProjectEntityBundle;
	@Mock
	EntityUpdatedHandler mockEntityUpdatedHandler;
	@Mock
	Project mockProjectEntity;
	@Mock
	UserEntityPermissions mockPermissions;
	
	@Mock
	CallbackP<Boolean> mockProjectInfoCallback;
	
	FilesTab tab;
	String entityId = "syn7777777";
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		AccessRequirement tou = new TermsOfUseAccessRequirement();
		when(mockProjectEntityBundle.getAccessRequirements()).thenReturn(Collections.singletonList(tou));
		when(mockProjectEntityBundle.getEntity()).thenReturn(mockProjectEntity);
		when(mockProjectEntity.getId()).thenReturn(entityId);
		when(mockProjectEntityBundle.getPermissions()).thenReturn(mockPermissions);
		
		tab = new FilesTab(mockView, mockTab, mockFileTitleBar, mockBasicTitleBar,
				mockBreadcrumb, mockEntityMetadata, mockFilesBrowser, mockPreviewWidget, 
				mockWikiPageWidget, mockSynapseAlert, mockSynapseClientAsync, mockPortalGinInjector);
		tab.setShowProjectInfoCallback(mockProjectInfoCallback);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setFileTitlebar(any(Widget.class));
		verify(mockView).setFolderTitlebar(any(Widget.class));
		verify(mockView).setBreadcrumb(any(Widget.class));
		verify(mockView).setPreview(any(Widget.class));
		verify(mockView).setMetadata(any(Widget.class));
		verify(mockView).setWikiPage(any(Widget.class));
		
		verify(mockFilesBrowser).setEntitySelectedHandler(any(EntitySelectedHandler.class));
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
		
		tab.configure(table, mockProjectEntityBundle, mockEntityUpdatedHandler, version);
		
		verify(mockFileTitleBar).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		verify(mockEntityMetadata).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		verify(mockFilesBrowser).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		
		verify(mockView, times(2)).setFileTitlebarVisible(false);
		verify(mockView, times(2)).setFolderTitlebarVisible(false);
		verify(mockView, times(2)).setPreviewVisible(false);
		verify(mockView, times(2)).setMetadataVisible(false);
		
		verify(mockBreadcrumb).configure(any(EntityPath.class), eq(EntityArea.FILES));
		//show project info
		verify(mockProjectInfoCallback).invoke(true);
		
		verify(mockView).clearActionMenuContainer();
		verify(mockView).setProgrammaticClientsVisible(false);
		verify(mockView).setProvenanceVisible(false);
		verify(mockView).configureModifiedAndCreatedWidget(mockProjectEntity);
		verify(mockView).setWikiPageWidgetVisible(false);
		
		verify(mockView).setFileBrowserVisible(true);
		verify(mockFilesBrowser).configure(entityId, canCertifiedUserAddChild, isCertifiedUser);
		
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockTab).setPlace(captor.capture());
		Synapse place = (Synapse)captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.FILES, place.getArea());
		assertNull(place.getAreaToken());
	}

	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}

}
