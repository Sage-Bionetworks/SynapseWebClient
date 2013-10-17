package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import org.sagebionetworks.web.client.widget.team.InviteWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class InviteWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	InviteWidgetView mockView;
	String teamId = "123";
	InviteWidget inviteWidget;
	AuthenticationController mockAuthenticationController;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(InviteWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		inviteWidget = new InviteWidget(mockView, mockSynapseClient, mockAuthenticationController, mockGlobalApplicationState);
		inviteWidget.configure(teamId);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSendInvitation() throws Exception {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).inviteMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		String principalId = "5";
		inviteWidget.sendInvitation(principalId, "you are invited!", "Wildcat");
		verify(mockSynapseClient).inviteMember(eq(principalId), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSendInvitationFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).inviteMember(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		String principalId = "5";
		inviteWidget.sendInvitation(principalId, "you are invited!", "Wildcat");
		verify(mockSynapseClient).inviteMember(eq(principalId), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
}
