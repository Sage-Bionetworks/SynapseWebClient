package org.sagebionetworks.web.unitclient.widget.entity.team.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidgetView;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class TeamDeleteModalWidgetTest {

	TeamDeleteModalWidget presenter;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	TeamDeleteModalWidgetView mockView;
	@Mock
	Callback mockRefreshCallback;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	Team mockTeam;
	
	String userId = "userId";
	String teamId = "teamId";
	Exception caught = new Exception("this is an exception");
	
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Captor
	ArgumentCaptor<Place> placeCaptor;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockRefreshCallback = mock(Callback.class);
		when(mockTeam.getId()).thenReturn(teamId);
		presenter = new TeamDeleteModalWidget(mockSynAlert, 
				mockSynapseClient, 
				mockGlobalApplicationState, 
				mockView, 
				mockAuthController);
		presenter.setRefreshCallback(mockRefreshCallback);
		presenter.configure(mockTeam);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(userId);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(presenter);
		verify(mockView).setSynAlertWidget(mockSynAlert.asWidget());
	}
	
	@Test
	public void testOnConfirmSuccess() {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient)
		.deleteTeam(eq(teamId), any(AsyncCallback.class));
		presenter.onConfirm();
		verify(mockSynAlert).clear();
		verify(mockSynapseClient).deleteTeam(eq(teamId), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
		verify(mockPlaceChanger).goTo(placeCaptor.capture());
		Place gotoPlace = placeCaptor.getValue();
		assertTrue(gotoPlace instanceof Profile);
		assertEquals(userId, ((Profile)gotoPlace).getUserId());
		assertEquals(ProfileArea.TEAMS, ((Profile)gotoPlace).getArea());
	}
	
	@Test
	public void testOnConfirmFailure() {
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient)
		.deleteTeam(eq(teamId), any(AsyncCallback.class));
		presenter.onConfirm();
		verify(mockSynAlert).clear();
		verify(mockSynapseClient).deleteTeam(eq(teamId), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(caught);
	}
	
}
