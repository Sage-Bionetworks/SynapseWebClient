package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
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
	EntityTreeBrowser mockEntityTreeBrowser;
	String currentUserId = "100042";
	@Before
	public void before() {
		mockView = mock(MyEntitiesBrowserView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		widget = new MyEntitiesBrowser(mockView, mockAuthenticationController,
				mockGlobalApplicationState, mockSynapseClient,
				jsonObjectAdapter, adapterFactory);
		mockEntityTreeBrowser = mock(EntityTreeBrowser.class);
		when(mockView.getEntityTreeBrowser()).thenReturn(mockEntityTreeBrowser);
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
		verify(mockView).showLoading();
		verify(mockEntityTreeBrowser).clear();
		verify(mockSynapseClient).executeEntityQuery(any(EntityQuery.class),
				any(AsyncCallback.class));
		verify(mockView).setUpdatableEntities(anyList());
	}
	
	@Test
	public void testLoadUserUpdateableAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.loadUserUpdateable();
		verify(mockView).showLoading();
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
		verify(mockView).showLoading();
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

}
