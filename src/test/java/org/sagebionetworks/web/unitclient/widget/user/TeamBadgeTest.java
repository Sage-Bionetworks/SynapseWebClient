package org.sagebionetworks.web.unitclient.widget.user;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.team.TeamBadgeView;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.user.UserBadgeView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the Team Badge widget.
 *
 */
public class TeamBadgeTest {

	NodeModelCreator mockNodeModelCreator;
	SynapseClientAsync mockSynapseClient;
	TeamBadgeView mockView;
	TeamBadge badge;
	Team team;
	String principalId = "id1";
	int max = 10;
	
	@Before
	public void before() throws JSONObjectAdapterException{
		team = new Team();
		team.setName("name");
		team.setId(principalId);

		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockView = mock(TeamBadgeView.class);
		badge = new TeamBadge(mockView, mockSynapseClient, mockNodeModelCreator);
	}
	
	@Test
	public void testConfigure(){
		badge.configure(team);		
		verify(mockView).setTeam(team, null);
	}
	
	@Test
	public void testConfigureAsync() throws Exception {
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getTeam(eq(principalId), any(AsyncCallback.class));
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(Team.class))).thenReturn(team);
		badge.setMaxNameLength(max);
		badge.configure(team);
		verify(mockView).setTeam(team, max);
	}
	
	@Test
	public void testConfigureAsyncFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getTeam(eq(principalId), any(AsyncCallback.class));		
		badge.configure(principalId);
		verify(mockView).showLoadError(principalId);
	}
	
	@Test
	public void testSetNameLength() {
		badge.setMaxNameLength(max);
		badge.configure(team);		
		verify(mockView).setTeam(eq(team), eq(max));		
	}
	
	@Test
	public void testConfigureNullPrincipalId() throws Exception {
		badge.configure((String)null);
		verify(mockView, never()).setTeam(any(Team.class), anyInt());
	}
	
	@Test
	public void testConfigureEmptyPrincipalId() throws Exception {
		badge.configure("");
		verify(mockView, never()).setTeam(any(Team.class), anyInt());
	}
	
	@Test
	public void testSetNotificationValue() throws Exception {
		//pass-through test
		String notificationValue = "98";
		badge.setNotificationValue(notificationValue);
		verify(mockView).setRequestCount(eq(notificationValue));
	}
	
}
