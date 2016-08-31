package org.sagebionetworks.web.unitclient.widget.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidget;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidgetView;
import org.sagebionetworks.web.client.widget.search.GroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.GroupSuggestionProvider.GroupSuggestion;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ChallengeWidgetTest {
	
	ChallengeWidget widget;
	@Mock
	ChallengeWidgetView mockView;
	@Mock
	ChallengeClientAsync mockChallengeClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	BigTeamBadge mockTeamBadge;
	@Mock
	SynapseSuggestBox mockTeamSuggestBox;
	@Mock
	GroupSuggestionProvider mockProvider;
	@Mock
	Challenge mockChallenge;
	@Mock
	GroupSuggestion mockGroupSuggestion;
	
	public static final String PARTICIPANT_TEAM_ID = "1234567890";
	public static final String CHALLENGE_ID = "45678";
	public static final String SELECTED_TEAM_ID = "987654";
	
	@Before
	public void setup() throws Exception{
		MockitoAnnotations.initMocks(this);
		widget = new ChallengeWidget(mockView, mockChallengeClient, mockSynAlert, mockTeamBadge, mockTeamSuggestBox, mockProvider);
		
		AsyncMockStubber.callSuccessWith(mockChallenge).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		when(mockChallenge.getId()).thenReturn(CHALLENGE_ID);
		when(mockChallenge.getParticipantTeamId()).thenReturn(PARTICIPANT_TEAM_ID);
		when(mockGroupSuggestion.getId()).thenReturn(SELECTED_TEAM_ID);
		when(mockTeamSuggestBox.getSelectedSuggestion()).thenReturn(mockGroupSuggestion);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).add(any(Widget.class));
		verify(mockView).setChallengeTeamWidget(any(Widget.class));
		verify(mockView).setSuggestWidget(any(Widget.class));
		verify(mockTeamSuggestBox).setSuggestionProvider(mockProvider);
	}
	
	@Test
	public void testConfigure() {
		widget.configure("syn100");
		verify(mockSynAlert).clear();
		verify(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		verify(mockTeamBadge).configure(PARTICIPANT_TEAM_ID);
		verify(mockView).setCreateChallengeVisible(false);
		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setChallengeVisible(false);
		inOrder.verify(mockView).setChallengeVisible(true);
		verify(mockView).setChallengeId(CHALLENGE_ID);
	}
	
	@Test
	public void testConfigureChallengeNotFound() {
		Exception ex = new NotFoundException("Challenge not found");
		AsyncMockStubber.callFailureWith(ex).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		widget.configure("syn100");
		
		verify(mockSynAlert).clear();
		verify(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		verify(mockView).setChallengeVisible(false);
		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setCreateChallengeVisible(false);
		inOrder.verify(mockView).setCreateChallengeVisible(true);
	}
	
	@Test
	public void testConfigureFailure() {
		Exception ex = new Exception("unknown error");
		AsyncMockStubber.callFailureWith(ex).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		widget.configure("syn100");
		
		verify(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		
		verify(mockView).setChallengeVisible(false);
		verify(mockView).setCreateChallengeVisible(false);
		
		InOrder inOrder = inOrder(mockSynAlert);
		inOrder.verify(mockSynAlert).clear();
		inOrder.verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testOnDeleteChallengeClicked() {
		widget.setCurrentChallenge(mockChallenge);
		AsyncMockStubber.callSuccessWith(null).when(mockChallengeClient).deleteChallenge(anyString(), any(AsyncCallback.class));
		widget.onDeleteChallengeClicked();
		verify(mockChallengeClient).deleteChallenge(anyString(), any(AsyncCallback.class));
		
		verify(mockView).setChallengeVisible(false);
		verify(mockView).setCreateChallengeVisible(true);
	}

	@Test
	public void testOnDeleteChallengeClickedFailure() {
		widget.setCurrentChallenge(mockChallenge);
		Exception ex = new Exception("unknown error");
		AsyncMockStubber.callFailureWith(ex).when(mockChallengeClient).deleteChallenge(anyString(), any(AsyncCallback.class));
		widget.onDeleteChallengeClicked();
		verify(mockChallengeClient).deleteChallenge(anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testOnCreateChallengeClicked() {
		widget.onCreateChallengeClicked();
		verify(mockView).showTeamSelectionModal();
		
		//no simulate that a team was selected
		widget.onSelectChallengeTeam();
		ArgumentCaptor<Challenge> captor = ArgumentCaptor.forClass(Challenge.class);
		verify(mockChallengeClient).createChallenge(captor.capture(), any(AsyncCallback.class));
		
		Challenge c = captor.getValue();
		assertNull(c.getId());
		assertEquals(SELECTED_TEAM_ID, c.getParticipantTeamId());
		verify(mockTeamSuggestBox).clear();
	}
	
	@Test
	public void testOnEditTeamClicked() {
		widget.setCurrentChallenge(mockChallenge);
		widget.onEditTeamClicked();
		verify(mockView).showTeamSelectionModal();
		
		//no simulate that a team was selected
		widget.onSelectChallengeTeam();
		verify(mockChallengeClient).updateChallenge(eq(mockChallenge), any(AsyncCallback.class));
		verify(mockChallenge).setParticipantTeamId(SELECTED_TEAM_ID);
		verify(mockTeamSuggestBox).clear();
	}

}
