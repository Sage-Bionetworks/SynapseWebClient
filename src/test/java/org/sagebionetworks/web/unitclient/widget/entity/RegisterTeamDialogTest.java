package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.RegisterTeamDialog;
import org.sagebionetworks.web.client.widget.entity.RegisterTeamDialogView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for RegisterTeamDialog
 */
public class RegisterTeamDialogTest {

	public static final String LOGGED_IN_USER_ID = "008";
	public static final String CHALLENGE_ID = "12";
	RegisterTeamDialog widget;
	RegisterTeamDialogView mockView;
	ChallengeClientAsync mockChallengeClient;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	PlaceChanger mockPlaceChanger;
	Callback mockCallback;
	
	List<Team> registratableTeams;
	Team firstTeam;
	
	@Before
	public void before() {
		mockChallengeClient = mock(ChallengeClientAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockView = mock(RegisterTeamDialogView.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockCallback = mock(Callback.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		widget = new RegisterTeamDialog(mockView, mockChallengeClient, mockGlobalApplicationState, mockAuthenticationController);
		registratableTeams = new ArrayList<Team>();

		firstTeam = new Team();
		firstTeam.setId("1001");
		Team secondTeam = new Team();
		secondTeam.setId("1002");
		registratableTeams.add(firstTeam);
		registratableTeams.add(secondTeam);
		
		AsyncMockStubber.callSuccessWith(registratableTeams).when(mockChallengeClient).getRegistratableTeams(anyString(), anyString(), any(AsyncCallback.class));
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(LOGGED_IN_USER_ID);
		ChallengeTeam newChallengeTeam = new ChallengeTeam();
		AsyncMockStubber.callSuccessWith(newChallengeTeam).when(mockChallengeClient).registerChallengeTeam(any(ChallengeTeam.class), any(AsyncCallback.class));
	}

	@Test
	public void testConfigureWithTeams() {
		widget.configure(CHALLENGE_ID, mockCallback);
		
		verify(mockView).setRecruitmentMessage("");
		verify(mockView).setNewTeamLink(eq("#!Profile:"+mockAuthenticationController.getCurrentUserPrincipalId()+Profile.DELIMITER+Synapse.ProfileArea.TEAMS));
		verify(mockView).setNoTeamsFoundVisible(false);
		verify(mockView).setTeams(registratableTeams);
		assertEquals(firstTeam.getId(), widget.getSelectedTeamId());
		verify(mockView).showModal();
		
		//now try selecting invalid and valid indices
		widget.teamSelected(-1);
		assertNull(widget.getSelectedTeamId());
		
		widget.teamSelected(0);
		assertEquals(firstTeam.getId(), widget.getSelectedTeamId());
		
		widget.teamSelected(10);
		assertNull(widget.getSelectedTeamId());
	}
	
	@Test
	public void testConfigureWithNoTeams() {
		registratableTeams.clear();
		widget.configure(CHALLENGE_ID, mockCallback);
		
		verify(mockView).setRecruitmentMessage("");
		verify(mockView).setNewTeamLink(eq("#!Profile:"+mockAuthenticationController.getCurrentUserPrincipalId()+Profile.DELIMITER+Synapse.ProfileArea.TEAMS));
		verify(mockView).setNoTeamsFoundVisible(true);
		verify(mockView).showModal();
	}
	
	@Test
	public void testConfigureFail() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockChallengeClient).getRegistratableTeams(anyString(), anyString(), any(AsyncCallback.class));
		widget.configure(CHALLENGE_ID, mockCallback);
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testIsValidNoSelectedTeam() {
		assertFalse(widget.isValid());
		verify(mockView).showErrorMessage(anyString());
	}
	@Test
	public void testIsValid() {
		widget.configure(CHALLENGE_ID, mockCallback);
		assertTrue(widget.isValid());
	}
	
	@Test
	public void testRegisterChallengeTeam() {
		widget.configure(CHALLENGE_ID, mockCallback);
		widget.onOk();
		ArgumentCaptor<ChallengeTeam> challengeTeamCaptor = ArgumentCaptor.forClass(ChallengeTeam.class);
		verify(mockChallengeClient).registerChallengeTeam(challengeTeamCaptor.capture(), any(AsyncCallback.class));
		ChallengeTeam capturedTeam = challengeTeamCaptor.getValue();
		assertEquals(CHALLENGE_ID, capturedTeam.getChallengeId());
		assertEquals(firstTeam.getId(), capturedTeam.getTeamId());
		
		verify(mockCallback).invoke();
		verify(mockView).showInfo(anyString());
		verify(mockView).hideModal();
	}

	@Test
	public void testRegisterChallengeTeamFailure() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockChallengeClient).registerChallengeTeam(any(ChallengeTeam.class), any(AsyncCallback.class));
		widget.configure(CHALLENGE_ID, mockCallback);
		widget.onOk();
		verify(mockChallengeClient).registerChallengeTeam(any(ChallengeTeam.class), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
}
