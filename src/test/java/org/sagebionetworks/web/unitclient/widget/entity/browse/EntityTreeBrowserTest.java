package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserView;
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
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(EntityTreeBrowserView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);
		mockInjector = mock(PortalGinInjector.class);
		adapterFactory = new AdapterFactoryImpl();
		// Injector?
		entityTreeBrowser = new EntityTreeBrowser(mockInjector, mockView, mockSynapseClient, mockAuthenticationController, mockEntityTypeProvider, mockGlobalApplicationState, mockIconsImageBundle, adapterFactory);
		verify(mockView).setPresenter(entityTreeBrowser);
		reset(mockView);
		searchResults = new EntityQueryResults();
		List<EntityQueryResult> entities = new ArrayList<EntityQueryResult>();
		searchResults.setEntities(entities);
	}
	// Make better examples/tests for parent != null.
	@Test
	public void testGetFolderChildren() {
		AsyncMockStubber.callSuccessWith(searchResults).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		AsyncCallback<List<EntityHeader>> mockCallback = mock(AsyncCallback.class);
		entityTreeBrowser.getFolderChildren("123", null, 0);
		
		ArgumentCaptor<EntityQuery> captor = ArgumentCaptor.forClass(EntityQuery.class);
		verify(mockSynapseClient, times(2)).executeEntityQuery(captor.capture(), any(AsyncCallback.class));
		List<EntityQuery> queries = captor.getAllValues();
		assertEquals(EntityType.folder, queries.get(0).getFilterByType());
		assertEquals(EntityType.file, queries.get(1).getFilterByType());
	}
	
	// Look into better race condition case
	@Test
	public void testGetFolderChildrenRaceCondition() {
		AsyncCallback<List<EntityHeader>> mockCallback = mock(AsyncCallback.class);
		entityTreeBrowser.getFolderChildren("123", null, 0);
		//capture the servlet call
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		verify(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), captor.capture());
		//before invoking asynccallback.success, set the current entity id to something else (simulating that the user has selected a different folder while this was still processing)
		entityTreeBrowser.setCurrentFolderChildrenEntityId("456");
		captor.getValue().onSuccess(searchResults);
		verify(mockCallback, never()).onSuccess(anyList());
	}

	@Test
	public void testCreateGetChildrenQuery() {
		String parentId = "9";
		EntityQuery query = entityTreeBrowser.createGetChildrenQuery(parentId, EntityType.folder);
		
		//verify sort
		assertEquals(EntityFieldName.name.name(), query.getSort().getColumnName());
		assertEquals(SortDirection.ASC, query.getSort().getDirection());
		List<Condition> conditions = query.getConditions();
		assertEquals(1, conditions.size());
		assertEquals(EntityType.folder, query.getFilterByType());
	}
}
