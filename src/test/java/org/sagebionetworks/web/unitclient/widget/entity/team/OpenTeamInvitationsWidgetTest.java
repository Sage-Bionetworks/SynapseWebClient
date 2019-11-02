package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;
import static org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget.RECEIVED;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidgetView;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class OpenTeamInvitationsWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	OpenTeamInvitationsWidgetView mockView;
	String teamId = "123";
	OpenTeamInvitationsWidget widget;
	AuthenticationController mockAuthenticationController;
	PortalGinInjector mockPortalGinInjector;
	JoinTeamWidget mockJoinTeamWidget;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	Team testTeam;
	MembershipInvitation testInvite;
	List<OpenUserInvitationBundle> testReturn;
	Callback mockTeamUpdatedCallback;
	CallbackP<List<OpenUserInvitationBundle>> mockOpenTeamInvitationsCallback;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	SynapseJavascriptClient mockJsClient;
	public static final String FORMATTED_DATE = "a second ago";

	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(OpenTeamInvitationsWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockTeamUpdatedCallback = mock(Callback.class);
		mockPortalGinInjector = mock(PortalGinInjector.class);
		mockJoinTeamWidget = mock(JoinTeamWidget.class);
		widget = new OpenTeamInvitationsWidget(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController, mockPortalGinInjector, mockSynapseAlert, mockDateTimeUtils, mockPopupUtils, mockJsClient);
		testTeam = new Team();
		testTeam.setId(teamId);
		testTeam.setName("Bob's Team");
		testInvite = new MembershipInvitation();
		testInvite.setTeamId(teamId);
		testInvite.setInviteeId("42");
		testInvite.setMessage("This is a test invite");
		testInvite.setCreatedOn(new Date());

		when(mockDateTimeUtils.getRelativeTime(any(Date.class))).thenReturn(FORMATTED_DATE);
		testReturn = new ArrayList<OpenUserInvitationBundle>();
		OpenUserInvitationBundle mib = new OpenUserInvitationBundle();
		mib.setTeam(testTeam);
		mib.setMembershipInvitation(testInvite);
		testReturn.add(mib);

		AsyncMockStubber.callSuccessWith(testReturn).when(mockSynapseClient).getOpenInvitations(anyString(), any(AsyncCallback.class));

		mockOpenTeamInvitationsCallback = mock(CallbackP.class);
		mockTeamUpdatedCallback = mock(Callback.class);

		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockPortalGinInjector.getJoinTeamWidget()).thenReturn(mockJoinTeamWidget);

		when(mockJsClient.deleteMembershipInvitation(anyString())).thenReturn(getDoneFuture(null));
	}

	@Test
	public void testConfigure() throws Exception {
		widget.configure(mockTeamUpdatedCallback, mockOpenTeamInvitationsCallback);
		verify(mockSynapseClient).getOpenInvitations(anyString(), any(AsyncCallback.class));
		verify(mockView).addTeamInvite(any(Team.class), any(String.class), eq(RECEIVED + FORMATTED_DATE), anyString(), any(Widget.class));
		verify(mockPortalGinInjector).getJoinTeamWidget();
		ArgumentCaptor<Callback> refreshCallbackCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockJoinTeamWidget).configure(eq(teamId), refreshCallbackCaptor.capture());
		ArgumentCaptor<List> invitesArg = ArgumentCaptor.forClass(List.class);
		verify(mockOpenTeamInvitationsCallback).invoke(invitesArg.capture());
		assertEquals(testReturn, invitesArg.getValue());


		// test refresh (if one would join using the join team widget)
		verify(mockTeamUpdatedCallback, never()).invoke();
		Callback refreshCallback = refreshCallbackCaptor.getValue();
		refreshCallback.invoke();
		verify(mockTeamUpdatedCallback).invoke();
		verify(mockSynapseClient, times(2)).getOpenInvitations(anyString(), any(AsyncCallback.class));
		verify(mockView, times(2)).addTeamInvite(any(Team.class), anyString(), eq(RECEIVED + FORMATTED_DATE), anyString(), any(Widget.class));
		verify(mockPortalGinInjector, times(2)).getJoinTeamWidget();
		verify(mockJoinTeamWidget, times(2)).configure(eq(teamId), any(Callback.class));
	}

	@Test
	public void testConfigureFailureGetOpenInvites() throws Exception {
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getOpenInvitations(anyString(), any(AsyncCallback.class));
		widget.configure(mockTeamUpdatedCallback, mockOpenTeamInvitationsCallback);
		verify(mockSynapseClient).getOpenInvitations(anyString(), any(AsyncCallback.class));
		verify(mockSynapseAlert).handleException(ex);
	}


	@Test
	public void testDeleteInvitation() {
		String inviteId = "14";
		widget.configure(mockTeamUpdatedCallback, mockOpenTeamInvitationsCallback);
		widget.deleteInvitation(inviteId);

		verify(mockSynapseAlert, atLeastOnce()).clear();
		verify(mockJsClient).deleteMembershipInvitation(inviteId);
		verify(mockPopupUtils).showInfo(OpenTeamInvitationsWidget.DELETED_INVITATION_MESSAGE);
		verify(mockTeamUpdatedCallback).invoke();
		verify(mockSynapseClient, times(2)).getOpenInvitations(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testDeleteInvitationFailure() {
		Exception ex = new Exception("error");
		when(mockJsClient.deleteMembershipInvitation(anyString())).thenReturn(getFailedFuture(ex));

		String inviteId = "14";
		widget.configure(mockTeamUpdatedCallback, mockOpenTeamInvitationsCallback);
		widget.deleteInvitation(inviteId);

		verify(mockSynapseAlert, atLeastOnce()).clear();
		verify(mockJsClient).deleteMembershipInvitation(inviteId);
		verify(mockSynapseAlert).handleException(ex);
	}
}
