package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
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
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
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
		when(mockSuggestion.getId()).thenReturn(userId);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSendInvitation() throws Exception {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).inviteMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(mockSuggestion);
		when(mockSuggestion.getHeader()).thenReturn(mockHeader);
		inviteWidget.doSendInvites(invitationMessage);
		verify(mockSynapseClient).inviteMember(eq(userId), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockRefreshCallback).invoke();
		verify(mockView).hide();
	}

	@Test
	public void testSendNoUserSelected() {
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(null);
		when(mockSuggestBox.getText()).thenReturn("notAnEmailAddress");

		inviteWidget.doSendInvites(invitationMessage);

		// no users added, so it's a no-op
		verifyZeroInteractions(mockSynapseClient);
		verify(mockSynAlert).showError(InviteWidget.INVALID_EMAIL_ERROR_MESSAGE);
	}

	@Test
	public void testSendNoUsersAdded() {
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(null);
		when(mockSuggestBox.getText()).thenReturn("");

		inviteWidget.doSendInvites(invitationMessage);

		verifyZeroInteractions(mockSynapseClient);
		verify(mockSynAlert).showError(InviteWidget.NO_USERS_OR_EMAILS_ADDED_ERROR_MESSAGE);
	}

	@Test
	public void testSendToEmailAddress() {
		String email = "Test@eXample.coM";
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(null);
		when(mockSuggestBox.getText()).thenReturn(email);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).inviteNewMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		inviteWidget.doSendInvites(invitationMessage);
		verify(mockSynapseClient).inviteNewMember(eq(email), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockRefreshCallback).invoke();
		verify(mockView).hide();
	}

	@Test
	public void testSendToEmailAddressInvalidEmail() {
		String emails = "test1@x.com, test2@y.edu";
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(null);
		when(mockSuggestBox.getText()).thenReturn(emails);
		inviteWidget.doSendInvites(invitationMessage);

		verify(mockSynAlert).showError(InviteWidget.INVALID_EMAIL_ERROR_MESSAGE);
		verify(mockSynapseClient, never()).inviteNewMember(anyString(), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockRefreshCallback, never()).invoke();
		verify(mockView, never()).hide();
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testSendInvitationFailure() throws Exception {
		Exception caught = new Exception("unhandled exception");
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).isTeamMember(anyString(), anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).inviteMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(mockSuggestion);
		when(mockSuggestion.getHeader()).thenReturn(mockHeader);
		inviteWidget.doSendInvites("You are invited!");
		verify(mockSynapseClient).inviteMember(eq(userId), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(caught);
	}

	@Test
	public void testInviteMultipleUsers() {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).inviteMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).inviteNewMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));

		// add 2 emails (and verify that it's added to the view)
		String email1 = "emailAddress1@synapse.org";
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(null);
		when(mockSuggestBox.getText()).thenReturn(email1);
		inviteWidget.addSuggestion();
		verify(mockView).addEmailToInvite(email1);
		verify(mockSuggestBox, times(2)).clear();

		String email2 = "emailAddress2@synapse.org";
		when(mockSuggestBox.getText()).thenReturn(email2);
		inviteWidget.addSuggestion();
		verify(mockView).addEmailToInvite(email2);

		// add user (and verify that it's added to the view)
		when(mockSuggestBox.getText()).thenReturn("");
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(mockSuggestion);

		inviteWidget.addSuggestion();

		verify(mockView).addUserToInvite(userId);
		verify(mockSuggestBox, times(4)).clear();
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(null);

		// verify that after this setup, we create 3 invitations when we click the Invite button
		inviteWidget.doSendInvites(invitationMessage);

		verify(mockSynapseClient).inviteNewMember(eq(email1), eq(teamId), eq(invitationMessage), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockSynapseClient).inviteNewMember(eq(email2), eq(teamId), eq(invitationMessage), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockSynapseClient).inviteMember(eq(userId), eq(teamId), eq(invitationMessage), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockRefreshCallback).invoke();
		verify(mockView).hide();
	}
}
