package org.sagebionetworks.web.server.servlet;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.EntityBundle;
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
	EntityBundle mockBundle;
	String entityId;
	int usersMask;
	int groupsMask;
	int emptyMask;
	EntityBundleTransport bundle;

	@Before
	public void before() throws SynapseException {
		mockSynapse = mock(Synapse.class);
		mockSynapseProvider = Mockito.mock(SynapseProvider.class);
		mockUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		mockTokenProvider = Mockito.mock(TokenProvider.class);
		mockBundle = new EntityBundle();
		mockBundle.setUsers(new PaginatedResults<UserProfile>());
		mockBundle.setGroups(new PaginatedResults<UserGroup>());
		when(mockSynapse.getEntityBundle(anyString(), anyInt())).thenReturn(mockBundle);
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
	}
	
	@Test
	public void testGetUsersGroupsNoCache() throws RestServiceException, SynapseException {

		synapseClient.getEntityBundle(entityId, usersMask | groupsMask);
		
		// Users/groups request should have been propagated once
		verify(mockSynapse).getEntityBundle(anyString(), eq(usersMask | groupsMask));
	}
	
	
	@Test
	public void testGetUsersGroupsFreshCache() throws RestServiceException, SynapseException {
		try {
			synapseClient.getEntityBundle(entityId, usersMask | groupsMask);
		} catch (Exception e) {}
		try {
			synapseClient.getEntityBundle(entityId, usersMask | groupsMask);
		} catch (Exception e) {}
		
		// Users/groups request should have been propagated once
		verify(mockSynapse).getEntityBundle(anyString(), eq(usersMask | groupsMask));
		// Users/groups request should have been intercepted once
		verify(mockSynapse).getEntityBundle(anyString(), eq(0));
	}
	
	@Test
	public void testGetUsersGroupsStaleCache() throws RestServiceException, SynapseException {
		synapseClient.getEntityBundle(entityId, usersMask | groupsMask);
		// Wait for cache to go stale
		try {
			Thread.sleep(CACHE_TIMEOUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		synapseClient.getEntityBundle(entityId, usersMask | groupsMask);
		
		// Users/groups request should have been propagated twice
		verify(mockSynapse, times(2)).getEntityBundle(anyString(), eq(usersMask | groupsMask));
	}
}
