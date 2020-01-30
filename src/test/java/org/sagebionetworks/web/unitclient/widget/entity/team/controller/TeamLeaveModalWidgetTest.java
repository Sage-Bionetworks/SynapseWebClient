package org.sagebionetworks.web.unitclient.widget.entity.team.controller;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidgetView;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class TeamLeaveModalWidgetTest {

	TeamLeaveModalWidget presenter;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	TeamLeaveModalWidgetView mockView;
	@Mock
	Callback mockRefreshCallback;
	@Mock
	Team mockTeam;

	String userId = "userId";
	String teamId = "teamId";
	Exception caught = new Exception("this is an exception");

	@Before
	public void setup() {
		when(mockTeam.getId()).thenReturn(teamId);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userId);
		presenter = new TeamLeaveModalWidget(mockSynAlert, mockJsClient, mockAuthenticationController, mockView);
		presenter.setRefreshCallback(mockRefreshCallback);
		presenter.configure(mockTeam);
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
		verify(mockAuthenticationController).getCurrentUserPrincipalId();
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		verify(mockJsClient).deleteTeamMember(eq(teamId), eq(userId), captor.capture());
		captor.getValue().onSuccess(null);
		verify(mockView).showInfo(anyString());
		verify(mockRefreshCallback).invoke();
	}

	@Test
	public void testOnConfirmFailure() {
		presenter.onConfirm();
		verify(mockSynAlert).clear();
		verify(mockAuthenticationController).getCurrentUserPrincipalId();
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		verify(mockJsClient).deleteTeamMember(eq(teamId), eq(userId), captor.capture());
		captor.getValue().onFailure(caught);
		verify(mockSynAlert).handleException(caught);
	}

}
