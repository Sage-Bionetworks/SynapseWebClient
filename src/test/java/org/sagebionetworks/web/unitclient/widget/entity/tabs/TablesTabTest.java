package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTabView;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;

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
	ProvenanceWidget mockProvenanceWidget;
	@Mock
	StuAlert mockSynapseAlert;
	@Mock
	PortalGinInjector mockPortalGinInjector;

	@Mock
	EntityBundle mockProjectEntityBundle;
	@Mock
	EntityBundle mockTableEntityBundle;
	@Mock
	TableEntity mockTableEntity;
	@Mock
	EntityView mockFileViewEntity;
	@Mock
	Project mockProjectEntity;
	@Mock
	UserEntityPermissions mockPermissions;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	@Mock
	CallbackP<String> mockEntitySelectedCallback;
	@Mock
	TableEntityWidget mockTableEntityWidget;
	@Mock
	ModifiedCreatedByWidget mockModifiedCreatedBy;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	@Mock
	EntityHeader mockEntityHeader;
	String projectEntityId = "syn666666";
	String projectName = "a test project";
	String tableEntityId = "syn22";
	String tableName = "test table";
	@Mock
	QueryTokenProvider mockQueryTokenProvider;
	
	TablesTab tab;
	Query query;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new TablesTab(mockTab, mockPortalGinInjector);
		
		when(mockPortalGinInjector.getTablesTabView()).thenReturn(mockView);
		when(mockPortalGinInjector.getTableListWidget()).thenReturn(mockTableListWidget);
		when(mockPortalGinInjector.getBasicTitleBar()).thenReturn(mockBasicTitleBar);
		when(mockPortalGinInjector.getBreadcrumb()).thenReturn(mockBreadcrumb);
		when(mockPortalGinInjector.getEntityMetadata()).thenReturn(mockEntityMetadata);
		when(mockPortalGinInjector.getQueryTokenProvider()).thenReturn(mockQueryTokenProvider);
		when(mockPortalGinInjector.getStuAlert()).thenReturn(mockSynapseAlert);
		when(mockPortalGinInjector.getModifiedCreatedByWidget()).thenReturn(mockModifiedCreatedBy);
		when(mockPortalGinInjector.getProvenanceRenderer()).thenReturn(mockProvenanceWidget);
		
		tab.setEntitySelectedCallback(mockEntitySelectedCallback);
		when(mockProjectEntityBundle.getEntity()).thenReturn(mockProjectEntity);
		when(mockProjectEntity.getId()).thenReturn(projectEntityId);
		when(mockProjectEntity.getName()).thenReturn(projectName);
		when(mockProjectEntityBundle.getPermissions()).thenReturn(mockPermissions);
		
		when(mockPortalGinInjector.createNewTableEntityWidget()).thenReturn(mockTableEntityWidget);
		
		when(mockTableEntityBundle.getEntity()).thenReturn(mockTableEntity);
		when(mockTableEntity.getId()).thenReturn(tableEntityId);
		when(mockTableEntity.getName()).thenReturn(tableName);
		when(mockTableEntityBundle.getPermissions()).thenReturn(mockPermissions);
		
		when(mockFileViewEntity.getId()).thenReturn(tableEntityId);
		when(mockFileViewEntity.getName()).thenReturn(tableName);
		
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
		tab.lazyInject();
	}
	
	@Test
	public void testSetTabClickedCallback() {
		tab.setTabClickedCallback(mockOnClickCallback);
		verify(mockTab).addTabClickedCallback(mockOnClickCallback);
	}

	@Test
	public void testClickTable() {
		//verify that clicking on an item in the tables list sends the event back to the entity page top (to get the new target entity)
		String newEntityId = "syn9839838";
		when(mockEntityHeader.getId()).thenReturn(newEntityId);
		verify(mockTableListWidget).setTableClickedCallback(callbackPCaptor.capture());
		callbackPCaptor.getValue().invoke(mockEntityHeader);
		verify(mockEntitySelectedCallback).invoke(newEntityId);
	}

	@Test
	public void testConfigureUsingTable() {
		String areaToken = null;
		
		boolean canCertifiedUserEdit = true;
		boolean isCertifiedUser = false;
		when(mockPermissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		when(mockPermissions.getIsCertifiedUser()).thenReturn(isCertifiedUser);
		
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockTableEntityBundle, areaToken, mockActionMenuWidget);
		
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
		tab.configure(mockTableEntityBundle, areaToken, mockActionMenuWidget);
		
		verifyTableConfiguration();
	}
	
	private void verifyTableConfiguration() {
		verify(mockBreadcrumb).configure(any(EntityPath.class), eq(EntityArea.TABLES));
		verify(mockBasicTitleBar).configure(mockTableEntityBundle);
		verify(mockEntityMetadata).configure(mockTableEntityBundle, null, mockActionMenuWidget);
		verify(mockTableEntityWidget).configure(eq(mockTableEntityBundle), eq(true), eq(tab), eq(mockActionMenuWidget));
		verify(mockView).setTableEntityWidget(any(Widget.class));
		verify(mockModifiedCreatedBy).configure(any(Date.class), anyString(), any(Date.class), anyString());
		verify(mockProvenanceWidget).configure(any(Map.class));
		verify(mockView).setProvenanceVisible(true);
		verify(mockView).setEntityMetadataVisible(true);
		verify(mockView).setBreadcrumbVisible(true);
		verify(mockView).setTableListVisible(false);
		verify(mockView).setTitlebarVisible(true);
		verify(mockView).clearActionMenuContainer();
		verify(mockView).clearTableEntityWidget();
		verify(mockModifiedCreatedBy).setVisible(false);

		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(tableName), captor.capture());
		Synapse place = (Synapse)captor.getValue();
		assertEquals(tableEntityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.TABLES, place.getArea());
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
		tab.configure(mockProjectEntityBundle, areaToken, mockActionMenuWidget);
		verify(mockModifiedCreatedBy, Mockito.never()).configure(any(Date.class), anyString(), any(Date.class), anyString());
		verify(mockView).setEntityMetadataVisible(false);
		verify(mockView).setBreadcrumbVisible(false);
		verify(mockView).setTableListVisible(true);
		verify(mockView).setTitlebarVisible(false);
		verify(mockView).clearActionMenuContainer();
		verify(mockView).clearTableEntityWidget();
		verify(mockModifiedCreatedBy).setVisible(false);
		verify(mockView).setProvenanceVisible(false);
		verify(mockView, never()).setProvenanceVisible(true);
		
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
		verify(mockTab, atLeastOnce()).setEntityNameAndPlace(eq(expectedName), captor.capture());
		return (Synapse)captor.getValue();
	}

	@Test
	public void testSetTableQueryWithNoToken() {
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockTableEntityBundle, null, mockActionMenuWidget);
		
		reset(mockTab);
		when(mockTab.isTabPaneVisible()).thenReturn(true);
		when(mockTableEntityWidget.getDefaultQuery()).thenReturn(query);
		tab.onQueryChange(query);
		
		Synapse place = getNewPlace(tableName);
		assertEquals(EntityArea.TABLES, place.getArea());
		assertTrue(place.getAreaToken().isEmpty());
	}
	
	@Test
	public void testSetQueryPaneNotVisible() {
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockTableEntityBundle, null, mockActionMenuWidget);
		reset(mockTab);
		when(mockTab.isTabPaneVisible()).thenReturn(false);
		when(mockTableEntityWidget.getDefaultQuery()).thenReturn(query);
		tab.onQueryChange(query);
		
		verify(mockTab, never()).setEntityNameAndPlace(anyString(), any(Synapse.class));
		verify(mockTab, never()).showTab(anyBoolean());
	}
	
	@Test
	public void testSetTableQueryWithToken() {
		query.setOffset(1L);
		when(mockTableEntityWidget.getDefaultQuery()).thenReturn(new Query());
		String startToken = "start token";
		// Start with a token.
		String encodedToken = "encoded token";
		when(mockQueryTokenProvider.queryToToken(any(Query.class))).thenReturn(encodedToken);
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockTableEntityBundle, TablesTab.TABLE_QUERY_PREFIX + startToken, mockActionMenuWidget);
		
		reset(mockTab);
		when(mockTab.isTabPaneVisible()).thenReturn(true);
		tab.onQueryChange(query);
		
		Synapse place = getNewPlace(tableName);
		assertEquals(EntityArea.TABLES, place.getArea());
		assertTrue(place.getAreaToken().contains(encodedToken));
	}
	

	@Test
	public void testGetTableQuery() {
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		
		String queryAreaToken;
		Query query1 = null;
		queryAreaToken = null;
		tab.configure(mockTableEntityBundle, queryAreaToken, mockActionMenuWidget);
		query1 = tab.getQueryString();
		assertNull(query1);
		
		queryAreaToken = "something else";
		tab.configure(mockTableEntityBundle, queryAreaToken, mockActionMenuWidget);
		query1 = tab.getQueryString();
		assertNull(query1);
		String token = "encoded query token";
		queryAreaToken = "query/"+token;
		when(mockQueryTokenProvider.tokenToQuery(anyString())).thenReturn(query);
		tab.configure(mockTableEntityBundle, queryAreaToken, mockActionMenuWidget);
		query1 = tab.getQueryString();
		assertEquals(query, query1);
		query.setSql("SELECT 'query/' FROM syn123 LIMIT 1");
		token = "encoded query token 2";
		queryAreaToken = "query/"+token;
		tab.configure(mockTableEntityBundle, queryAreaToken, mockActionMenuWidget);
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
		verify(mockModifiedCreatedBy).setVisible(false);
		verify(mockView).setProvenanceVisible(false);
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
