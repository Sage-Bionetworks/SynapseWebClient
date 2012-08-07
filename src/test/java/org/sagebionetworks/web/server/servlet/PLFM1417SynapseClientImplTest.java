package org.sagebionetworks.web.server.servlet;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

/**
 * This class tests functionality of the temporary fix for [PLFM-1417: Share
 * dialog fails to load]. The main Portal-side Synapse client should cache
 * users and groups requests to minimize loading times (and avoid timeouts).
 * 
 * @author bkng
 */
public class PLFM1417SynapseClientImplTest {
	
	private static final Long CACHE_TIMEOUT = 500L;
	
	SynapseClientImpl synapseClient;
	Synapse mockSynapse;
	SynapseProvider mockSynapseProvider;
	ServiceUrlProvider mockUrlProvider;
	TokenProvider mockTokenProvider;
	PaginatedResults<UserProfile> mockUsers;
	PaginatedResults<UserGroup> mockGroups;
	String entityId;
	int usersMask;
	int groupsMask;
	int emptyMask;
	EntityBundleTransport bundle;
	
	@SuppressWarnings("unchecked")
	@Before
	public void before() throws SynapseException {
		mockSynapse = mock(Synapse.class);
		mockSynapseProvider = Mockito.mock(SynapseProvider.class);
		mockUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		mockTokenProvider = Mockito.mock(TokenProvider.class);
		mockUsers = (PaginatedResults<UserProfile>) mock(PaginatedResults.class);
		mockGroups = (PaginatedResults<UserGroup>) mock(PaginatedResults.class);
		when(mockSynapse.getUsers(anyInt(), anyInt())).thenReturn(mockUsers);
		when(mockSynapse.getGroups(anyInt(), anyInt())).thenReturn(mockGroups);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		
		synapseClient = new SynapseClientImpl();
		synapseClient.setSynapseProvider(mockSynapseProvider);
		synapseClient.setTokenProvider(mockTokenProvider);
		synapseClient.setServiceUrlProvider(mockUrlProvider);
		synapseClient.setTimeout(CACHE_TIMEOUT);
		
		usersMask = EntityBundleTransport.USERS;
		groupsMask = EntityBundleTransport.GROUPS;
		emptyMask = 0;
		entityId = "syn123";
		bundle = null;
	}
	
	@Test
	public void testGetUsersGroupsNoCache() throws RestServiceException, SynapseException {
		bundle = synapseClient.getEntityBundle(entityId, usersMask | groupsMask);
		
		// Users/groups should have been fetched once
		verify(mockSynapse).getUsers(anyInt(), anyInt());
		verify(mockSynapse).getGroups(anyInt(), anyInt());
	}
	
	@Test
	public void testGetUsersOnly() throws RestServiceException, SynapseException {
		bundle = synapseClient.getEntityBundle(entityId, usersMask);
		verify(mockSynapse).getUsers(anyInt(), anyInt());
		verify(mockSynapse, never()).getGroups(anyInt(), anyInt());
	}
	
	@Test
	public void testGetGroupsOnly() throws RestServiceException, SynapseException {
		bundle = synapseClient.getEntityBundle(entityId, groupsMask);
		verify(mockSynapse, never()).getUsers(anyInt(), anyInt());
		verify(mockSynapse).getGroups(anyInt(), anyInt());
	}
	
	
	@Test
	public void testGetUsersGroupsFreshCache() throws RestServiceException, SynapseException {
		// Two users/groups fetch requests
		bundle = synapseClient.getEntityBundle(entityId, usersMask | groupsMask);
		bundle = synapseClient.getEntityBundle(entityId, usersMask | groupsMask);
		
		// Users/groups should have been fetched once
		verify(mockSynapse).getUsers(anyInt(), anyInt());
		verify(mockSynapse).getGroups(anyInt(), anyInt());
	}
	
	@Test
	public void testGetUsersGroupsCacheStale() throws RestServiceException, SynapseException {
		bundle = synapseClient.getEntityBundle(entityId, usersMask | groupsMask);
		// Wait for cache to go stale
		try {
			Thread.sleep(CACHE_TIMEOUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bundle = synapseClient.getEntityBundle(entityId, usersMask | groupsMask);
		
		// Users/groups should have been fetched twice
		verify(mockSynapse, times(2)).getUsers(anyInt(), anyInt());
		verify(mockSynapse, times(2)).getGroups(anyInt(), anyInt());
	}
}
