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
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItem;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsTreeItem;

@RunWith(MockitoJUnitRunner.class)
public class EntityTreeBrowserTest {
	public static final String TEST_RESULT_ID = "testResultId";
	@Mock
	EntityTreeBrowserView mockView;
	@Mock
	IconsImageBundle mockIconsImageBundle;
	@Mock
	PortalGinInjector mockInjector;
	AdapterFactory adapterFactory;
	EntityTreeBrowser entityTreeBrowser;
	@Mock
	EntityChildrenResponse mockResults;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	CallbackP<String> mockEntityClickedCallback;
	@Mock
	CallbackP<Boolean> mockIsEmptyCallback;
	List<EntityHeader> searchResults;
	@Mock
	EntityTreeItem mockEntityTreeItem;
	@Mock
	MoreTreeItem mockMoreTreeItem;
	@Mock
	IsTreeItem mockLoadingItem;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	@Captor
	ArgumentCaptor<EntityChildrenRequest> entityChildrenRequestCaptor;
	@Mock
	EntityHeader mockEntityHeader;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	Request mockRequest;
	@Captor
	ArgumentCaptor<String> stringCaptor;

	String parentId;

	@Before
	public void before() throws JSONObjectAdapterException {
		adapterFactory = new AdapterFactoryImpl();
		entityTreeBrowser = new EntityTreeBrowser(mockInjector, mockView, mockSynapseJavascriptClient, mockIconsImageBundle, adapterFactory, mockSynAlert);
		verify(mockView).setPresenter(entityTreeBrowser);
		reset(mockView);
		parentId = "testParentId";
		searchResults = new ArrayList<EntityHeader>();
		when(mockResults.getPage()).thenReturn(searchResults);

		when(mockInjector.getEntityTreeItemWidget()).thenReturn(mockEntityTreeItem);
		EntityHeader header = new EntityHeader();
		header.setId(parentId);
		header.setType(Folder.class.getName());

		when(mockEntityTreeItem.getHeader()).thenReturn(header);
		// when(mockView.appendLoading(any(EntityTreeItem.class))).thenReturn(mockLoadingItem);
		// when(mockView.insertLoading(any(EntityTreeItem.class),
		// Mockito.anyInt())).thenReturn(mockLoadingItem);
		Mockito.when(mockInjector.getMoreTreeWidget()).thenReturn(mockMoreTreeItem);
		AsyncMockStubber.callSuccessWith(mockResults).when(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		entityTreeBrowser.setIsEmptyCallback(mockIsEmptyCallback);
	}

	@Test
	public void testGetChildren() {
		String childEntityId = "syn98765";
		searchResults.add(mockEntityHeader);
		when(mockEntityHeader.getId()).thenReturn(childEntityId);
		when(mockEntityHeader.getType()).thenReturn(FileEntity.class.getName());

		entityTreeBrowser.setEntityClickedHandler(mockEntityClickedCallback);
		entityTreeBrowser.configure("123");

		verify(mockSynapseJavascriptClient).getEntityChildren(entityChildrenRequestCaptor.capture(), any(AsyncCallback.class));
		EntityChildrenRequest request = entityChildrenRequestCaptor.getValue();
		assertEquals("123", request.getParentId());
		assertNull(request.getNextPageToken());
		verify(mockEntityTreeItem).setClickHandler(clickHandlerCaptor.capture());
		verify(mockEntityClickedCallback, never()).invoke(anyString());
		assertEquals(EntityTreeBrowser.DEFAULT_SORT_BY, request.getSortBy());
		assertEquals(EntityTreeBrowser.DEFAULT_DIRECTION, request.getSortDirection());
		verify(mockView).clearSortUI();
		verify(mockIsEmptyCallback).invoke(false);

		// verify user selecting another sorting option resets the query, and changes the request sort
		// parameters
		entityTreeBrowser.onSort(SortBy.CREATED_ON, Direction.DESC);

		verify(mockView, times(2)).clear();
		verify(mockSynapseJavascriptClient, times(2)).getEntityChildren(entityChildrenRequestCaptor.capture(), any(AsyncCallback.class));
		request = entityChildrenRequestCaptor.getValue();
		assertEquals(SortBy.CREATED_ON, request.getSortBy());
		assertEquals(Direction.DESC, request.getSortDirection());
		verify(mockView).setSortUI(SortBy.CREATED_ON, Direction.DESC);

		assertNull(request.getNextPageToken());
		assertEquals("123", request.getParentId());

		clickHandlerCaptor.getValue().onClick(null);
		verify(mockEntityClickedCallback).invoke(childEntityId);
	}

	@Test
	public void testGetChildrenEmptyResult() {
		entityTreeBrowser.setEntityClickedHandler(mockEntityClickedCallback);
		entityTreeBrowser.configure("123");

		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));

		verify(mockIsEmptyCallback).invoke(true);
	}

	@Test
	public void testCreateGetChildrenQuery() {
		String parentId = "9";
		EntityChildrenRequest query = entityTreeBrowser.createGetEntityChildrenRequest(parentId, null);

		// verify sort
		assertEquals(SortBy.NAME, query.getSortBy());
		assertEquals(Direction.ASC, query.getSortDirection());
	}

	@Test
	public void testGetFolderChildrenRaceCondition() {
		mockSynapseJavascriptClient = mock(SynapseJavascriptClient.class);
		AsyncCallback<List<EntityHeader>> mockCallback = mock(AsyncCallback.class);
		entityTreeBrowser = new EntityTreeBrowser(mockInjector, mockView, mockSynapseJavascriptClient, mockIconsImageBundle, adapterFactory, mockSynAlert);
		entityTreeBrowser.getChildren("123", null, null);
		// capture the servlet call
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), captor.capture());
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
		verify(mockView, times(100)).appendRootEntityTreeItem(any(EntityTreeItem.class));
		verify(mockView).placeRootMoreTreeItem(any(MoreTreeItem.class), eq(parentId), eq(nextPageToken));
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
		verify(mockView, times(20)).appendChildEntityTreeItem(any(EntityTreeItem.class), Mockito.eq(mockEntityTreeItem));
		// Calls once for folders, once for files.
		verify(mockView).placeChildMoreTreeItem(any(MoreTreeItem.class), Mockito.eq(mockEntityTreeItem), eq(nextPageToken));
	}

	// Used to create test query results
	private void setQueryResults(long totalEntries) {
		for (int i = 0; i < totalEntries; i++) {
			EntityHeader res = new EntityHeader();
			res.setId(TEST_RESULT_ID + i);
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
		// set entity selected handler
		entityTreeBrowser.setEntitySelectedHandler(handler);
		assertEquals(handler, entityTreeBrowser.getEntitySelectedHandler());
		// verify firing a selection event
		entityTreeBrowser.fireEntitySelectedEvent();
		verify(handler).onSelection(any(EntitySelectedEvent.class));
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

	@Test
	public void testCopyToClipboard() {
		setQueryResults(10);

		String nextPageToken = "abc";
		when(mockResults.getNextPageToken()).thenReturn(nextPageToken);
		entityTreeBrowser.getChildren(parentId, null, null);

		entityTreeBrowser.copyIDsToClipboard();

		verify(mockView).copyToClipboard(stringCaptor.capture());
		String clipboardValue = stringCaptor.getValue();
		for (int i = 0; i < 10; i++) {
			assertTrue(clipboardValue.contains(TEST_RESULT_ID + i));
		}
	}

	@Test
	public void testReconfigureCancels() {
		// Do not test async response, only test the Request
		reset(mockSynapseJavascriptClient);
		when(mockSynapseJavascriptClient.getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class))).thenReturn(mockRequest);

		entityTreeBrowser.configure("123");

		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockRequest, never()).cancel();

		entityTreeBrowser.configure("1234");

		verify(mockRequest).cancel();
		verify(mockSynapseJavascriptClient, times(2)).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
	}
}
