package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadge;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadgeView;
import org.sagebionetworks.web.shared.ChallengeBundle;

public class ChallengeBadgeTest {

	ChallengeBadgeView mockView;
	ChallengeBadge widget;
	ChallengeBundle testChallengeBundle;
	Challenge testChallenge;
	public static final String testProjectName= "my test challenge project";
	public static final String testProjectId = "syn123";
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(ChallengeBadgeView.class);
		widget = new ChallengeBadge(mockView);
		testChallengeBundle = new ChallengeBundle();
		testChallenge = new Challenge();
		testChallenge.setProjectId(testProjectId);
		testChallenge.setParticipantTeamId("12345");
		testChallengeBundle.setChallenge(testChallenge);
		testChallengeBundle.setProjectName(testProjectName);
	}
	
	private void verifyNoProjectIdSet() {
		verify(mockView, never()).setProjectId(anyString());
	}
	
	/**
	 * Verifies that setHref is called, and returns the value passed to the view
	 * @return
	 */
	private String verifyProjectId() {
		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockView).setProjectId(stringCaptor.capture());
		return stringCaptor.getValue();
	}
	
	@Test
	public void testConfigure() {
		widget.configure(testChallengeBundle);
		String projectId = verifyProjectId();
		assertEquals(testProjectId, projectId);
	}
	
	@Test
	public void testConfigureNullChallenge() {
		testChallengeBundle.setChallenge(null);
		widget.configure(testChallengeBundle);
		verifyNoProjectIdSet();
	}
	
	@Test
	public void testConfigureNullProjectId() {
		testChallenge.setProjectId(null);
		widget.configure(testChallengeBundle);
		verifyNoProjectIdSet();
	}
}
