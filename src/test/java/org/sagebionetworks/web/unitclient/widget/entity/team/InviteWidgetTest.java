package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import org.sagebionetworks.web.client.widget.team.InviteWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.entity.EvaluationSubmitterTest;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class InviteWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	InviteWidgetView mockView;
	String teamId = "123";
	InviteWidget inviteWidget;
	AuthenticationController mockAuthenticationController;
	Callback mockRefreshCallback;
	GWTWrapper mockGWTWrapper;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(InviteWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGWTWrapper = mock(GWTWrapper.class);
		inviteWidget = new InviteWidget(mockView, mockSynapseClient, mockAuthenticationController, mockGlobalApplicationState, mockGWTWrapper);
		mockRefreshCallback = mock(Callback.class);
		inviteWidget.configure(teamId, mockRefreshCallback);
		when(mockGWTWrapper.getHostPageBaseURL()).thenReturn(EvaluationSubmitterTest.HOST_PAGE_URL);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSendInvitation() throws Exception {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).inviteMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		String principalId = "5";
		inviteWidget.sendInvitation(principalId, "you are invited!", "Wildcat");
		verify(mockSynapseClient).inviteMember(eq(principalId), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockRefreshCallback).invoke();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSendInvitationFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).inviteMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		String principalId = "5";
		inviteWidget.sendInvitation(principalId, "you are invited!", "Wildcat");
		verify(mockSynapseClient).inviteMember(eq(principalId), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
}
