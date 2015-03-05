package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadge;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadgeView;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeView;
import org.sagebionetworks.web.client.widget.entity.EntityIconsCache;
import org.sagebionetworks.web.shared.ChallengeBundle;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ChallengeBadgeTest {

	ChallengeBadgeView mockView;
	ChallengeBadge widget;
	ChallengeBundle testChallengeBundle;
	Challenge testChallenge;
	String testProjectName= "my test challenge project";
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(ChallengeBadgeView.class);
		widget = new ChallengeBadge(mockView);
		testChallengeBundle = new ChallengeBundle();
		testChallenge = new Challenge();
		testChallenge.setProjectId("syn123");
		testChallenge.setParticipantTeamId("12345");
		testChallengeBundle.setChallenge(testChallenge);
		testChallengeBundle.setProjectName(testProjectName);
	}
	
	@Test
	public void testSetPresenter() {
		widget.configure(testChallengeBundle);
		verify(mockView).setPresenter(widget);
	}
	
	@Test
	public void testOnClick() {
		widget.onClick();
		verify(mockPlaceChanger).goTo(any(Synapse.class));
	}
	
	@Test
	public void testOnClickNullChallenge() {
		testChallengeBundle.setChallenge(null);
		widget.onClick();
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testOnClickNullProjectId() {
		testChallenge.setProjectId(null);
		widget.onClick();
		verify(mockView).showErrorMessage(anyString());
	}
}
