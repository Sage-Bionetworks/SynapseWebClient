package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
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
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import org.sagebionetworks.web.client.widget.team.InviteWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.entity.EvaluationSubmitterTest;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class InviteWidgetTest {

	SynapseClientAsync mockSynapseClient;
	InviteWidgetView mockView;
	SynapseAlert mockSynAlert;
	SynapseSuggestBox mockSuggestBox;
	SynapseJSNIUtils mockJSNIUtils;
	Team mockTeam;
	String teamId = "123";
	String userId = "testId";
	InviteWidget inviteWidget;
	UserGroupHeader mockHeader;
	UserGroupSuggestionProvider mockSuggestionProvider;
	UserGroupSuggestion mockSuggestion;
	Callback mockRefreshCallback;
	GWTWrapper mockGWTWrapper;
	
	String invitationMessage = "You are invited!";
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(InviteWidgetView.class);
		mockGWTWrapper = mock(GWTWrapper.class);
		mockSynAlert = mock(SynapseAlert.class);
		mockSuggestBox = mock(SynapseSuggestBox.class);
		mockSuggestion = mock(UserGroupSuggestion.class);
		mockJSNIUtils = mock(SynapseJSNIUtils.class);
		mockHeader = mock(UserGroupHeader.class);
		mockTeam = mock(Team.class);
		mockSuggestionProvider = mock(UserGroupSuggestionProvider.class);
		inviteWidget = new InviteWidget(mockView, mockSynapseClient, mockGWTWrapper, mockSynAlert, mockSuggestBox, mockSuggestionProvider);
		mockRefreshCallback = mock(Callback.class);
		inviteWidget.configure(mockTeam);
		inviteWidget.setRefreshCallback(mockRefreshCallback);
		when(mockGWTWrapper.getHostPageBaseURL()).thenReturn(EvaluationSubmitterTest.HOST_PAGE_URL);
		when(mockHeader.getOwnerId()).thenReturn(userId);
		when(mockTeam.getId()).thenReturn(teamId);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSendInvitation() throws Exception {		
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).isTeamMember(anyString(), anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).inviteMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(mockSuggestion);
		when(mockSuggestion.getHeader()).thenReturn(mockHeader);
		inviteWidget.validateAndSendInvite(invitationMessage);
		verify(mockSynapseClient).inviteMember(eq(userId), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockRefreshCallback).invoke();
		verify(mockView).hide();
	}
	
	@Test
	public void testSendToMember() {
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(mockSuggestion);
		when(mockSuggestion.getHeader()).thenReturn(mockHeader);
		AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).isTeamMember(anyString(), anyLong(), any(AsyncCallback.class));
		inviteWidget.validateAndSendInvite(invitationMessage);
		verify(mockSynAlert).showError("This user is already a member.");
	}
	
	@Test
	public void testSendNoUserSelected() {
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(null);
		inviteWidget.validateAndSendInvite(invitationMessage);
		verify(mockSynAlert).showError("Please select a user to send an invite to.");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSendInvitationFailure() throws Exception {
		Exception caught = new Exception("unhandled exception");
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).isTeamMember(anyString(), anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).inviteMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(mockSuggestion);
		when(mockSuggestion.getHeader()).thenReturn(mockHeader);
		inviteWidget.validateAndSendInvite("You are invited!");
		verify(mockSynapseClient).inviteMember(eq(userId), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(caught);
	}
}
