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

import com.google.gwt.event.dom.client.ClickHandler;
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
	@Mock
	ClickHandler mockClickHandler;
	
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
		verify(mockView).setTeam(team, null, xsrfToken, null);
	}
	

	@Test
	public void testConfigureAsyncCustomClickHandler() throws Exception {
		AsyncMockStubber.callSuccessWith(team).when(mockSynapseClient).getTeam(eq(principalId), any(AsyncCallback.class));
		badge.setMaxNameLength(max);
		badge.configure(principalId, mockClickHandler);
		verify(mockView).setTeam(team, max, xsrfToken, mockClickHandler);
	}
	
	@Test
	public void testConfigureAsync() throws Exception {
		AsyncMockStubber.callSuccessWith(team).when(mockSynapseClient).getTeam(eq(principalId), any(AsyncCallback.class));
		badge.setMaxNameLength(max);
		badge.configure(team);
		verify(mockView).setTeam(team, max, xsrfToken, null);
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
		verify(mockView).setTeam(team, max, xsrfToken, null);		
	}
	
	@Test
	public void testConfigureNullPrincipalId() throws Exception {
		badge.configure((String)null);
		verify(mockView, never()).setTeam(any(Team.class), anyInt(), anyString(), any(ClickHandler.class));
	}
	
	@Test
	public void testConfigureEmptyPrincipalId() throws Exception {
		badge.configure("");
		verify(mockView, never()).setTeam(any(Team.class), anyInt(), anyString(), any(ClickHandler.class));
	}
	
	@Test
	public void testSetNotificationValue() throws Exception {
		//pass-through test
		String notificationValue = "98";
		badge.setNotificationValue(notificationValue);
		verify(mockView).setRequestCount(eq(notificationValue));
	}
	
	@Test
	public void testNewWindow() throws Exception {
		badge.setOpenNewWindow(true);
		verify(mockView).setTarget("_blank");
	}
	@Test
	public void testSameWindow() throws Exception {
		badge.setOpenNewWindow(false);
		verify(mockView).setTarget("");
	}
	
}
