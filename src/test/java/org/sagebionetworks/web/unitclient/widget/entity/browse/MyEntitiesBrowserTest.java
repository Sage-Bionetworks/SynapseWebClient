package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowserView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class MyEntitiesBrowserTest {
	EntityQueryResults searchResults;
	MyEntitiesBrowser widget;
	MyEntitiesBrowserView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseClientAsync mockSynapseClient;
	JSONObjectAdapter jsonObjectAdapter;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	@Mock
	EntityTreeBrowser mockEntityTreeBrowser;
	@Mock
	EntityTreeBrowser mockFavoritesTreeBrowser;
	@Mock
	EntityTreeBrowser mockCurrentContextTreeBrowser;
	
	String currentUserId = "100042";
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		mockView = mock(MyEntitiesBrowserView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		widget = new MyEntitiesBrowser(mockView, mockAuthenticationController,
				mockGlobalApplicationState, mockSynapseClient,
				jsonObjectAdapter, adapterFactory);
		mockEntityTreeBrowser = mock(EntityTreeBrowser.class);
		when(mockView.getEntityTreeBrowser()).thenReturn(mockEntityTreeBrowser);
		when(mockView.getFavoritesTreeBrowser()).thenReturn(mockFavoritesTreeBrowser);
		when(mockView.getCurrentContextTreeBrowser()).thenReturn(mockCurrentContextTreeBrowser);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(currentUserId);
		searchResults = new EntityQueryResults();
		List<EntityQueryResult> entities = new ArrayList<EntityQueryResult>();
		searchResults.setEntities(entities);

		AsyncMockStubber
			.callSuccessWith(searchResults).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class),
				any(AsyncCallback.class));
	}

	@Test
	public void testLoadUserUpdateable() {
		widget.loadUserUpdateable();
		verify(mockEntityTreeBrowser).clear();
		verify(mockSynapseClient).executeEntityQuery(any(EntityQuery.class),
				any(AsyncCallback.class));
		verify(mockView).setUpdatableEntities(anyList());
	}
	
	@Test
	public void testLoadUserUpdateableAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.loadUserUpdateable();
		verify(mockEntityTreeBrowser).clear();
		verify(mockView, never()).setUpdatableEntities(anyList());
	}
	
	@Test
	public void testLoadUserUpdateableFailure() {
		String errorMessage = "An error occurred.";
		AsyncMockStubber
		.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class),
			any(AsyncCallback.class));
		widget.loadUserUpdateable();
		verify(mockEntityTreeBrowser).clear();
		verify(mockView, never()).setUpdatableEntities(anyList());
		verify(mockView).showErrorMessage(errorMessage);
	}

	@Test
	public void testCreateGetMyProjectQuery() {
		EntityQuery query = widget.createMyProjectQuery();

		// verify sort
		assertEquals(EntityFieldName.name.name(), query.getSort()
				.getColumnName());
		assertEquals(SortDirection.ASC, query.getSort().getDirection());
		List<Condition> conditions = query.getConditions();
		assertEquals(1, conditions.size());
		assertEquals(EntityType.project, query.getFilterByType());
		assertEquals(MyEntitiesBrowser.PROJECT_LIMIT, query.getLimit());
		assertEquals(MyEntitiesBrowser.ZERO_OFFSET, query.getOffset());
	}
	
	@Test
	public void testIsSameContextNullPlaceAndToken() {
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(null);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(null);
		assertFalse(widget.isSameContext());
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(new Synapse("syn123"));
		assertFalse(widget.isSameContext());
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(null);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn("12345");
		assertFalse(widget.isSameContext());
	}
	
	@Test
	public void testContextChange() {
		//first, verify that updateContext does what we expect it to
		Synapse s = new Synapse("syn123");
		String userId = "12345";
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(s);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userId);
		assertNull(widget.getCachedCurrentPlace());
		assertNull(widget.getCachedUserId());
		widget.updateContext();
		assertEquals(s, widget.getCachedCurrentPlace());
		assertEquals(userId, widget.getCachedUserId());
		
		//test refresh when the context has not changed
		widget.refresh();
		//should have done nothing
		verifyZeroInteractions(mockSynapseClient);
		
		//test clearState() when context has not changed
		widget.clearState();
		verify(mockView).clearSelection();

		//now test refresh after a context change
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(new Synapse("different place"));
		widget.refresh();
		verify(mockSynapseClient).executeEntityQuery(any(EntityQuery.class),
				any(AsyncCallback.class));
		
		//test clearState() when context has changed
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(new Synapse("yet another different place"));
		widget.clearState();
		verify(mockView).clear();
	}
	
	@Test
	public void testLoadContextNotSynapsePlace() {
		//in case the entity finder is used outside of a Synapse place, we don't know how to figure out the current project
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(new Profile(""));
		widget.loadCurrentContext();
		verify(mockView).setCurrentContextTabVisible(false);
	}
	
	@Test
	public void testLoadContextSynapsePlaceSuccess() {
		EntityBundle eb = new EntityBundle();
		EntityPath path = new EntityPath();
		path.setPath(new ArrayList<EntityHeader>());
		eb.setPath(path);
		AsyncMockStubber.callSuccessWith(eb).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(new Synapse("syn123"));
		widget.loadCurrentContext();
		verify(mockView).setCurrentContextTabVisible(true);
		verify(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		verify(mockCurrentContextTreeBrowser).configure(anyList());
	}
	@Test
	public void testLoadContextSynapsePlaceFailure() {
		String errorMessage = "failure to load entity path";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(new Synapse("syn123"));
		widget.loadCurrentContext();
		verify(mockView).setCurrentContextTabVisible(true);
		verify(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(errorMessage);
	}
	
	@Test
	public void testSetEntityFilter() {
		Synapse s = new Synapse("syn123");
		String userId = "12345";
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(s);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userId);
		widget.updateContext();
		assertEquals(s, widget.getCachedCurrentPlace());
		assertEquals(userId, widget.getCachedUserId());

		//verify that context is updated when the filter is set.
		s = new Synapse("syn321");
		userId = "54321";
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(s);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userId);
		EntityFilter filter = EntityFilter.FILE;
		widget.setEntityFilter(filter);
		verify(mockEntityTreeBrowser).setEntityFilter(filter);
		verify(mockFavoritesTreeBrowser).setEntityFilter(filter);
		verify(mockCurrentContextTreeBrowser).setEntityFilter(filter);
		assertEquals(s, widget.getCachedCurrentPlace());
		assertEquals(userId, widget.getCachedUserId());
	}
}
