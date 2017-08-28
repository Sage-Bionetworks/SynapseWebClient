package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.ProjectListType;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowserView;
import org.sagebionetworks.web.shared.ProjectPagedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class MyEntitiesBrowserTest {
	MyEntitiesBrowser widget;
	MyEntitiesBrowserView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseClientAsync mockSynapseClient;
	JSONObjectAdapter jsonObjectAdapter;
	@Mock
	EntityTreeBrowser mockEntityTreeBrowser;
	@Mock
	EntityTreeBrowser mockFavoritesTreeBrowser;
	@Mock
	EntityTreeBrowser mockCurrentContextTreeBrowser;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;

	String currentUserId = "100042";
	ProjectPagedResults projects;
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		mockView = mock(MyEntitiesBrowserView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		widget = new MyEntitiesBrowser(mockView, mockAuthenticationController,
				mockGlobalApplicationState, mockSynapseClient,
				jsonObjectAdapter,
				mockSynapseJavascriptClient);
		mockEntityTreeBrowser = mock(EntityTreeBrowser.class);
		when(mockView.getEntityTreeBrowser()).thenReturn(mockEntityTreeBrowser);
		when(mockView.getFavoritesTreeBrowser()).thenReturn(mockFavoritesTreeBrowser);
		when(mockView.getCurrentContextTreeBrowser()).thenReturn(mockCurrentContextTreeBrowser);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(currentUserId);
		
		List<ProjectHeader> entities = new ArrayList<ProjectHeader>();
		projects = new ProjectPagedResults();
		ProjectHeader projectHeader1 = new ProjectHeader();
		projectHeader1.setId("syn1");
		ProjectHeader projectHeader2 = new ProjectHeader();
		projectHeader2.setId("syn2");
		projects.setResults(entities);
		projects.setTotalNumberOfResults(entities.size());
		AsyncMockStubber.callSuccessWith(projects).when(mockSynapseClient).getMyProjects(any(ProjectListType.class), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class), any(AsyncCallback.class));
	}

	@Test
	public void testLoadUserUpdateable() {
		widget.loadMoreUserUpdateable();
		verify(mockSynapseClient).getMyProjects(any(ProjectListType.class), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class), any(AsyncCallback.class));
		verify(mockView).addUpdatableEntities(anyList());
		verify(mockView).setIsMoreUpdatableEntities(false);
	}
	
	@Test
	public void testLoadUserUpdateableWithMore() {
		projects.setTotalNumberOfResults(MyEntitiesBrowser.PROJECT_LIMIT*2);
		widget.loadMoreUserUpdateable();
		verify(mockSynapseClient).getMyProjects(any(ProjectListType.class), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class), any(AsyncCallback.class));
		verify(mockView).addUpdatableEntities(anyList());
		verify(mockView).setIsMoreUpdatableEntities(true);
	}
	
	@Test
	public void testLoadUserUpdateableAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.loadMoreUserUpdateable();
		verify(mockView, never()).addUpdatableEntities(anyList());
	}
	
	@Test
	public void testLoadUserUpdateableFailure() {
		String errorMessage = "An error occurred.";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).getMyProjects(any(ProjectListType.class), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class), any(AsyncCallback.class));
		widget.loadMoreUserUpdateable();
		verify(mockView, never()).addUpdatableEntities(anyList());
		verify(mockView).showErrorMessage(errorMessage);
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
		
		verify(mockEntityTreeBrowser).clear();
		verify(mockView).setIsMoreUpdatableEntities(true);
		assertEquals(MyEntitiesBrowser.ZERO_OFFSET, widget.getUserUpdatableOffset());
		verify(mockSynapseClient, never()).getMyProjects(any(ProjectListType.class), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class), any(AsyncCallback.class));
		
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
		AsyncMockStubber.callSuccessWith(eb).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(new Synapse("syn123"));
		widget.loadCurrentContext();
		verify(mockView).setCurrentContextTabVisible(true);
		verify(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		verify(mockCurrentContextTreeBrowser).configure(anyList());
	}
	@Test
	public void testLoadContextSynapsePlaceFailure() {
		String errorMessage = "failure to load entity path";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(new Synapse("syn123"));
		widget.loadCurrentContext();
		verify(mockView).setCurrentContextTabVisible(true);
		verify(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
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
		EntityFilter filter = EntityFilter.ALL_BUT_LINK;
		widget.setEntityFilter(filter);
		verify(mockEntityTreeBrowser).setEntityFilter(filter);
		verify(mockFavoritesTreeBrowser).setEntityFilter(filter);
		verify(mockCurrentContextTreeBrowser).setEntityFilter(filter);
		assertEquals(s, widget.getCachedCurrentPlace());
		assertEquals(userId, widget.getCachedUserId());
	}
}
