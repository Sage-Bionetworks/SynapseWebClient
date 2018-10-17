package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.DisplayUtils.getDisplayName;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.InviteeVerificationSignedToken;
import org.sagebionetworks.repo.model.MembershipInvtnSignedToken;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseFutureClient;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.EmailInvitation;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.EmailInvitationPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.EmailInvitationView;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

@RunWith(MockitoJUnitRunner.class)
public class EmailInvitationPresenterTest {
	@Mock private EmailInvitationView view;
	@Mock private RegisterWidget registerWidget;
	@Mock private SynapseJavascriptClient jsClient;
	@Mock private SynapseFutureClient futureClient;
	@Mock private SynapseAlert synapseAlert;
	@Mock private AuthenticationController authController;
	@Mock private GlobalApplicationState globalApplicationState;
	@Mock private PlaceChanger placeChanger;
	@Mock private EmailInvitation place;
	@Mock private InviteeVerificationSignedToken inviteeVerificationSignedToken;
	@Mock private MembershipInvitation mis;
	@Mock private Team team;
	@Mock private UserProfile inviterProfile;

	private EmailInvitationPresenter presenter;
	private String encodedMISignedToken;

	@Before
	public void before() {
		when(globalApplicationState.getPlaceChanger()).thenReturn(placeChanger);
		presenter = new EmailInvitationPresenter(
				view, registerWidget, jsClient, futureClient, synapseAlert, authController, globalApplicationState);
	}

	public void beforeSetPlace(boolean loggedIn) {
		when(authController.isLoggedIn()).thenReturn(loggedIn);
		encodedMISignedToken = "encodedToken";
		when(place.toToken()).thenReturn(encodedMISignedToken);
		MembershipInvtnSignedToken decodedToken = new MembershipInvtnSignedToken();
		when(futureClient.hexDecodeAndDeserialize(anyString(), eq(encodedMISignedToken))).thenReturn(getDoneFuture(decodedToken));
		when(jsClient.getMembershipInvitation(decodedToken)).thenReturn(getDoneFuture(mis));
		when(mis.getId()).thenReturn("misId");
		when(mis.getInviteeEmail()).thenReturn("invitee@email.com");
		when(mis.getTeamId()).thenReturn("teamId");
		when(mis.getCreatedBy()).thenReturn("createdBy");
		when(mis.getMessage()).thenReturn("message");
	}

	@Test
	public void testLoggedIn() {
		beforeSetPlace(true);
		when(jsClient.getInviteeVerificationSignedToken(mis.getId())).thenReturn(getDoneFuture(inviteeVerificationSignedToken));
		when(jsClient.updateInviteeId(inviteeVerificationSignedToken)).thenReturn(getDoneFuture(null));
		presenter.setPlace(place);
		verify(jsClient).getInviteeVerificationSignedToken(mis.getId());
		verify(jsClient).updateInviteeId(inviteeVerificationSignedToken);
		ArgumentCaptor<org.sagebionetworks.web.client.place.Team> captor = ArgumentCaptor.forClass(org.sagebionetworks.web.client.place.Team.class);
		verify(placeChanger).goTo(captor.capture());
		assertEquals(mis.getTeamId(), captor.getValue().getTeamId());
	}

	@Test
	public void testNotLoggedIn() {
		beforeSetPlace(false);
		when(jsClient.getTeam(mis.getTeamId())).thenReturn(getDoneFuture(team));
		when(team.getName()).thenReturn("teamName");
		when(jsClient.getUserProfile(mis.getCreatedBy())).thenReturn(getDoneFuture(inviterProfile));
		when(inviterProfile.getFirstName()).thenReturn("First");
		when(inviterProfile.getLastName()).thenReturn("Last");
		when(inviterProfile.getUserName()).thenReturn("Nick");
		presenter.setPlace(place);
		verify(view).showNotLoggedInUI();
		verify(registerWidget).enableEmailAddressField(false);
		verify(view).setRegisterWidget(registerWidget.asWidget());
		verify(view).setSynapseAlertContainer(synapseAlert.asWidget());
		verify(registerWidget).setEncodedMembershipInvtnSignedToken(encodedMISignedToken);
		verify(registerWidget).setEmail(mis.getInviteeEmail());
		verify(view).setInvitationTitle(contains(getDisplayName(inviterProfile)));
		verify(view).setInvitationMessage(mis.getMessage());
	}

	@Test
	public void testInvalidSignedToken() {
		beforeSetPlace(false);
		when(futureClient.hexDecodeAndDeserialize(anyString(), eq(encodedMISignedToken))).thenReturn(getFailedFuture());
		presenter.setPlace(place);
		verify(synapseAlert).handleException(any(Throwable.class));
		verify(jsClient, never()).getMembershipInvitation(any());
	}

	@Test
	public void testLoggedInUserIsNotInvitee() {
		beforeSetPlace(true);
		when(jsClient.getInviteeVerificationSignedToken(mis.getId())).thenReturn(getFailedFuture());
		presenter.setPlace(place);
		verify(synapseAlert).handleException(any(Throwable.class));
		verify(jsClient, never()).updateInviteeId(any());
	}

	@Test
	public void testOnLoginClick() {
		presenter.onLoginClick();
		ArgumentCaptor<LoginPlace> captor = ArgumentCaptor.forClass(LoginPlace.class);
		verify(placeChanger).goTo(captor.capture());
		assertEquals(LoginPlace.LOGIN_TOKEN, captor.getValue().toToken());
	}
}
