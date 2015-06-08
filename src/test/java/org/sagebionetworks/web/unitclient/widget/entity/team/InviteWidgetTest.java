package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracle.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import org.sagebionetworks.web.client.widget.team.InviteWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.entity.EvaluationSubmitterTest;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class InviteWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	InviteWidgetView mockView;
	SynapseAlert mockSynAlert;
	UserGroupSuggestBox mockSuggestBox;
	SynapseJSNIUtils mockJSNIUtils;
	Team mockTeam;
	String teamId = "123";
	String userId = "testId";
	InviteWidget inviteWidget;
	UserGroupHeader mockHeader;
	UserGroupSuggestion mockSuggestion;
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
		mockSynAlert = mock(SynapseAlert.class);
		mockSuggestBox = mock(UserGroupSuggestBox.class);
		mockSuggestion = mock(UserGroupSuggestion.class);
		mockJSNIUtils = mock(SynapseJSNIUtils.class);
		mockHeader = mock(UserGroupHeader.class);
		mockTeam = mock(Team.class);
		inviteWidget = new InviteWidget(mockView, mockSynapseClient, mockAuthenticationController,
				mockGlobalApplicationState, mockGWTWrapper, mockSynAlert, mockSuggestBox, mockJSNIUtils);
		mockRefreshCallback = mock(Callback.class);
		inviteWidget.setTeam(mockTeam);
		inviteWidget.setRefreshCallback(mockRefreshCallback);
		when(mockGWTWrapper.getHostPageBaseURL()).thenReturn(EvaluationSubmitterTest.HOST_PAGE_URL);
		when(mockHeader.getOwnerId()).thenReturn(userId);
		when(mockTeam.getId()).thenReturn(teamId);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSendInvitation() throws Exception {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).inviteMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(mockSuggestion);
		when(mockSuggestion.getHeader()).thenReturn(mockHeader);
		inviteWidget.sendInvite("You are invited!");
		verify(mockSynapseClient).inviteMember(eq(userId), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockRefreshCallback).invoke();
		verify(mockView).setVisible(false);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSendInvitationFailure() throws Exception {
		Exception caught = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).inviteMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(mockSuggestion);
		when(mockSuggestion.getHeader()).thenReturn(mockHeader);
		inviteWidget.sendInvite("You are invited!");
		verify(mockSynapseClient).inviteMember(eq(userId), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(caught);
	}
}
