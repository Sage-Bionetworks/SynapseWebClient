package org.sagebionetworks.web.unitclient.place;

import static org.junit.Assert.*;
import org.junit.Test;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;

/**
 * Profile Place token testing
 * @author jayhodgson
 *
 */
public class ProfilePlaceTest {
	
	Profile.Tokenizer tokenizer = new Profile.Tokenizer();
	String testUserId = "314159";
	
	@Test
	public void testUserIdOnlyCase() {
		String testToken = testUserId;
		Profile place = tokenizer.getPlace(testToken);
		
		assertEquals(testUserId, place.getUserId());
		//default area is projects
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
	}
	
	
	@Test
	public void testProjectsCase() {
		String testToken = testUserId + Profile.PROJECTS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testChallengesCase() {
		String testToken = testUserId + Profile.CHALLENGES_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.CHALLENGES, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testSettingsCase() {
		String testToken = testUserId + Profile.SETTINGS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.SETTINGS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testTeamsCase() {
		String testToken = testUserId + Profile.TEAMS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.TEAMS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testAllProjectFilterCase() {
		String testToken = testUserId + Profile.PROJECTS_DELIMITER + Profile.ALL_PROJECTS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
		assertEquals(ProjectFilterEnum.ALL, place.getProjectFilter());
	}
	
	@Test
	public void testFavProjectFilterCase() {
		String testToken = testUserId + Profile.PROJECTS_DELIMITER + Profile.FAV_PROJECTS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
		assertEquals(ProjectFilterEnum.FAVORITES, place.getProjectFilter());
	}
	
	@Test
	public void testMyProjectFilterCase() {
		String testToken = testUserId + Profile.PROJECTS_DELIMITER + Profile.CREATED_BY_ME_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
		assertEquals(ProjectFilterEnum.CREATED_BY_ME, place.getProjectFilter());
	}
	
	@Test
	public void testSharedWithMeProjectFilterCase() {
		String testToken = testUserId + Profile.PROJECTS_DELIMITER + Profile.SHARED_DIRECTLY_WITH_ME_PROJECTS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
		assertEquals(ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME, place.getProjectFilter());
	}

	@Test
	public void testAllMyTeamsProjectFilterCase() {
		String testToken = testUserId + Profile.PROJECTS_DELIMITER + Profile.ALL_MY_TEAM_PROJECTS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
		assertEquals(ProjectFilterEnum.ALL_MY_TEAM_PROJECTS, place.getProjectFilter());
	}
	
	@Test
	public void testTeamProjectFilterCase() {
		String teamId = "99993";
		String testToken = testUserId + Profile.PROJECTS_DELIMITER + Profile.TEAM_PROJECTS_DELIMITER + Profile.DELIMITER + teamId;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
		assertEquals(ProjectFilterEnum.TEAM, place.getProjectFilter());
		assertEquals(teamId, place.getTeamId());
	}

}
