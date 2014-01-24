package org.sagebionetworks.web.unitclient.widget.user;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
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

	NodeModelCreator mockNodeModelCreator;
	SynapseClientAsync mockSynapseClient;
	UserBadgeView mockView;
	UserBadge userBadge;
	UserProfile profile;		
	String principalId = "id1";
	int max=10;
	
	@Before
	public void before() throws JSONObjectAdapterException{
		profile = new UserProfile();
		profile.setDisplayName("name");
		profile.setOwnerId(principalId);

		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockView = mock(UserBadgeView.class);
		userBadge = new UserBadge(mockView, mockSynapseClient, mockNodeModelCreator);
	}
	
	@Test
	public void testConfigureStatic(){
		userBadge.configure(profile);		
		verify(mockView).setProfile(profile, null);
	}
	
	@Test
	public void testConfigureAsync() throws Exception {
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(UserProfile.class))).thenReturn(profile);
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
