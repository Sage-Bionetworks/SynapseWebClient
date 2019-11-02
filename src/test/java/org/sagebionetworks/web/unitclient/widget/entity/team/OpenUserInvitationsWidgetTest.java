package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.EmailInvitationBadge;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidgetView;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.OpenTeamInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class OpenUserInvitationsWidgetTest {
	@Mock
	private SynapseClientAsync mockSynapseClient;
	@Mock
	private GlobalApplicationState mockGlobalApplicationState;
	@Mock
	private OpenUserInvitationsWidgetView mockView;
	@Mock
	private Callback mockTeamUpdatedCallback;
	@Mock
	private SynapseAlert mockSynapseAlert;
	@Mock
	private GWTWrapper mockGWT;
	@Mock
	private DateTimeUtils mockDateTimeUtils;
	@Mock
	private SynapseJavascriptClient mockJsClient;
	@Mock
	private PortalGinInjector mockGinInjector;
	@Mock
	private UserBadge mockUserBadge;
	@Mock
	private EmailInvitationBadge mockEmailInvitationBadge;
	@Mock
	private PopupUtilsView mockPopupUtils;
	private String teamId = "123";
	private OpenUserInvitationsWidget widget;
	private UserProfile testProfile;
	private MembershipInvitation testInvite;
	private MembershipInvitation testEmailInvite;

	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		widget = new OpenUserInvitationsWidget(mockView, mockSynapseClient, mockGlobalApplicationState, mockSynapseAlert, mockGWT, mockDateTimeUtils, mockJsClient, mockPopupUtils, mockGinInjector);

		testProfile = new UserProfile();
		testProfile.setOwnerId("42");
		testProfile.setFirstName("Bob");
		testInvite = new MembershipInvitation();
		testInvite.setTeamId(teamId);
		testInvite.setInviteeId("42");
		testInvite.setMessage("This is a test invite");
		testEmailInvite = new MembershipInvitation();
		testEmailInvite.setTeamId(teamId);
		testEmailInvite.setInviteeEmail("test@example.com");
		testEmailInvite.setMessage("This is an email test invite");

		List<OpenTeamInvitationBundle> testReturn = new ArrayList<>();
		OpenTeamInvitationBundle mib = new OpenTeamInvitationBundle();
		mib.setUserProfile(testProfile);
		mib.setMembershipInvitation(testInvite);
		testReturn.add(mib);

		AsyncMockStubber.callSuccessWith(testReturn).when(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).deleteMembershipInvitation(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).resendTeamInvitation(anyString(), anyString(), any(AsyncCallback.class));

		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		when(mockGinInjector.getEmailInvitationBadgeWidget()).thenReturn(mockEmailInvitationBadge);
	}

	private void setupGetOpenTeamInvitations(int userInvitationCount, int emailInvitationCount) {
		List<OpenTeamInvitationBundle> testReturn = new ArrayList<>();
		for (int i = 0; i < userInvitationCount; i++) {
			OpenTeamInvitationBundle mockBundle = mock(OpenTeamInvitationBundle.class);
			when(mockBundle.getUserProfile()).thenReturn(testProfile);
			when(mockBundle.getMembershipInvitation()).thenReturn(testInvite);
			testReturn.add(mockBundle);
		}
		for (int i = 0; i < emailInvitationCount; i++) {
			OpenTeamInvitationBundle mockBundle = mock(OpenTeamInvitationBundle.class);
			when(mockBundle.getUserProfile()).thenReturn(null);
			when(mockBundle.getMembershipInvitation()).thenReturn(testEmailInvite);
			testReturn.add(mockBundle);
		}
		AsyncMockStubber.callSuccessWith(testReturn).when(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}

	@Test
	public void testConfigure() throws Exception {
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView, times(1)).addInvitation(eq(mockUserBadge), eq(null), eq(testInvite.getId()), eq(testInvite.getMessage()), anyString());
		verify(mockGWT).restoreWindowPosition();
		verify(mockView).setVisible(true);
	}

	@Test
	public void testEmptyResults() {
		AsyncMockStubber.callSuccessWith(new ArrayList<>()).when(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockView).setVisible(false);
	}

	@Test
	public void testConfigureMultipleInvitations() {
		int invitationCount = 5;
		setupGetOpenTeamInvitations(invitationCount, 0);
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView, times(invitationCount)).addInvitation(eq(mockUserBadge), eq(null), eq(testInvite.getId()), eq(testInvite.getMessage()), anyString());
		verify(mockGWT).restoreWindowPosition();
	}

	@Test
	public void testConfigureMultipleEmailInvitations() {
		int emailInvitationCount = 5;
		setupGetOpenTeamInvitations(0, emailInvitationCount);
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView, times(emailInvitationCount)).addInvitation(eq(mockEmailInvitationBadge), eq(null), eq(testEmailInvite.getId()), eq(testEmailInvite.getMessage()), anyString());
		verify(mockGWT).restoreWindowPosition();
	}

	@Test
	public void testConfigureMixedInvitations() {
		int userInvitationCount = 3;
		int emailInvitationCount = 7;
		setupGetOpenTeamInvitations(userInvitationCount, emailInvitationCount);
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView, times(userInvitationCount)).addInvitation(eq(mockUserBadge), eq(testInvite.getInviteeEmail()), eq(testInvite.getId()), eq(testInvite.getMessage()), anyString());
		verify(mockView, times(emailInvitationCount)).addInvitation(eq(mockEmailInvitationBadge), eq(null), eq(testEmailInvite.getId()), eq(testEmailInvite.getMessage()), anyString());
		verify(mockGWT).restoreWindowPosition();
	}

	@Test
	public void testConfigureFailure() throws Exception {
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockSynapseAlert).handleException(ex);
	}

	@Test
	public void testDeleteOpenInvite() throws Exception {
		int expectedOffset = 0;
		String invitationId = "123";
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockView).clear();
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), eq(expectedOffset), any(AsyncCallback.class));

		widget.removeInvitation(invitationId);

		verify(mockGWT).saveWindowPosition();
		verify(mockJsClient).deleteMembershipInvitation(eq(invitationId), any(AsyncCallback.class));
		verify(mockPopupUtils).showInfo(OpenTeamInvitationsWidget.DELETED_INVITATION_MESSAGE);
		verify(mockView, times(2)).clear();
		verify(mockSynapseClient, times(2)).getOpenTeamInvitations(anyString(), anyInt(), eq(expectedOffset), any(AsyncCallback.class));
	}

	@Test
	public void testDeleteOpenInviteFailure() throws Exception {
		String invitationId = "123";
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).deleteMembershipInvitation(anyString(), any(AsyncCallback.class));
		widget.configure(teamId, mockTeamUpdatedCallback);
		widget.removeInvitation(invitationId);
		verify(mockJsClient).deleteMembershipInvitation(eq(invitationId), any(AsyncCallback.class));
		verify(mockSynapseAlert).handleException(ex);
	}

	@Test
	public void testMoreResultsUnavailable() throws Exception {
		setupGetOpenTeamInvitations(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT - 1, 0);
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockView, times(2)).hideMoreButton();
	}

	@Test
	public void testNoResultsAvailable() throws Exception {
		setupGetOpenTeamInvitations(0, 0);
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockView, times(2)).hideMoreButton();
	}

	@Test
	public void testMoreResultsAvailableGetNextBatch() throws Exception {
		setupGetOpenTeamInvitations(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT, 0);
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockView).showMoreButton();
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), eq(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT), eq(0), any(AsyncCallback.class));

		// simulate that there are really no more results
		reset(mockSynapseClient);
		setupGetOpenTeamInvitations(0, 0);
		widget.getNextBatch();
		verify(mockView, times(3)).hideMoreButton();
		// offset should now be 1*OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), eq(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT), eq(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT), any(AsyncCallback.class));
	}

	@Test
	public void testNoResultsFoundGetNextBatch() throws Exception {
		setupGetOpenTeamInvitations(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT, 0);
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockView).showMoreButton();
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), eq(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT), eq(0), any(AsyncCallback.class));

		// simulate that there are really no more results
		reset(mockSynapseClient);
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.getNextBatch();
		verify(mockView, times(3)).hideMoreButton();
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), eq(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT), eq(OpenUserInvitationsWidget.INVITATION_BATCH_LIMIT), any(AsyncCallback.class));
	}

	@Test
	public void testResendOpenInvite() throws Exception {
		int expectedOffset = 0;
		String invitationId = "123";
		widget.configure(teamId, mockTeamUpdatedCallback);
		verify(mockView).clear();
		verify(mockSynapseClient).getOpenTeamInvitations(anyString(), anyInt(), eq(expectedOffset), any(AsyncCallback.class));

		widget.resendInvitation(invitationId);

		verify(mockGWT).saveWindowPosition();
		verify(mockSynapseClient).resendTeamInvitation(eq(invitationId), anyString(), any(AsyncCallback.class));
		verify(mockPopupUtils).showInfo(OpenUserInvitationsWidget.RESENT_INVITATION);
		verify(mockView, times(2)).clear();
		verify(mockSynapseClient, times(2)).getOpenTeamInvitations(anyString(), anyInt(), eq(expectedOffset), any(AsyncCallback.class));
	}

	@Test
	public void testResendOpenInviteFailure() throws Exception {
		String invitationId = "123";
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).resendTeamInvitation(anyString(), anyString(), any(AsyncCallback.class));
		widget.configure(teamId, mockTeamUpdatedCallback);

		widget.resendInvitation(invitationId);

		verify(mockSynapseClient).resendTeamInvitation(eq(invitationId), anyString(), any(AsyncCallback.class));
		verify(mockSynapseAlert).handleException(ex);
	}
}
