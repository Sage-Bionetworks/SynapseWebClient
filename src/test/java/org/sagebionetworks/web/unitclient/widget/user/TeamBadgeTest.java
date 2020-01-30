package org.sagebionetworks.web.unitclient.widget.user;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.widget.asynch.TeamAsyncHandler;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.team.TeamBadgeView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the Team Badge widget.
 *
 */
public class TeamBadgeTest {

	@Mock
	TeamBadgeView mockView;
	TeamBadge badge;
	Team team;
	String principalId = "id1";
	int max = 10;
	@Mock
	ClickHandler mockClickHandler;
	@Mock
	TeamAsyncHandler mockTeamAsyncHandler;
	@Mock
	SynapseJSNIUtils mockSnapseJsniUtils;
	@Mock
	SynapseProperties mockSynapseProperties;
	public static final String TEAM_ICON_URL = "http://team.icon.png";
	public static final String PUBLIC_USER_ID = "222";
	public static final String AUTHENTICATED_USERS_GROUP_ID = "444";

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		team = new Team();
		team.setName("name");
		team.setId(principalId);
		when(mockSnapseJsniUtils.getFileHandleAssociationUrl(anyString(), any(FileHandleAssociateType.class), anyString())).thenReturn(TEAM_ICON_URL);
		when(mockSynapseProperties.getSynapseProperty(WebConstants.PUBLIC_ACL_PRINCIPAL_ID)).thenReturn(PUBLIC_USER_ID);
		when(mockSynapseProperties.getSynapseProperty(WebConstants.AUTHENTICATED_ACL_PRINCIPAL_ID)).thenReturn(AUTHENTICATED_USERS_GROUP_ID);
		badge = new TeamBadge(mockView, mockTeamAsyncHandler, mockSynapseProperties, mockSnapseJsniUtils);

	}

	@Test
	public void testConfigure() {
		badge.configure(team);
		verify(mockView).setTeam(team, null, TEAM_ICON_URL, null);
	}


	@Test
	public void testConfigureAsyncCustomClickHandler() throws Exception {
		AsyncMockStubber.callSuccessWith(team).when(mockTeamAsyncHandler).getTeam(eq(principalId), any(AsyncCallback.class));
		badge.setMaxNameLength(max);
		badge.configure(principalId, mockClickHandler);
		verify(mockView).setTeam(team, max, TEAM_ICON_URL, mockClickHandler);
	}

	@Test
	public void testConfigureAsync() throws Exception {
		AsyncMockStubber.callSuccessWith(team).when(mockTeamAsyncHandler).getTeam(eq(principalId), any(AsyncCallback.class));
		badge.setMaxNameLength(max);
		badge.configure(team);
		verify(mockView).setTeam(team, max, TEAM_ICON_URL, null);
	}

	@Test
	public void testConfigureAsyncFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockTeamAsyncHandler).getTeam(eq(principalId), any(AsyncCallback.class));
		badge.configure(principalId);
		verify(mockView).showLoadError(principalId);
	}

	@Test
	public void testSetNameLength() {
		badge.setMaxNameLength(max);
		badge.configure(team);
		verify(mockView).setTeam(team, max, TEAM_ICON_URL, null);
	}

	@Test
	public void testConfigureNullPrincipalId() throws Exception {
		badge.configure((String) null);
		verify(mockView, never()).setTeam(any(Team.class), anyInt(), anyString(), any(ClickHandler.class));
	}

	@Test
	public void testConfigureEmptyPrincipalId() throws Exception {
		badge.configure("");
		verify(mockView, never()).setTeam(any(Team.class), anyInt(), anyString(), any(ClickHandler.class));
	}

	@Test
	public void testSetNotificationValue() throws Exception {
		// pass-through test
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

	@Test
	public void testPublicUserGroup() {
		badge.configure(PUBLIC_USER_ID);
		verify(mockView).setTeamWithoutLink(TeamBadge.PUBLIC_GROUP_NAME, true);
	}

	@Test
	public void testAuthenticatedUserGroup() {
		badge.configure(AUTHENTICATED_USERS_GROUP_ID);
		verify(mockView).setTeamWithoutLink(TeamBadge.AUTHENTICATED_USERS_GROUP_NAME, true);
	}
}
