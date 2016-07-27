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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.team.TeamBadgeView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the Team Badge widget.
 *
 */
public class TeamBadgeTest {

	SynapseClientAsync mockSynapseClient;
	TeamBadgeView mockView;
	TeamBadge badge;
	Team team;
	String principalId = "id1";
	int max = 10;
	@Mock
	AuthenticationController mockAuthController;
	String xsrfToken = "98208";
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		team = new Team();
		team.setName("name");
		team.setId(principalId);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockView = mock(TeamBadgeView.class);
		badge = new TeamBadge(mockView, mockSynapseClient, mockAuthController);
		when(mockAuthController.getCurrentXsrfToken()).thenReturn(xsrfToken);
	}
	
	@Test
	public void testConfigure(){
		badge.configure(team);		
		verify(mockView).setTeam(team, null, xsrfToken);
	}
	
	@Test
	public void testConfigureAsync() throws Exception {
		AsyncMockStubber.callSuccessWith(team).when(mockSynapseClient).getTeam(eq(principalId), any(AsyncCallback.class));
		badge.setMaxNameLength(max);
		badge.configure(team);
		verify(mockView).setTeam(team, max, xsrfToken);
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
		verify(mockView).setTeam(team, max, xsrfToken);		
	}
	
	@Test
	public void testConfigureNullPrincipalId() throws Exception {
		badge.configure((String)null);
		verify(mockView, never()).setTeam(any(Team.class), anyInt(), anyString());
	}
	
	@Test
	public void testConfigureEmptyPrincipalId() throws Exception {
		badge.configure("");
		verify(mockView, never()).setTeam(any(Team.class), anyInt(), anyString());
	}
	
	@Test
	public void testSetNotificationValue() throws Exception {
		//pass-through test
		String notificationValue = "98";
		badge.setNotificationValue(notificationValue);
		verify(mockView).setRequestCount(eq(notificationValue));
	}
	
}
