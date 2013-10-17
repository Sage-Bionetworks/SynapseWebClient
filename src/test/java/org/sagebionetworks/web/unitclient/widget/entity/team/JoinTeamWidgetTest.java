package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class JoinTeamWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	JoinTeamWidgetView mockView;
	String teamId = "123";
	JoinTeamWidget joinWidget;
	AuthenticationController mockAuthenticationController;
	Callback mockTeamUpdatedCallback;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(JoinTeamWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockTeamUpdatedCallback = mock(Callback.class);
		joinWidget = new JoinTeamWidget(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController);
		TeamMembershipStatus status = new TeamMembershipStatus();
		status.setHasOpenInvitation(false);
		status.setCanJoin(false);
		status.setHasOpenRequest(false);
		status.setIsMember(false);
		joinWidget.configure(teamId, status, mockTeamUpdatedCallback);
		
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteOpenMembershipRequests(anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), any(AsyncCallback.class));
	}
	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testDeleteAllJoinRequests() throws Exception {
//		joinWidget.deleteAllJoinRequests();
//		verify(mockSynapseClient).deleteOpenMembershipRequests(anyString(), anyString(), any(AsyncCallback.class));
//		verify(mockView).showInfo(anyString(), anyString());
//		verify(mockTeamUpdatedCallback).invoke();
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testDeleteAllJoinRequestsFailure() throws Exception {
//		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).deleteOpenMembershipRequests(anyString(), anyString(), any(AsyncCallback.class));
//		joinWidget.deleteAllJoinRequests();
//		verify(mockSynapseClient).deleteOpenMembershipRequests(anyString(), anyString(), any(AsyncCallback.class));
//		verify(mockView).showErrorMessage(anyString());
//	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSendJoinRequest() throws Exception {
		joinWidget.sendJoinRequest("Please join", false);
		verify(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockTeamUpdatedCallback).invoke();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSendJoinRequestFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).requestMembership(anyString(), anyString(),anyString(), any(AsyncCallback.class));
		joinWidget.sendJoinRequest("", false);
		verify(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
}
