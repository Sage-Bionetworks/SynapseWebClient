package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.table.FileView;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTabView;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
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
	StuAlert mockSynapseAlert;
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
	FileView mockFileViewEntity;
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
	@Mock
	ModifiedCreatedByWidget mockModifiedCreatedBy;
	
	String projectEntityId = "syn666666";
	String projectName = "a test project";
	String tableEntityId = "syn22";
	String tableName = "test table";
	QueryTokenProvider queryTokenProvider;
	
	TablesTab tab;
	Query query;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		queryTokenProvider = new QueryTokenProvider(new AdapterFactoryImpl());
		tab = new TablesTab(mockView, mockTab, mockTableListWidget, mockBasicTitleBar, 
				mockBreadcrumb, mockEntityMetadata, queryTokenProvider, mockSynapseAlert, mockSynapseClientAsync,
				mockPortalGinInjector, mockModifiedCreatedBy);
		tab.setShowProjectInfoCallback(mockProjectInfoCallback);
		AccessRequirement tou = new TermsOfUseAccessRequirement();
		when(mockProjectEntityBundle.getAccessRequirements()).thenReturn(Collections.singletonList(tou));
		when(mockProjectEntityBundle.getEntity()).thenReturn(mockProjectEntity);
		when(mockProjectEntity.getId()).thenReturn(projectEntityId);
		when(mockProjectEntity.getName()).thenReturn(projectName);
		when(mockProjectEntityBundle.getPermissions()).thenReturn(mockPermissions);
		
		when(mockPortalGinInjector.createActionMenuWidget()).thenReturn(mockActionMenuWidget);
		when(mockPortalGinInjector.createEntityActionController()).thenReturn(mockEntityActionController);
		when(mockPortalGinInjector.createNewTableEntityWidget()).thenReturn(mockTableEntityWidget);
		
		when(mockTableEntityBundle.getAccessRequirements()).thenReturn(Collections.singletonList(tou));
		when(mockTableEntityBundle.getEntity()).thenReturn(mockTableEntity);
		when(mockTableEntity.getId()).thenReturn(tableEntityId);
		when(mockTableEntity.getName()).thenReturn(tableName);
		when(mockTableEntityBundle.getPermissions()).thenReturn(mockPermissions);
		
		when(mockFileViewEntity.getId()).thenReturn(tableEntityId);
		when(mockFileViewEntity.getName()).thenReturn(tableName);
		
		AsyncMockStubber.callSuccessWith(mockTableEntityBundle).when(mockSynapseClientAsync).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		// setup a complex query.
		query = new Query();
		query.setSql("select one, two, three from syn123 where name=\"bar\" and type in('one','two','three'");
		query.setLimit(101L);
		query.setOffset(33L);
		query.setIsConsistent(true);
		SortItem one = new SortItem();
		one.setColumn("one");
		one.setDirection(SortDirection.ASC);
		SortItem two = new SortItem();
		two.setColumn("one");
		two.setDirection(SortDirection.DESC);
		query.setSort(Arrays.asList(one, two));
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
		
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockTableEntity, mockEntityUpdatedHandler, areaToken);
		
		verifyTableConfiguration();
	}


	@Test
	public void testConfigureUsingFileView() {
		when(mockTableEntityBundle.getEntity()).thenReturn(mockFileViewEntity);
		String areaToken = null;
		boolean canCertifiedUserEdit = true;
		boolean isCertifiedUser = false;
		when(mockPermissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		when(mockPermissions.getIsCertifiedUser()).thenReturn(isCertifiedUser);
		
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockFileViewEntity, mockEntityUpdatedHandler, areaToken);
		
		verifyTableConfiguration();
	}
	
	private void verifyTableConfiguration() {
		verify(mockSynapseClientAsync).getEntityBundle(eq(tableEntityId), anyInt(), any(AsyncCallback.class));
		verify(mockBreadcrumb).configure(any(EntityPath.class), eq(EntityArea.TABLES));
		verify(mockPortalGinInjector).createActionMenuWidget();
		verify(mockPortalGinInjector).createEntityActionController();
		verify(mockBasicTitleBar).configure(mockTableEntityBundle);
		verify(mockEntityMetadata).setEntityBundle(mockTableEntityBundle, null);
		verify(mockTableEntityWidget).configure(eq(mockTableEntityBundle), eq(true), eq(tab), eq(mockActionMenuWidget));
		verify(mockView).setTableEntityWidget(any(Widget.class));
		verify(mockModifiedCreatedBy).configure(any(Date.class), anyString(), any(Date.class), anyString());
		verify(mockEntityMetadata).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		
		verify(mockView).setEntityMetadataVisible(true);
		verify(mockView).setBreadcrumbVisible(true);
		verify(mockView).setTableListVisible(false);
		verify(mockView).setTitlebarVisible(true);
		verify(mockView).clearActionMenuContainer();
		verify(mockView).clearTableEntityWidget();
		verify(mockModifiedCreatedBy).setVisible(false);

		//hide project info
		verify(mockProjectInfoCallback).invoke(false);
		
		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(tableName), captor.capture());
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
		
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(file, mockEntityUpdatedHandler, areaToken);
		verify(mockModifiedCreatedBy, Mockito.never()).configure(any(Date.class), anyString(), any(Date.class), anyString());
		verify(mockEntityMetadata).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		verify(mockView).setEntityMetadataVisible(false);
		verify(mockView).setBreadcrumbVisible(false);
		verify(mockView).setTableListVisible(true);
		verify(mockView).setTitlebarVisible(false);
		verify(mockView).clearActionMenuContainer();
		verify(mockView).clearTableEntityWidget();
		verify(mockModifiedCreatedBy).setVisible(false);

		//show project info
		verify(mockProjectInfoCallback).invoke(true);
		
		verify(mockTableListWidget).configure(mockProjectEntityBundle);
		
		Synapse place = getNewPlace(projectName);
		assertEquals(projectEntityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.TABLES, place.getArea());
		assertNull(place.getAreaToken());
	}
	
	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}
	
	private Synapse getNewPlace(String expectedName) {
		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(expectedName), captor.capture());
		return (Synapse)captor.getValue();
	}

	@Test
	public void testSetTableQueryWithNoToken() {
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockTableEntity, mockEntityUpdatedHandler, null);
		
		reset(mockTab);
		String queryToken = queryTokenProvider.queryToToken(query);
		tab.onQueryChange(query);
		
		Synapse place = getNewPlace(tableName);
		assertEquals(EntityArea.TABLES, place.getArea());
		assertTrue(place.getAreaToken().contains(queryToken));
	}
	
	@Test
	public void testSetTableQueryWithToken() {
		query.setOffset(1L);
		String startToken = queryTokenProvider.queryToToken(query);
		// Start with a token.
		
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockTableEntity, mockEntityUpdatedHandler, TablesTab.TABLE_QUERY_PREFIX + startToken);
		
		reset(mockTab);
		String queryToken = queryTokenProvider.queryToToken(query);
		tab.onQueryChange(query);
		
		Synapse place = getNewPlace(tableName);
		assertEquals(EntityArea.TABLES, place.getArea());
		assertTrue(place.getAreaToken().contains(queryToken));
	}
	

	@Test
	public void testGetTableQuery() {
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		
		String queryAreaToken;
		Query query1 = null;
		queryAreaToken = null;
		tab.configure(mockTableEntity, mockEntityUpdatedHandler, queryAreaToken);
		query1 = tab.getQueryString();
		assertNull(query1);
		
		queryAreaToken = "something else";
		tab.configure(mockTableEntity, mockEntityUpdatedHandler, queryAreaToken);
		query1 = tab.getQueryString();
		assertNull(query1);
		String token = queryTokenProvider.queryToToken(query);
		queryAreaToken = "query/"+token;
		tab.configure(mockTableEntity, mockEntityUpdatedHandler, queryAreaToken);
		query1 = tab.getQueryString();
		assertEquals(query, query1);
		query.setSql("SELECT 'query/' FROM syn123 LIMIT 1");
		token = queryTokenProvider.queryToToken(query);
		queryAreaToken = "query/"+token;
		tab.configure(mockTableEntity, mockEntityUpdatedHandler, queryAreaToken);
		query1 = tab.getQueryString();
		assertEquals(query, query1);
	}
	
	@Test
	public void testResetView() {
		tab.resetView();
		verify(mockSynapseAlert).clear();
		verify(mockView).setEntityMetadataVisible(false);
		verify(mockView).setBreadcrumbVisible(false);
		verify(mockView).setTableListVisible(false);
		verify(mockView).setTitlebarVisible(false);
		verify(mockView).clearActionMenuContainer();
		verify(mockView).clearTableEntityWidget();
		verify(mockView).clearActionMenuContainer();
		verify(mockProjectInfoCallback).invoke(false);
		verify(mockModifiedCreatedBy).setVisible(false);
	}

	@Test
	public void testShowProjectLoadError() {
		Exception projectLoadError = new Exception("error loading project");
		tab.setProject(projectEntityId, null, projectLoadError);
		tab.showProjectLevelUI();
		Synapse expectedPlace = new Synapse(projectEntityId, null, EntityArea.TABLES, null);
		verify(mockTab).setEntityNameAndPlace(projectEntityId, expectedPlace);
		verify(mockSynapseAlert).handleException(projectLoadError);
	}
}
