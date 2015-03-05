package org.sagebionetworks.web.unitclient.widget.entity;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
	
	@Test
	public void testSetPresenter() {
		widget.configure(testChallengeBundle);
		verify(mockView).setPresenter(widget);
	}
	
	private void verifyNoHrefSet() {
		verify(mockView, never()).setHref(anyString());
	}
	
	/**
	 * Verifies that setHref is called, and returns the value passed to the view
	 * @return
	 */
	private String verifyHref() {
		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockView).setHref(stringCaptor.capture());
		return stringCaptor.getValue();
	}
	
	@Test
	public void testConfigure() {
		widget.configure(testChallengeBundle);
		String href = verifyHref();
		assertTrue(href.contains("#!Synapse:"));
		assertTrue(href.contains(testProjectId));
		verify(mockView).setPresenter(widget);
	}
	
	@Test
	public void testConfigureNullChallenge() {
		testChallengeBundle.setChallenge(null);
		widget.configure(testChallengeBundle);
		verifyNoHrefSet();
	}
	
	@Test
	public void testConfigureNullProjectId() {
		testChallenge.setProjectId(null);
		widget.configure(testChallengeBundle);
		verifyNoHrefSet();
	}
}
