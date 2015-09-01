package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.events.EntityDeletedEvent;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.renderer.IFrameWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidget;
import org.sagebionetworks.web.client.widget.handlers.AreaChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableRowHeader;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;

public class EntityPageTopTest {

	QueryTokenProvider queryTokenProvider;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	EntityPageTopView mockView;
	EntitySchemaCache mockSchemaCache;
	JSONObjectAdapter jsonObjectAdapter;
	IconsImageBundle mockIconsImageBundle;
	EventBus mockEventBus;
	JiraURLHelper mockJiraURLHelper;
	WidgetRegistrar mockWidgetRegistrar;
	EntityPageTop pageTop;
	ExampleEntity entity;
	TableEntity tableEntity;
	WidgetRendererPresenter testWidgetRenderer;
	String entityId = "syn123";
	Long entityVersion = 1L;
	String projectId = "syn456";
	String wikiSubpage = "987654";
	EntityBundle entityBundle;
	EntityBundle entityBundleTable;
	EntityHeader projectHeader;
	ArgumentCaptor<Synapse> capture;
	Synapse gotoPlace;
	EntityBundle projectBundle;
	Project projectEntity = new Project();		
	AreaChangeHandler areaChangeHandler;
	Query query;


	@Before
	public void before() throws JSONObjectAdapterException {
		queryTokenProvider = new QueryTokenProvider(new AdapterFactoryImpl());
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockView = mock(EntityPageTopView.class);
		mockSchemaCache = mock(EntitySchemaCache.class);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		mockEventBus = mock(EventBus.class);
		mockJiraURLHelper = mock(JiraURLHelper.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		areaChangeHandler = mock(AreaChangeHandler.class);
		
		pageTop = new EntityPageTop(mockView, mockAuthenticationController,
				mockSchemaCache,
				mockGlobalApplicationState, mockEventBus, queryTokenProvider);
		pageTop.setAreaChangeHandler(areaChangeHandler);
		pageTop.clearProjectAreaState();
		// Setup the the entity
		entity = new ExampleEntity();
		entity.setId(entityId);
		entity.setEntityType(ExampleEntity.class.getName());
		testWidgetRenderer = new YouTubeWidget(mock(IFrameWidgetView.class));
		when(mockWidgetRegistrar.getWidgetRendererForWidgetDescriptor(any(WikiPageKey.class), anyString(), any(Map.class), any(Callback.class), any(Long.class))).thenReturn(testWidgetRenderer);

		
		// setup table entity
		tableEntity = new TableEntity();
		tableEntity.setId(entityId);
		tableEntity.setEntityType(TableEntity.class.getName());
		entityBundleTable = new EntityBundle();
		entityBundleTable.setEntity(tableEntity);
		
		entityBundle = new EntityBundle();
		entityBundle.setEntity(entity);
		projectHeader = new EntityHeader();
		projectHeader.setId(projectId);
		
		projectEntity.setId(projectId);
		projectBundle = new EntityBundle();
		projectBundle.setEntity(projectEntity);
		
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
		
		ObjectSchema schema = new ObjectSchema();
		schema.setTitle("");
		when(mockSchemaCache.getSchemaEntity(any(Entity.class))).thenReturn(schema);
	}
	
	@Test
	public void testProjectAreaState() {
		assertNull(pageTop.getCurrentEntityPageProjectId());
		pageTop.configure(projectBundle, null, projectHeader, null, null);
		assertEquals(projectId, pageTop.getCurrentEntityPageProjectId());
		
		//on tab change, page top is recreated, but the project id should change iff we reconfigure with a different project id
		pageTop = new EntityPageTop(mockView, mockAuthenticationController,
				mockSchemaCache,
				mockGlobalApplicationState, mockEventBus, queryTokenProvider);
		assertEquals(projectId, pageTop.getCurrentEntityPageProjectId());
		//configure with a table associated to the same project
		pageTop.configure(entityBundleTable, null, projectHeader, null, null);
		//did not change
		assertEquals(projectId, pageTop.getCurrentEntityPageProjectId());
		//not with a different project
		EntityHeader newProjectHeader = new EntityHeader();
		String newProjectId = "syn3141599999999";
		newProjectHeader.setId(newProjectId);
		pageTop.configure(entityBundleTable, null, newProjectHeader, null, null);
		assertEquals(newProjectId, pageTop.getCurrentEntityPageProjectId());
	}
		
	@Test 
	public void testProjectInPageNoState() {		
		// default project visit, no area, no area token
		pageTop.configure(projectBundle, null, projectHeader, null, null);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));

		//check that the area is pushed to the url
		pageTop.refresh();
		ArgumentCaptor<Synapse> placeCaptor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockGlobalApplicationState).replaceCurrentPlace(placeCaptor.capture());
		Synapse place = placeCaptor.getValue();
		assertEquals(EntityArea.WIKI, place.getArea());
		assertNull(place.getAreaToken());
	}
	
	@Test 
	public void testTableInPageNoState() {		
		// default table visit, no area, no area token
		pageTop.configure(entityBundleTable, null, projectHeader, null, null);
		pageTop.refresh();
		verify(mockGlobalApplicationState, never()).replaceCurrentPlace(any(Place.class));
	}

	@Test 
	public void testProjectWithFilesStateNoArea_SWC_1240() {
		// create some state for a file entity but not explicitly setting the Files area
		pageTop.configure(entityBundle, entityVersion, projectHeader, null, null);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));		
		// go back to project wiki
		pageTop.configure(projectBundle, null, projectHeader, EntityArea.WIKI, null);		
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));		
		// click files to test state
		pageTop.gotoProjectArea(EntityArea.FILES, EntityArea.WIKI);
		gotoPlace = captureGoTo();
		assertNull(gotoPlace.getArea()); // should not specify area for sub entity
		assertEquals(entityId, gotoPlace.getEntityId());
		assertEquals(entityVersion, gotoPlace.getVersionNumber());				
	}	
	
	@Test 
	public void testWikiSubPageState() {
		// create some state for the wiki tab on project
		pageTop.configure(projectBundle, null, projectHeader, EntityArea.WIKI, wikiSubpage);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// go to files tab on project
		pageTop.configure(projectBundle, null, projectHeader, EntityArea.FILES, null);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));		
		// now lets go to a child file
		pageTop.configure(entityBundle, entityVersion, projectHeader, null, null);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));				
		// click wiki to test
		pageTop.gotoProjectArea(EntityArea.WIKI, EntityArea.FILES);
		gotoPlace = captureGoTo();
		assertEquals(EntityArea.WIKI, gotoPlace.getArea());
		assertEquals(projectId, gotoPlace.getEntityId());
		assertEquals(wikiSubpage, gotoPlace.getAreaToken());
	}	
	
	@Test 
	public void testGoingBackToProjectWikiWithSubPageState() {
		// create some state for the wiki tab on project
		pageTop.configure(projectBundle, null, projectHeader, EntityArea.WIKI, wikiSubpage);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// now lets go to the project WIKI area with no subpage token
		projectBundle = new EntityBundle();
		projectBundle.setEntity(projectEntity);
		pageTop.configure(projectBundle, entityVersion, projectHeader, EntityArea.WIKI, null);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// now lets go to a child file
		pageTop.configure(entityBundle, entityVersion, projectHeader, null, null);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));				
		// click wiki to test back to wiki root
		pageTop.gotoProjectArea(EntityArea.WIKI, EntityArea.FILES);
		gotoPlace = captureGoTo();
		assertEquals(EntityArea.WIKI, gotoPlace.getArea());
		assertEquals(projectId, gotoPlace.getEntityId());		
		assertNull(gotoPlace.getAreaToken());
	}	

	// just like above but not going back to the project wiki, go to the project unparameterized
	@Test 
	public void testGoingBackToProjectWithSubPageState() {
		// create some state for the wiki tab on project
		pageTop.configure(projectBundle, null, projectHeader, EntityArea.WIKI, wikiSubpage);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// now lets go to the project WIKI area with no subpage 
		projectBundle = new EntityBundle();
		projectBundle.setEntity(projectEntity);
		pageTop.configure(projectBundle, entityVersion, projectHeader, null, null);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// now lets go to a child file
		pageTop.configure(entityBundle, entityVersion, projectHeader, null, null);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));				
		// click wiki to test back to wiki root
		pageTop.gotoProjectArea(null, EntityArea.FILES);
		gotoPlace = captureGoTo();
		assertNull(gotoPlace.getArea());
		assertEquals(projectId, gotoPlace.getEntityId());		
		assertNull(gotoPlace.getAreaToken());
	}	

	@Test 
	public void testVisitNewProject() {
		// create some state for the wiki tab on project
		pageTop.configure(projectBundle, null, projectHeader, EntityArea.WIKI, wikiSubpage);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// create some state for the files tab on project
		pageTop.configure(entityBundle, entityVersion, projectHeader, null, null);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// visit a new project, make sure all area state is wiped out
		String newProjectId = "syn789";
		assertFalse(newProjectId.equals(projectId)); // assumption check
		projectEntity = new Project();
		projectEntity.setId(newProjectId);
		projectBundle = new EntityBundle();
		projectBundle.setEntity(projectEntity);
		EntityHeader newProjectHeader = new EntityHeader();
		newProjectHeader.setId(newProjectId);
		pageTop.configure(projectBundle, entityVersion, newProjectHeader, EntityArea.WIKI, null);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// click files to test
		pageTop.gotoProjectArea(EntityArea.FILES, EntityArea.WIKI);
		gotoPlace = captureGoTo();
		assertEquals(EntityArea.FILES, gotoPlace.getArea());
		assertEquals(newProjectId, gotoPlace.getEntityId());
		assertNull(gotoPlace.getVersionNumber());					
		// visit wiki subpage and assure switch to default files view and back is not place change
		pageTop.configure(projectBundle, entityVersion, newProjectHeader, EntityArea.WIKI, wikiSubpage);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));

	}	

	@Test
	public void testRevisitFilesAfterFileStateAndFilesRootVisit() {
		// create some state for files tab
		pageTop.configure(entityBundle, entityVersion, projectHeader, null, null);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));		
		// go to files root
		pageTop.configure(projectBundle, null, projectHeader, EntityArea.FILES, null);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));				
		// go wiki tab
		pageTop.configure(projectBundle, null, projectHeader, EntityArea.WIKI, null);		
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));		
		// click files to test that there is no files tab state
		pageTop.gotoProjectArea(EntityArea.FILES, EntityArea.WIKI);
		gotoPlace = captureGoTo();
		assertEquals(EntityArea.FILES, gotoPlace.getArea()); 
		assertEquals(projectId, gotoPlace.getEntityId());
		assertNull(gotoPlace.getVersionNumber());				
	}

	@Test
	public void testRevisitWikiAfterFileStateAndFilesRootVisit_SWC_922() {
		// create some state for wiki tab
		pageTop.configure(projectBundle, null, projectHeader, EntityArea.WIKI, wikiSubpage);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));		
		// click on files tab
		pageTop.configure(projectBundle, null, projectHeader, EntityArea.FILES, null);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));		
		// click on files child entity
		pageTop.configure(entityBundle, entityVersion, projectHeader, null, null);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));		
		// go to files root
		pageTop.configure(projectBundle, null, projectHeader, EntityArea.FILES, null);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));				
		// click on wiki tab
		pageTop.gotoProjectArea(EntityArea.WIKI, EntityArea.FILES);
		gotoPlace = captureGoTo();
		assertEquals(EntityArea.WIKI, gotoPlace.getArea()); 
		assertEquals(projectId, gotoPlace.getEntityId());
		assertEquals(wikiSubpage, gotoPlace.getAreaToken());				
	}

	@Test
	public void testDeletedEntityIdInTabState_SWC_1046() {
		// create some state for files tab
		pageTop.configure(entityBundle, entityVersion, projectHeader, null, null);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));		
		// now delete entity 
		pageTop.entityDeleted(new EntityDeletedEvent(entityId));
		// goto files tab and check that entity is gone from the state and we are at project root
		pageTop.gotoProjectArea(EntityArea.FILES, EntityArea.FILES);
		gotoPlace = captureGoTo();
		assertEquals(EntityArea.FILES, gotoPlace.getArea());
		assertEquals(projectId, gotoPlace.getEntityId());
	}

	@Test
	public void testDeletedTableEntityIdInTabState() {
		// create some state for tables tab
		pageTop.configure(entityBundleTable, entityVersion, projectHeader, null, null);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));		
		// now delete entity 
		pageTop.entityDeleted(new EntityDeletedEvent(entityId));
		// goto tables tab and check that entity is gone from the state and we are at tables root
		pageTop.gotoProjectArea(EntityArea.TABLES, EntityArea.TABLES);
		gotoPlace = captureGoTo();
		assertEquals(EntityArea.TABLES, gotoPlace.getArea());
		assertEquals(projectId, gotoPlace.getEntityId());
	}
	
	@Test 
	public void testEntityDeleted_SWC_1116() {
		String id = "syn123";
		EntityDeletedEvent event = new EntityDeletedEvent(id);
		pageTop.entityDeleted(event);		
	}
	
	@Test 
	public void testSetArea() {
		String areaToken = "token";
		pageTop.setArea(EntityArea.FILES, areaToken);
		verify(areaChangeHandler).areaChanged(EntityArea.FILES, areaToken);
	}
	
	@Test
	public void testSetTableQueryWithNoToken() {
		String queryToken = queryTokenProvider.queryToToken(query);
		pageTop.setTableQuery(query);
		verify(areaChangeHandler).replaceArea(eq(EntityArea.TABLES), contains(queryToken));
		verify(areaChangeHandler, never()).areaChanged(eq(EntityArea.TABLES), contains(queryToken));	
	}
	
	@Test
	public void testSetTableQueryWithToken() {
		query.setOffset(1L);
		String startToken = queryTokenProvider.queryToToken(query);
		// Start with a token.
		pageTop.setArea(EntityArea.TABLES, EntityPageTop.TABLE_QUERY_PREFIX + startToken);
		reset(areaChangeHandler);
		String queryToken = queryTokenProvider.queryToToken(query);
		pageTop.setTableQuery(query);
		verify(areaChangeHandler, never()).replaceArea(eq(EntityArea.TABLES), contains(queryToken));		
		verify(areaChangeHandler).areaChanged(eq(EntityArea.TABLES), contains(queryToken));		
	}
	
	@Test
	public void testSetTableRow() {
		String rowId="9999";
		TableRowHeader header = new TableRowHeader(rowId, null);
		pageTop.setTableRow(header);		
		verify(areaChangeHandler).areaChanged(eq(EntityArea.TABLES), contains(rowId));		
	}
	
	@Test
	public void testSetTableRowVersion() {
		String rowId="9999";
		String version="8888";
		TableRowHeader header = new TableRowHeader(rowId, version);
		pageTop.setTableRow(header);		
		verify(areaChangeHandler).areaChanged(eq(EntityArea.TABLES), contains(rowId));		
		verify(areaChangeHandler).areaChanged(eq(EntityArea.TABLES), contains(version));
	}
	
	@Test
	public void testGetTableRowHeader() {
		String rowAreaToken;
		TableRowHeader rh;

		rowAreaToken = null;
		pageTop.configure(entityBundle, entityVersion, projectHeader, EntityArea.TABLES, rowAreaToken);		
		rh = pageTop.getTableRowHeader();
		assertNull(rh);

		rowAreaToken = "something else";
		pageTop.configure(entityBundle, entityVersion, projectHeader, EntityArea.TABLES, rowAreaToken);		
		rh = pageTop.getTableRowHeader();
		assertNull(rh);
		
		rowAreaToken = "row/9999";
		pageTop.configure(entityBundle, entityVersion, projectHeader, EntityArea.TABLES, rowAreaToken);		
		rh = pageTop.getTableRowHeader();
		assertEquals("9999", rh.getRowId());
		assertNull(rh.getVersion());
		
		rowAreaToken = "row/9999/rowversion/8888";
		pageTop.configure(entityBundle, entityVersion, projectHeader, EntityArea.TABLES, rowAreaToken);		
		rh = pageTop.getTableRowHeader();
		assertEquals("9999", rh.getRowId());
		assertEquals("8888", rh.getVersion());
		
	}
	
	@Test
	public void testGetTableQuery() {
		String queryAreaToken;
		Query query1 = null;
		queryAreaToken = null;
		pageTop.configure(entityBundle, entityVersion, projectHeader, EntityArea.TABLES, queryAreaToken);		
		query1 = pageTop.getTableQuery();
		assertNull(query1);
		
		queryAreaToken = "something else";
		pageTop.configure(entityBundle, entityVersion, projectHeader, EntityArea.TABLES, queryAreaToken);		
		query1 = pageTop.getTableQuery();
		assertNull(query1);
		String token = queryTokenProvider.queryToToken(query);
		queryAreaToken = "query/"+token;
		pageTop.configure(entityBundle, entityVersion, projectHeader, EntityArea.TABLES, queryAreaToken);		
		query1 = pageTop.getTableQuery();
		assertEquals(query, query1);
		query.setSql("SELECT 'query/' FROM syn123 LIMIT 1");
		token = queryTokenProvider.queryToToken(query);
		queryAreaToken = "query/"+token;
		pageTop.configure(entityBundle, entityVersion, projectHeader, EntityArea.TABLES, queryAreaToken);		
		query1 = pageTop.getTableQuery();
		assertEquals(query, query1);
		
	}

	@Test
	public void testGetWikiPageId() {
		String rootWikiId = "IAmGroot";
		String areaToken = "notRoot";
		
		assertEquals(rootWikiId, pageTop.getWikiPageId(EntityArea.FILES, areaToken, rootWikiId));
		assertEquals(areaToken, pageTop.getWikiPageId(EntityArea.WIKI, areaToken, rootWikiId));
		assertEquals(rootWikiId, pageTop.getWikiPageId(EntityArea.WIKI, rootWikiId, rootWikiId));
		assertEquals(rootWikiId, pageTop.getWikiPageId(EntityArea.WIKI, "", rootWikiId));
		assertEquals(rootWikiId, pageTop.getWikiPageId(EntityArea.WIKI, null, rootWikiId));
	}

	@Test
	public void testHandleWikiReloadSuccess() {
		String wikiPageId = "123";
		// create some state for the wiki tab on project
		pageTop.configure(projectBundle, null, projectHeader, EntityArea.WIKI, wikiSubpage);
		pageTop.handleWikiReload(wikiPageId);
		verify(areaChangeHandler).areaChanged(EntityArea.WIKI, wikiPageId);
		verify(mockView).configureProjectActionMenu(projectBundle, wikiPageId);
	}
	
	@Test
	public void testConfigureFileHistoryIsFileLatestVersion() {
		FileEntity file = new FileEntity();
		file.setId("123");
		EntityBundle fileEntityBundle = new EntityBundle();
		fileEntityBundle.setEntity(file);
		pageTop.configure(fileEntityBundle, null, projectHeader, EntityArea.FILES, wikiSubpage);
		pageTop.refresh();
		verify(mockView).setFileHistoryVisible(false);
	}
	
	public void testConfigureFileHistoryIsFileEarlierVersion() {
		FileEntity file = new FileEntity();
		file.setId("123");
		EntityBundle fileEntityBundle = new EntityBundle();
		fileEntityBundle.setEntity(file);
		pageTop.configure(fileEntityBundle, 1L, projectHeader, EntityArea.FILES, wikiSubpage);
		pageTop.refresh();
		verify(mockView).setFileHistoryVisible(true);
	}

	@Test
	public void testConfigureFileHistoryIsProject() {
		Project project = new Project();
		project.setId("123");
		EntityBundle projectEntityBundle = new EntityBundle();
		projectEntityBundle.setEntity(project);
		pageTop.configure(projectEntityBundle, 1L, projectHeader, EntityArea.FILES, wikiSubpage);
		pageTop.refresh();
		verify(mockView).setFileHistoryVisible(false);
	}
	
	/*
	 * Private Methods
	 */
	private Synapse captureGoTo() {
		capture = ArgumentCaptor.forClass(Synapse.class);
		verify(mockPlaceChanger).goTo(capture.capture());
		return capture.getValue();
	}

}
