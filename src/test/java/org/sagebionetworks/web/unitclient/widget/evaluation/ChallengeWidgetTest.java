package org.sagebionetworks.web.unitclient.widget.evaluation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidget;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidget;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidgetView;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.SelectTeamModal;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

@RunWith(MockitoJUnitRunner.class)
public class ChallengeWidgetTest {

	ChallengeWidget widget;
	@Mock
	ChallengeWidgetView mockView;
	@Mock
	SubmitToEvaluationWidget submitToChallengeWidget;
	@Mock
	ChallengeClientAsync mockChallengeClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	BigTeamBadge mockTeamBadge;
	@Mock
	Challenge mockChallenge;
	@Mock
	SelectTeamModal mockSelectTeamModal;
	@Captor
	ArgumentCaptor<HashMap> hashmapCaptor;

	public static final String PARTICIPANT_TEAM_ID = "1234567890";
	public static final String CHALLENGE_ID = "45678";
	public static final String SELECTED_TEAM_ID = "987654";

	@Before
	public void setup() throws Exception {
		widget = new ChallengeWidget(mockView, mockChallengeClient, mockSynAlert, mockTeamBadge, mockSelectTeamModal, submitToChallengeWidget);

		AsyncMockStubber.callSuccessWith(mockChallenge).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		when(mockChallenge.getId()).thenReturn(CHALLENGE_ID);
		when(mockChallenge.getParticipantTeamId()).thenReturn(PARTICIPANT_TEAM_ID);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).add(any(Widget.class));
		verify(mockView).setChallengeTeamWidget(any(Widget.class));
		verify(mockView).setSelectTeamModal(any(Widget.class));
	}

	@Test
	public void testConfigure() {
		widget.configure("syn100");
		verify(mockSynAlert).clear();
		verify(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		verify(mockTeamBadge).configure(PARTICIPANT_TEAM_ID);
		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setChallengeVisible(false);
		inOrder.verify(mockView).setChallengeVisible(true);
		verify(mockView).setChallengeId(CHALLENGE_ID);
		// submit to challenge widget has been configured
		verify(submitToChallengeWidget).configure(any(), hashmapCaptor.capture(), any(), any());
		assertEquals(CHALLENGE_ID, hashmapCaptor.getValue().get(WidgetConstants.CHALLENGE_ID_KEY));
	}

	@Test
	public void testConfigureChallengeNotFound() {
		Exception ex = new NotFoundException("Challenge not found");
		AsyncMockStubber.callFailureWith(ex).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		widget.configure("syn100");

		verify(mockSynAlert).clear();
		verify(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		verify(mockView, times(2)).setChallengeVisible(false);
	}

	@Test
	public void testConfigureFailure() {
		Exception ex = new Exception("unknown error");
		AsyncMockStubber.callFailureWith(ex).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		widget.configure("syn100");

		verify(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));

		verify(mockView).setChallengeVisible(false);

		InOrder inOrder = inOrder(mockSynAlert);
		inOrder.verify(mockSynAlert).clear();
		inOrder.verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testOnEditTeamClicked() {
		widget.setCurrentChallenge(mockChallenge);
		widget.onEditTeamClicked();
		verify(mockSelectTeamModal).show();

		// now simulate that a team was selected
		ArgumentCaptor<CallbackP> teamSelectedCallback = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockSelectTeamModal).configure(teamSelectedCallback.capture());
		teamSelectedCallback.getValue().invoke(SELECTED_TEAM_ID);

		verify(mockChallengeClient).updateChallenge(eq(mockChallenge), any(AsyncCallback.class));
		verify(mockChallenge).setParticipantTeamId(SELECTED_TEAM_ID);
	}

}
