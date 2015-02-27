package org.sagebionetworks.web.unitclient.widget.user;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
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
	GlobalApplicationState mockGlobalApplicationState;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	UserBadgeView mockView;
	UserBadge userBadge;
	UserProfile profile;
	
	ClientCache mockCache;
	String principalId = "id1";
	int max=10;
	String displayName;
	
	@Before
	public void before(){
		profile = new UserProfile();
		profile.setFirstName("John");
		profile.setLastName("Doe");
		profile.setUserName("doeboy");
		displayName = DisplayUtils.getDisplayName(profile);
		profile.setOwnerId(principalId);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockView = mock(UserBadgeView.class);
		mockCache = mock(ClientCache.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		userBadge = new UserBadge(mockView, mockSynapseClient, mockGlobalApplicationState, mockSynapseJSNIUtils);
	}
	
	@Test
	public void testConfigureStatic(){
		userBadge.configure(profile);		
		verify(mockView).setDisplayName(displayName, displayName);
	}
	
	@Test
	public void testConfigureAsync() throws Exception {
		AsyncMockStubber.callSuccessWith(profile).when(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));
		userBadge.setMaxNameLength(max);
		userBadge.configure(profile);
		verify(mockView).setDisplayName(eq(displayName), anyString());
	}
	
	@Test
	public void testConfigureAsyncFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));		
		profile.setDisplayName("name");
		userBadge.configure(principalId);
		verify(mockView).showLoadError(principalId);
	}
	
	@Test
	public void testConfigureNullPrincipalId() throws Exception {
		userBadge.configure((String)null);
		verify(mockView).showLoadError(anyString());
	}
	
	@Test
	public void testConfigureEmptyPrincipalId() throws Exception {
		userBadge.configure("");
		verify(mockView).showLoadError(anyString());
	}
}
