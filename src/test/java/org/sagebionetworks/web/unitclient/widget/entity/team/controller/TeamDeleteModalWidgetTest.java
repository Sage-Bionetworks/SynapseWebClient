package org.sagebionetworks.web.unitclient.widget.entity.team.controller;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidgetView;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidgetView;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class TeamDeleteModalWidgetTest {

	TeamDeleteModalWidget presenter;
	SynapseAlert mockSynAlert;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseClientAsync mockSynapseClient;
	TeamDeleteModalWidgetView mockView;
	Callback mockRefreshCallback;
	Team mockTeam;
	
	String userId = "userId";
	String teamId = "teamId";
	Exception caught = new Exception("this is an exception");

	@Before
	public void setup() {
		mockSynAlert = mock(SynapseAlert.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(TeamDeleteModalWidgetView.class);
		mockRefreshCallback = mock(Callback.class);
		mockTeam = mock(Team.class);
		when(mockTeam.getId()).thenReturn(teamId);
		presenter = new TeamDeleteModalWidget(mockSynAlert, mockSynapseClient, mockGlobalApplicationState, mockView);
		presenter.setRefreshCallback(mockRefreshCallback);
		presenter.setTeam(mockTeam);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(presenter);
		verify(mockView).setSynAlertWidget(mockSynAlert.asWidget());
	}
	
	@Test
	public void testOnConfirmSuccess() {
		presenter.onConfirm();
		verify(mockSynAlert).clear();
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		verify(mockSynapseClient).deleteTeam(eq(teamId), captor.capture());
		captor.getValue().onSuccess(null);
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockGlobalApplicationState).gotoLastPlace();
	}
	
	@Test
	public void testOnConfirmFailure() {
		presenter.onConfirm();
		verify(mockSynAlert).clear();
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		verify(mockSynapseClient).deleteTeam(eq(teamId), captor.capture());
		captor.getValue().onFailure(caught);
		verify(mockSynAlert).handleException(caught);
	}
	
}
