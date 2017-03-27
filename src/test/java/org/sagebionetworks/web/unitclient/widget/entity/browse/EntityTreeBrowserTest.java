package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
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
	@Mock
	EntityChildrenResponse mockResults;
	List<EntityHeader> searchResults;
	
	EntityTreeItem mockEntityTreeItem;
	MoreTreeItem mockMoreTreeItem;
	IsTreeItem mockLoadingItem;
	String parentId;

	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
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
		searchResults = new ArrayList<EntityHeader>();
		when(mockResults.getPage()).thenReturn(searchResults);

		when(mockInjector.getEntityTreeItemWidget()).thenReturn(
				mockEntityTreeItem);
		EntityHeader header = new EntityHeader();
		header.setId(parentId);
		header.setType(Folder.class.getName());
		
		when(mockEntityTreeItem.getHeader()).thenReturn(header);
//		when(mockView.appendLoading(any(EntityTreeItem.class))).thenReturn(mockLoadingItem);
//		when(mockView.insertLoading(any(EntityTreeItem.class), Mockito.anyInt())).thenReturn(mockLoadingItem);
		Mockito.when(mockInjector.getMoreTreeWidget()).thenReturn(mockMoreTreeItem);
		AsyncMockStubber
				.callSuccessWith(mockResults)
				.when(mockSynapseClient)
				.getEntityChildren(any(EntityChildrenRequest.class),
						any(AsyncCallback.class));
	}

	@Test
	public void testGetChildren() {
		entityTreeBrowser.getChildren("123", null, null);
		ArgumentCaptor<EntityChildrenRequest> captor = ArgumentCaptor
				.forClass(EntityChildrenRequest.class);
		verify(mockSynapseClient).getEntityChildren(captor.capture(), any(AsyncCallback.class));
		EntityChildrenRequest request = captor.getValue();
		assertEquals("123", request.getParentId());
		assertNull(request.getNextPageToken());
	}

	@Test
	public void testCreateGetChildrenQuery() {
		String parentId = "9";
		EntityChildrenRequest query = entityTreeBrowser.createGetChildrenQuery(parentId, null);

		// verify sort
		assertEquals(SortBy.NAME, query.getSortBy());
		assertEquals(Direction.ASC, query.getSortDirection());
	}

	@Test
	public void testGetFolderChildrenRaceCondition() {
		mockSynapseClient = mock(SynapseClientAsync.class);
		AsyncCallback<List<EntityHeader>> mockCallback = mock(AsyncCallback.class);
		entityTreeBrowser = new EntityTreeBrowser(mockInjector, mockView,
				mockSynapseClient, mockAuthenticationController, mockGlobalApplicationState,
				mockIconsImageBundle, adapterFactory);
		entityTreeBrowser.getChildren("123", null, null);
		// capture the servlet call
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor
				.forClass(AsyncCallback.class);
		verify(mockSynapseClient).getEntityChildren(any(EntityChildrenRequest.class), captor.capture());
		// before invoking asynccallback.success, set the current entity id to
		// something else (simulating that the user
		// has selected a different folder while this was still processing)
		captor.getValue().onSuccess(mockResults);
		verify(mockCallback, never()).onSuccess(anyList());
	}

	@Test
	public void testMoreButtonRootLevel() {
		setQueryResults(100);
		String nextPageToken = "abc";
		when(mockResults.getNextPageToken()).thenReturn(nextPageToken);
		entityTreeBrowser.getChildren(parentId, null, null);
		// Creates the limited number of entity items
		// 100 links, and 100 files
		verify(mockView, times(100))
				.appendRootEntityTreeItem(any(EntityTreeItem.class));
		verify(mockView).placeRootMoreTreeItem(
				any(MoreTreeItem.class), eq(parentId), eq(nextPageToken));
	}

	// Taken care of by expandTreeItemOnOpen
	@Test
	public void testMoreButtonChildLevel() {
		setQueryResults(1);
		String nextPageToken = "abc";
		when(mockResults.getNextPageToken()).thenReturn(nextPageToken);
		entityTreeBrowser.getChildren(parentId, mockEntityTreeItem, null);
		// Creates the limited number of entity items
		verify(mockView).appendChildEntityTreeItem(any(EntityTreeItem.class), eq(mockEntityTreeItem));
		// Calls once for folders, once for files.
		verify(mockView).placeChildMoreTreeItem(any(MoreTreeItem.class), Mockito.eq(mockEntityTreeItem), anyString());
	}

	@Test
	public void testGetMoreButtonRequery() {
		setQueryResults(20);
		String nextPageToken = "abc";
		when(mockResults.getNextPageToken()).thenReturn(nextPageToken);
		entityTreeBrowser.getChildren(parentId, mockEntityTreeItem, null);
		// Adds the limited number of entity items
		verify(mockView, times(20)).appendChildEntityTreeItem(
				any(EntityTreeItem.class), Mockito.eq(mockEntityTreeItem));
		// Calls once for folders, once for files.
		verify(mockView).placeChildMoreTreeItem(
				any(MoreTreeItem.class), Mockito.eq(mockEntityTreeItem),
				eq(nextPageToken));
	}

	// Used to create test query results
	private void setQueryResults(long totalEntries) {
		for (int i = 0; i < totalEntries; i++) {
			EntityHeader res = new EntityHeader();
			res.setId("testResultId" + i);
			res.setType(Folder.class.getName());
			searchResults.add(res);
		}
	}
	
	@Test
	public void testConfigure() {
		List<EntityHeader> headers = new ArrayList<EntityHeader>();
		EntityHeader header = new EntityHeader();
		header.setType(Project.class.getName());
		headers.add(header);
		
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
		type = Project.class.getName();
		versionNumber = 1L;
		headers.add(createEntityHeader(id, name, type, versionNumber));
		
		EntityQueryResults results = entityTreeBrowser.getEntityQueryResultsFromHeaders(headers);
		assertEquals(1L, results.getTotalEntityCount().longValue());
		assertEquals(1, results.getEntities().size());
		EntityQueryResult result = results.getEntities().get(0);
		assertEquals(id, result.getId());
		assertEquals(name, result.getName());
		assertEquals(EntityType.project.name(), result.getEntityType());
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
	
	@Test
	public void testIsExpandable() {
		EntityHeader result = new EntityHeader();
		result.setType(Folder.class.getName());
		assertTrue(entityTreeBrowser.isExpandable(result));
		result.setType(Project.class.getName());
		assertTrue(entityTreeBrowser.isExpandable(result));
		result.setType(FileEntity.class.getName());
		assertFalse(entityTreeBrowser.isExpandable(result));
	}
	
}
