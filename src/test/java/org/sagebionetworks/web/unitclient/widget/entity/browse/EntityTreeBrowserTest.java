package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.assertEquals;
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

import org.gwtbootstrap3.client.ui.Button;
import org.junit.Before;
import org.junit.Ignore;
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
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItem;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityTreeBrowserTest {	
	EntityTreeBrowserView mockView;
	SynapseClientAsync mockSynapseClient; 
	AuthenticationController mockAuthenticationController;
	EntityTypeProvider mockEntityTypeProvider;
	GlobalApplicationState mockGlobalApplicationState;
	IconsImageBundle mockIconsImageBundle;
	PortalGinInjector mockInjector;
	AdapterFactory adapterFactory;
	EntityTreeBrowser entityTreeBrowser;
	EntityQueryResults searchResults;
	EntityTreeItem mockEntityTreeItem;
	MoreTreeItem mockMoreTreeItem;
	String parentId;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(EntityTreeBrowserView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);
		mockInjector = mock(PortalGinInjector.class);
		mockEntityTreeItem = mock(EntityTreeItem.class);
		mockMoreTreeItem = mock(MoreTreeItem.class);
		adapterFactory = new AdapterFactoryImpl();
		entityTreeBrowser = new EntityTreeBrowser(mockInjector, mockView, mockSynapseClient, mockAuthenticationController, mockEntityTypeProvider, mockGlobalApplicationState, mockIconsImageBundle, adapterFactory);
		verify(mockView).setPresenter(entityTreeBrowser);
		reset(mockView);
		parentId = "testParentId";
		searchResults = new EntityQueryResults();
		List<EntityQueryResult> entities = new ArrayList<EntityQueryResult>();
		searchResults.setEntities(entities);
		when(mockInjector.getEntityTreeItemWidget()).thenReturn(mockEntityTreeItem);
		mockMoreTreeItem.type = MoreTreeItem.MORE_TYPE.FOLDER;
		EntityHeader header = new EntityHeader();
		header.setId(parentId);
		when(mockEntityTreeItem.getHeader()).thenReturn(header);
		when(mockInjector.getMoreTreeWidget()).thenReturn(mockMoreTreeItem);
		AsyncMockStubber.callSuccessWith(searchResults).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
	}
	
	// Make better examples/tests for parent != null.
	@Test
	public void testGetFolderChildren() {
		AsyncCallback<List<EntityHeader>> mockCallback = mock(AsyncCallback.class);
		entityTreeBrowser.getFolderChildren("123", null, 0);
		ArgumentCaptor<EntityQuery> captor = ArgumentCaptor.forClass(EntityQuery.class);
		verify(mockSynapseClient, times(2)).executeEntityQuery(captor.capture(), any(AsyncCallback.class));
		List<EntityQuery> queries = captor.getAllValues();
		assertEquals(EntityType.folder, queries.get(0).getFilterByType());
		assertEquals(EntityType.file, queries.get(1).getFilterByType());
	}
	
	@Test
	public void testCreateGetChildrenQuery() {
		String parentId = "9";
		EntityQuery query = entityTreeBrowser.createGetChildrenQuery(parentId, 0, EntityType.folder);
		
		//verify sort
		assertEquals(EntityFieldName.name.name(), query.getSort().getColumnName());
		assertEquals(SortDirection.ASC, query.getSort().getDirection());
		List<Condition> conditions = query.getConditions();
		assertEquals(1, conditions.size());
		assertEquals(EntityType.folder, query.getFilterByType());
	}	
	
	// Look into better race condition case
	@Test
	public void testGetFolderChildrenRaceCondition() {
		mockSynapseClient = mock(SynapseClientAsync.class);
		AsyncCallback<List<EntityHeader>> mockCallback = mock(AsyncCallback.class);
		entityTreeBrowser = new EntityTreeBrowser(mockInjector, mockView, mockSynapseClient, mockAuthenticationController, mockEntityTypeProvider, mockGlobalApplicationState, mockIconsImageBundle, adapterFactory);
		entityTreeBrowser.getFolderChildren("123", null, 0);
		//capture the servlet call
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		verify(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), captor.capture());
		//before invoking asynccallback.success, set the current entity id to something else (simulating that the user 
		//has selected a different folder while this was still processing)
		captor.getValue().onSuccess(searchResults);
		verify(mockCallback, never()).onSuccess(anyList());
	}
	
	@Test
	public void testMoreButtonRootLevel() {
		long maxLim = entityTreeBrowser.getMaxLimit();
		setQueryResults(2*maxLim, 0, maxLim);
		entityTreeBrowser.getFolderChildren(parentId, null, 0);
		//  Creates the limited number of entity items
		verify(mockView, times((int)entityTreeBrowser.getMaxLimit())).insertRootEntityTreeItem(any(EntityTreeItem.class), Mockito.anyLong());
		// Calls once for folders, once for files.
		verify(mockView, times(2)).placeRootMoreFoldersTreeItem(any(MoreTreeItem.class), Mockito.eq(parentId), Mockito.eq(maxLim));
	}
	
	// Taken care of by expandTreeItemOnOpen
	@Test
	public void testMoreButtonChildLevel() {
		long maxLim = entityTreeBrowser.getMaxLimit();
		setQueryResults(2*maxLim, 0, maxLim);
		entityTreeBrowser.getFolderChildren(parentId, mockEntityTreeItem, 0);
		//  Creates the limited number of entity items
		verify(mockView, times((int)maxLim)).insertChildEntityTreeItem(any(EntityTreeItem.class), Mockito.eq(mockEntityTreeItem), Mockito.anyLong());
		// Calls once for folders, once for files.
		verify(mockView, times(2)).placeChildMoreFoldersTreeItem(any(MoreTreeItem.class), Mockito.eq(mockEntityTreeItem), Mockito.eq(maxLim));
	}
	
	@Test
	public void testExpandTreeItemMoreButton() {
		long maxLim = entityTreeBrowser.getMaxLimit();
		setQueryResults(2*maxLim, 0, maxLim);
		entityTreeBrowser.expandTreeItemOnOpen(mockEntityTreeItem);
		//  Creates the limited number of entity items
		verify(mockView, times((int)maxLim)).insertChildEntityTreeItem(any(EntityTreeItem.class), Mockito.eq(mockEntityTreeItem), Mockito.anyLong());
		// Calls once for folders, once for files.
		verify(mockView, times(2)).placeChildMoreFoldersTreeItem(any(MoreTreeItem.class), Mockito.eq(mockEntityTreeItem), Mockito.eq(maxLim));
	}
	
	@Test
	public void testGetMoreButtonRequery() {
		long maxLim = entityTreeBrowser.getMaxLimit();
		setQueryResults(2*maxLim, 0, maxLim);
		entityTreeBrowser.expandTreeItemOnOpen(mockEntityTreeItem);
		//  Adds the limited number of entity items
		verify(mockView, times((int)maxLim)).insertChildEntityTreeItem(any(EntityTreeItem.class), Mockito.eq(mockEntityTreeItem), Mockito.anyLong());
		// Calls once for folders, once for files.
		verify(mockView, times(2)).placeChildMoreFoldersTreeItem(any(MoreTreeItem.class), Mockito.eq(mockEntityTreeItem), Mockito.eq(maxLim));
		setQueryResults(2*maxLim, maxLim, 2*maxLim);
		// Verified that the more button is created with offset maxLim, so directly the moreButton should call getFolderChildren with that offset
		entityTreeBrowser.getFolderChildren(parentId, mockEntityTreeItem, maxLim);
		//  Adds the rest of the entity items
		verify(mockView, times((int)(2*maxLim))).insertChildEntityTreeItem(any(EntityTreeItem.class), Mockito.eq(mockEntityTreeItem), Mockito.anyLong());
		// Does not create any more "More (Entity)" buttons, which still should be at 2 times.
		verify(mockView, times(2)).placeChildMoreFoldersTreeItem(any(MoreTreeItem.class), Mockito.eq(mockEntityTreeItem), Mockito.eq(maxLim));
	}
	
	// Used to create overflow 
	private void setQueryResults(long totalEntries, long startIndex, long endIndex) {
		List<EntityQueryResult> entities = new ArrayList<EntityQueryResult>();
		for (int i = 0; i < totalEntries; i++) {
			EntityQueryResult res = new EntityQueryResult();
			res.setParentId(parentId);
			res.setId("testResultId" + i);
			entities.add(res);
		}
		searchResults.setEntities(entities.subList((int)startIndex, (int)endIndex));
		searchResults.setTotalEntityCount(totalEntries);
	}
}
