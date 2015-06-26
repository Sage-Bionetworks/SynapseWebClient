package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityType;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItem;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsTreeItem;

public class EntityTreeBrowserTest {
	EntityTreeBrowserView mockView;
	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	IconsImageBundle mockIconsImageBundle;
	PortalGinInjector mockInjector;
	AdapterFactory adapterFactory;
	EntityTreeBrowser entityTreeBrowser;
	EntityQueryResults searchResults;
	EntityTreeItem mockEntityTreeItem;
	MoreTreeItem mockMoreTreeItem;
	IsTreeItem mockLoadingItem;
	String parentId;

	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(EntityTreeBrowserView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);
		mockInjector = mock(PortalGinInjector.class);
		mockEntityTreeItem = mock(EntityTreeItem.class);
		mockMoreTreeItem = mock(MoreTreeItem.class);
		mockLoadingItem = mock(IsTreeItem.class);
		adapterFactory = new AdapterFactoryImpl();
		entityTreeBrowser = new EntityTreeBrowser(mockInjector, mockView,
				mockSynapseClient, mockAuthenticationController,  mockGlobalApplicationState,
				mockIconsImageBundle, adapterFactory);
		verify(mockView).setPresenter(entityTreeBrowser);
		reset(mockView);
		parentId = "testParentId";
		searchResults = new EntityQueryResults();
		List<EntityQueryResult> entities = new ArrayList<EntityQueryResult>();
		searchResults.setEntities(entities);
		when(mockInjector.getEntityTreeItemWidget()).thenReturn(
				mockEntityTreeItem);
		EntityQueryResult header = new EntityQueryResult();
		header.setId(parentId);
		header.setEntityType("folder");
		when(mockEntityTreeItem.getHeader()).thenReturn(header);
//		when(mockView.appendLoading(any(EntityTreeItem.class))).thenReturn(mockLoadingItem);
//		when(mockView.insertLoading(any(EntityTreeItem.class), Mockito.anyInt())).thenReturn(mockLoadingItem);
		Mockito.when(mockInjector.getMoreTreeWidget()).thenReturn(mockMoreTreeItem);
		AsyncMockStubber
				.callSuccessWith(searchResults)
				.when(mockSynapseClient)
				.executeEntityQuery(any(EntityQuery.class),
						any(AsyncCallback.class));
	}

//	@Test
//	public void testGetFolderChildren() {
//		AsyncCallback<List<EntityHeader>> mockCallback = mock(AsyncCallback.class);
//		entityTreeBrowser.getChildren("123", null, 0);
//		ArgumentCaptor<EntityQuery> captor = ArgumentCaptor
//				.forClass(EntityQuery.class);
//		verify(mockSynapseClient).executeEntityQuery(
//				captor.capture(), any(AsyncCallback.class));
//		List<EntityQuery> queries = captor.getAllValues();
//		assertEquals(EntityType.folder, queries.get(0).getFilterByType());
//		assertEquals(EntityType.link, queries.get(1).getFilterByType());
//		assertEquals(EntityType.file, queries.get(2).getFilterByType());
//	}

	@Test
	public void testCreateGetChildrenQuery() {
		String parentId = "9";
		EntityQuery query = entityTreeBrowser.createGetChildrenQuery(parentId,
				0);

		// verify sort
		assertEquals(EntityFieldName.name.name(), query.getSort()
				.getColumnName());
		assertEquals(SortDirection.ASC, query.getSort().getDirection());
		List<Condition> conditions = query.getConditions();
		assertEquals(2, conditions.size());
	}

	@Test
	public void testGetFolderChildrenRaceCondition() {
		mockSynapseClient = mock(SynapseClientAsync.class);
		AsyncCallback<List<EntityHeader>> mockCallback = mock(AsyncCallback.class);
		entityTreeBrowser = new EntityTreeBrowser(mockInjector, mockView,
				mockSynapseClient, mockAuthenticationController, mockGlobalApplicationState,
				mockIconsImageBundle, adapterFactory);
		entityTreeBrowser.getChildren("123", null, 0);
		// capture the servlet call
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor
				.forClass(AsyncCallback.class);
		verify(mockSynapseClient).executeEntityQuery(any(EntityQuery.class),
				captor.capture());
		// before invoking asynccallback.success, set the current entity id to
		// something else (simulating that the user
		// has selected a different folder while this was still processing)
		captor.getValue().onSuccess(searchResults);
		verify(mockCallback, never()).onSuccess(anyList());
	}

	@Test
	public void testMoreButtonRootLevel() {
		long maxLim = entityTreeBrowser.getMaxLimit();
		setQueryResults(4 * maxLim, 0, maxLim);
		entityTreeBrowser.getChildren(parentId, null, 0);
		// Creates the limited number of entity items
		// 100 links, and 100 files
		verify(mockView, times((int) entityTreeBrowser.getMaxLimit()))
				.appendRootEntityTreeItem(any(EntityTreeItem.class));
		verify(mockView).placeRootMoreTreeItem(
				any(MoreTreeItem.class), Mockito.eq(parentId),
				Mockito.eq(maxLim));
	}

	// Taken care of by expandTreeItemOnOpen
	@Test
	public void testMoreButtonChildLevel() {
		long maxLim = entityTreeBrowser.getMaxLimit();
		setQueryResults(4 * maxLim, 0, maxLim);
		entityTreeBrowser.getChildren(parentId, mockEntityTreeItem, 0);
		// Creates the limited number of entity items
		verify(mockView, times((int) maxLim)).appendChildEntityTreeItem(
				any(EntityTreeItem.class), Mockito.eq(mockEntityTreeItem));
		// Calls once for folders, once for files.
		verify(mockView).placeChildMoreTreeItem(any(MoreTreeItem.class), Mockito.eq(mockEntityTreeItem),
				Mockito.eq(maxLim));
	}

	@Test
	public void testGetMoreButtonRequery() {
		long maxLim = entityTreeBrowser.getMaxLimit();
		setQueryResults(4 * maxLim, 0, maxLim);
		entityTreeBrowser.getChildren(parentId, mockEntityTreeItem, 0);
		// Adds the limited number of entity items
		verify(mockView, times((int) maxLim)).appendChildEntityTreeItem(
				any(EntityTreeItem.class), Mockito.eq(mockEntityTreeItem));
		// Calls once for folders, once for files.
		verify(mockView).placeChildMoreTreeItem(
				any(MoreTreeItem.class), Mockito.eq(mockEntityTreeItem),
				Mockito.eq(maxLim));
		setQueryResults(2 * maxLim, maxLim, 2 * maxLim);
		// Verified that the more button is created with offset maxLim, so
		// directly the moreButton should call getFolderChildren with that
		// offset
		entityTreeBrowser.getChildren(parentId, mockEntityTreeItem,
				maxLim);
		// Adds the rest of the entity items
		verify(mockView, times((int) (2 * maxLim))).appendChildEntityTreeItem(
				any(EntityTreeItem.class), Mockito.eq(mockEntityTreeItem));
		// Does not create any more "More (Entity)" buttons, which still should
		// be at 2 times.
		verify(mockView).placeChildMoreTreeItem(
				any(MoreTreeItem.class), Mockito.eq(mockEntityTreeItem),
				Mockito.eq(maxLim));
	}

	// Used to create test query results
	private void setQueryResults(long totalEntries, long startIndex,
			long endIndex) {
		List<EntityQueryResult> entities = new ArrayList<EntityQueryResult>();
		for (int i = 0; i < totalEntries; i++) {
			EntityQueryResult res = new EntityQueryResult();
			res.setParentId(parentId);
			res.setId("testResultId" + i);
			res.setEntityType("folder");
			entities.add(res);
		}
		searchResults.setEntities(entities.subList((int) startIndex,
				(int) endIndex));
		searchResults.setTotalEntityCount(totalEntries);
	}
	
	@Test
	public void testConfigure() {
		List<EntityHeader> headers = new ArrayList<EntityHeader>();
		headers.add(new EntityHeader());
		
		entityTreeBrowser.configure(headers);
		verify(mockView).clear();
		verify(mockView).setLoadingVisible(true);
		verify(mockView).appendRootEntityTreeItem(any(EntityTreeItem.class));
		verify(mockView).setLoadingVisible(false);
	}
	
	@Test
	public void testEntitySelectedHandler() {
		EntitySelectedHandler handler = mock(EntitySelectedHandler.class);
		assertNull(entityTreeBrowser.getEntitySelectedHandler());
		//set entity selected handler
		entityTreeBrowser.setEntitySelectedHandler(handler);
		assertEquals(handler, entityTreeBrowser.getEntitySelectedHandler());
		//verify firing a selection event
		entityTreeBrowser.fireEntitySelectedEvent();
		verify(handler).onSelection(any(EntitySelectedEvent.class));
		//verify clearing the state clears the selection handler
		entityTreeBrowser.clearState();
		assertNull(entityTreeBrowser.getEntitySelectedHandler());
		entityTreeBrowser.fireEntitySelectedEvent();
	}
	
	private EntityHeader createEntityHeader(String id, String name, String type, Long versionNumber) {
		EntityHeader header = new EntityHeader();
		header.setId(id);
		header.setName(name);
		header.setType(type);
		header.setVersionNumber(versionNumber);
		return header;
	}
	
	@Test
	public void testGetEntityQueryResultsFromHeaders() {
		List<EntityHeader> headers = new ArrayList<EntityHeader>();
		String id, name, type;
		Long versionNumber;
		id = "12";
		name = "project 1";
		type = "project";
		versionNumber = 1L;
		headers.add(createEntityHeader(id, name, type, versionNumber));
		
		EntityQueryResults results = entityTreeBrowser.getEntityQueryResultsFromHeaders(headers);
		assertEquals(1L, results.getTotalEntityCount().longValue());
		assertEquals(1, results.getEntities().size());
		EntityQueryResult result = results.getEntities().get(0);
		assertEquals(id, result.getId());
		assertEquals(name, result.getName());
		assertEquals(type, result.getEntityType());
		assertEquals(versionNumber, result.getVersionNumber());
		
		id = "24";
		name = "project 2";
		type = "file";
		versionNumber = 3L;
		headers.add(createEntityHeader(id, name, type, versionNumber));
		
		results = entityTreeBrowser.getEntityQueryResultsFromHeaders(headers);
		assertEquals(2L, results.getTotalEntityCount().longValue());
		assertEquals(2, results.getEntities().size());
		result = results.getEntities().get(1);
		assertEquals(id, result.getId());
		assertEquals(name, result.getName());
		assertEquals(type, result.getEntityType());
		assertEquals(versionNumber, result.getVersionNumber());
	}
}
