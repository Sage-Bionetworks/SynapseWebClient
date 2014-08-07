package org.sagebionetworks.web.unitclient.widget.user;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.user.UserBadgeView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the Summary widget.
 * @author dburdick
 *
 */
public class UserBadgeTest {

	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	SynapseClientAsync mockSynapseClient;
	UserBadgeView mockView;
	UserBadge userBadge;
	UserProfile profile;
	String profileJson;
	ClientCache mockCache;
	String principalId = "id1";
	int max=10;
	
	@Before
	public void before() throws JSONObjectAdapterException{
		profile = new UserProfile();
		profile.setDisplayName("name");
		profile.setOwnerId(principalId);
		profileJson = profile.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockView = mock(UserBadgeView.class);
		mockCache = mock(ClientCache.class);
		userBadge = new UserBadge(mockView, mockSynapseClient, adapterFactory, mockCache);
	}
	
	@Test
	public void testConfigureStatic(){
		userBadge.configure(profile);		
		verify(mockView).setProfile(profile, null);
	}
	
	@Test
	public void testConfigureAsync() throws Exception {
		AsyncMockStubber.callSuccessWith(profileJson).when(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));
		profile.setDisplayName("name");
		userBadge.setMaxNameLength(max);
		userBadge.configure(profile);
		verify(mockView).setProfile(profile, max);
	}
	
	@Test
	public void testConfigureAsyncFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));		
		profile.setDisplayName("name");
		userBadge.configure(principalId);
		verify(mockView).showLoadError(principalId);
	}
	
	@Test
	public void testConfigureFromCache() throws Exception {
		AsyncMockStubber.callSuccessWith(profileJson).when(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));
		when(mockCache.get(anyString())).thenReturn(profileJson);
		profile.setDisplayName("name");
		userBadge.setMaxNameLength(max);
		userBadge.configure(principalId);
		verify(mockView).setProfile(profile, max);
		//did not use the synapse client, used cache instead
		verify(mockSynapseClient, never()).getUserProfile(anyString(), any(AsyncCallback.class));
	}
		
	@Test
	public void testSetNameLength() {
		userBadge.setMaxNameLength(max);
		userBadge.configure(profile);		
		verify(mockView).setProfile(profile, max);		
	}
	
	@Test
	public void testConfigureNullPrincipalId() throws Exception {
		userBadge.configure((String)null);
		verify(mockView, never()).setProfile(any(UserProfile.class), anyInt());
	}
	
	@Test
	public void testConfigureEmptyPrincipalId() throws Exception {
		userBadge.configure("");
		verify(mockView, never()).setProfile(any(UserProfile.class), anyInt());
	}
}
