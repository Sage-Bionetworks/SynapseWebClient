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
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.WikiTab;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class TablesTabTest {
	@Mock
	Tab mockTab;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	TablesTabView mockView;
	@Mock
	TableListWidget mockTableListWidget;
	@Mock
	BasicTitleBar mockBasicTitleBar;
	@Mock
	Breadcrumb mockBreadcrumb;
	@Mock
	EntityMetadata mockEntityMetadata;
	@Mock
	QueryTokenProvider mockQueryTokenProvider;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	SynapseClientAsync mockSynapseClientAsync;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	
	@Mock
	EntityBundle mockProjectEntityBundle;
	@Mock
	EntityBundle mockTableEntityBundle;
	@Mock
	TableEntity mockTableEntity;
	@Mock
	Project mockProjectEntity;
	@Mock
	UserEntityPermissions mockPermissions;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	@Mock
	EntityActionController mockEntityActionController;
	@Mock
	EntityUpdatedHandler mockEntityUpdatedHandler;
	@Mock
	CallbackP<Boolean> mockProjectInfoCallback;
	@Mock
	TableEntityWidget mockTableEntityWidget;
	
	String projectEntityId = "syn666666";
	String tableEntityId = "syn22";
	
	TablesTab tab;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new TablesTab(mockView, mockTab, mockTableListWidget, mockBasicTitleBar, 
				mockBreadcrumb, mockEntityMetadata, mockQueryTokenProvider, mockSynapseAlert, mockSynapseClientAsync,
				mockPortalGinInjector);
		tab.setShowProjectInfoCallback(mockProjectInfoCallback);
		AccessRequirement tou = new TermsOfUseAccessRequirement();
		when(mockProjectEntityBundle.getAccessRequirements()).thenReturn(Collections.singletonList(tou));
		when(mockProjectEntityBundle.getEntity()).thenReturn(mockProjectEntity);
		when(mockProjectEntity.getId()).thenReturn(projectEntityId);
		when(mockProjectEntityBundle.getPermissions()).thenReturn(mockPermissions);
		
		when(mockPortalGinInjector.createActionMenuWidget()).thenReturn(mockActionMenuWidget);
		when(mockPortalGinInjector.createEntityActionController()).thenReturn(mockEntityActionController);
		when(mockPortalGinInjector.createNewTableEntityWidget()).thenReturn(mockTableEntityWidget);
		
		when(mockTableEntityBundle.getAccessRequirements()).thenReturn(Collections.singletonList(tou));
		when(mockTableEntityBundle.getEntity()).thenReturn(mockTableEntity);
		when(mockTableEntity.getId()).thenReturn(tableEntityId);
		when(mockTableEntityBundle.getPermissions()).thenReturn(mockPermissions);
		
		AsyncMockStubber.callSuccessWith(mockTableEntityBundle).when(mockSynapseClientAsync).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testSetTabClickedCallback() {
		tab.setTabClickedCallback(mockOnClickCallback);
		verify(mockTab).addTabClickedCallback(mockOnClickCallback);
	}

	@Test
	public void testConfigureUsingTable() {
		String areaToken = null;
		
		boolean canCertifiedUserEdit = true;
		boolean isCertifiedUser = false;
		when(mockPermissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		when(mockPermissions.getIsCertifiedUser()).thenReturn(isCertifiedUser);
		
		tab.configure(mockTableEntity, mockProjectEntityBundle, mockEntityUpdatedHandler, areaToken);
		
		verify(mockSynapseClientAsync).getEntityBundle(eq(tableEntityId), anyInt(), any(AsyncCallback.class));
		verify(mockBreadcrumb).configure(any(EntityPath.class), eq(EntityArea.TABLES));
		verify(mockPortalGinInjector).createActionMenuWidget();
		verify(mockPortalGinInjector).createEntityActionController();
		verify(mockBasicTitleBar).configure(mockTableEntityBundle);
		verify(mockEntityMetadata).setEntityBundle(mockTableEntityBundle, null);
		verify(mockTableEntityWidget).configure(eq(mockTableEntityBundle), eq(canCertifiedUserEdit), any(QueryChangeHandler.class), eq(mockActionMenuWidget));
		verify(mockView).setTableEntityWidget(any(Widget.class));
		
		verify(mockEntityMetadata).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		
		verify(mockView).setEntityMetadataVisible(true);
		verify(mockView).setBreadcrumbVisible(true);
		verify(mockView).setTableListVisible(false);
		verify(mockView).setTitlebarVisible(true);
		verify(mockView).clearActionMenuContainer();
		verify(mockView).clearTableEntityWidget();
		verify(mockView).clearModifiedAndCreatedWidget();
		
		//hide project info
		verify(mockProjectInfoCallback).invoke(false);
		
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockTab).setPlace(captor.capture());
		Synapse place = (Synapse)captor.getValue();
		assertEquals(tableEntityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertNull(place.getArea());
		assertNull(place.getAreaToken());
	}

	
	@Test
	public void testConfigureUsingProject() {
		//configures using a project if the target entity is anything other than a table
		FileEntity file = mock(FileEntity.class);

		String areaToken = null;
		
		boolean canCertifiedUserAddChild = true;
		boolean isCertifiedUser = false;
		when(mockPermissions.getCanCertifiedUserAddChild()).thenReturn(canCertifiedUserAddChild);
		when(mockPermissions.getIsCertifiedUser()).thenReturn(isCertifiedUser);
		
		tab.configure(file, mockProjectEntityBundle, mockEntityUpdatedHandler, areaToken);
		
		verify(mockEntityMetadata).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		
		verify(mockView).setEntityMetadataVisible(false);
		verify(mockView).setBreadcrumbVisible(false);
		verify(mockView).setTableListVisible(true);
		verify(mockView).setTitlebarVisible(false);
		verify(mockView).clearActionMenuContainer();
		verify(mockView).clearTableEntityWidget();
		verify(mockView).clearModifiedAndCreatedWidget();
		
		//show project info
		verify(mockProjectInfoCallback).invoke(true);
		
		verify(mockTableListWidget).configure(mockProjectEntityBundle);
		
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockTab).setPlace(captor.capture());
		Synapse place = (Synapse)captor.getValue();
		assertEquals(projectEntityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.TABLES, place.getArea());
		assertNull(place.getAreaToken());
	}
	
	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}

}
