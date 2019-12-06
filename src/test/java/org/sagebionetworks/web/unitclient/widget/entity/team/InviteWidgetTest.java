package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoDialog;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import org.sagebionetworks.web.client.widget.team.InviteWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.entity.EvaluationSubmitterTest;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class InviteWidgetTest {
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	InviteWidgetView mockView;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseSuggestBox mockSuggestBox;
	@Mock
	SynapseJSNIUtils mockJSNIUtils;
	@Mock
	Team mockTeam;
	String teamId = "123";
	String userId = "873873";
	InviteWidget inviteWidget;
	@Mock
	UserGroupHeader mockHeader;
	@Mock
	UserGroupSuggestionProvider mockSuggestionProvider;
	@Mock
	UserGroupSuggestion mockSuggestion;
	@Mock
	Callback mockRefreshCallback;
	@Mock
	GWTWrapper mockGWTWrapper;
	@Mock
	QuizInfoDialog mockQuizInfoDialog;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	UserBundle mockUserBundle;

	String invitationMessage = "You are invited!";

	@Before
	public void before() throws JSONObjectAdapterException {
		when(mockGinInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);
		when(mockGinInjector.getQuizInfoDialog()).thenReturn(mockQuizInfoDialog);
		when(mockGinInjector.getAuthenticationController()).thenReturn(mockAuthController);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(userId);
		AsyncMockStubber.callSuccessWith(mockUserBundle).when(mockJsClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
		when(mockUserBundle.getIsCertified()).thenReturn(true);
		inviteWidget = new InviteWidget(mockView, mockSynapseClient, mockGWTWrapper, mockSynAlert, mockSuggestBox, mockSuggestionProvider, mockGinInjector);
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
		String errorMessage = "unhandled exception";
		Exception caught = new Exception(errorMessage);
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).isTeamMember(anyString(), anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).inviteMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(mockSuggestion);
		when(mockSuggestion.getHeader()).thenReturn(mockHeader);
		inviteWidget.doSendInvites("You are invited!");
		verify(mockSynapseClient).inviteMember(eq(userId), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockSynAlert).showError(errorMessage);
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
	
	@Test
	public void testUncertifiedAddEmail() {
		when(mockUserBundle.getIsCertified()).thenReturn(false);
		inviteWidget.configure(mockTeam);
		
		// add an email, and verify that it shows you the Get Certified UI instead
		String email = "emailAddress1@synapse.org";
		when(mockSuggestBox.getSelectedSuggestion()).thenReturn(null);
		when(mockSuggestBox.getText()).thenReturn(email);
		inviteWidget.addSuggestion();
		
		verify(mockView).hide();
		verify(mockQuizInfoDialog).show();
	}
}
