package org.sagebionetworks.web.unitclient.place;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;

/**
 * Profile Place token testing
 * 
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
		// default area is profile
		assertEquals(Synapse.ProfileArea.PROFILE, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testProfileCase() {
		String testToken = testUserId + PROFILE_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROFILE, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testProjectsCase() {
		String testToken = testUserId + PROJECTS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testChallengesCase() {
		String testToken = testUserId + CHALLENGES_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.CHALLENGES, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testSettingsCase() {
		String testToken = testUserId + SETTINGS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.SETTINGS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testTeamsCase() {
		String testToken = testUserId + TEAMS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.TEAMS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testAllProjectFilterCase() {
		String testToken = testUserId + PROJECTS_DELIMITER + ALL_PROJECTS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
		assertEquals(ProjectFilterEnum.ALL, place.getProjectFilter());
	}

	@Test
	public void testFavProjectFilterCase() {
		String testToken = testUserId + PROJECTS_DELIMITER + FAV_PROJECTS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
		assertEquals(ProjectFilterEnum.FAVORITES, place.getProjectFilter());
	}

	@Test
	public void testMyProjectFilterCase() {
		String testToken = testUserId + PROJECTS_DELIMITER + CREATED_BY_ME_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
		assertEquals(ProjectFilterEnum.CREATED_BY_ME, place.getProjectFilter());
	}

	@Test
	public void testSharedWithMeProjectFilterCase() {
		String testToken = testUserId + PROJECTS_DELIMITER + SHARED_DIRECTLY_WITH_ME_PROJECTS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
		assertEquals(ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME, place.getProjectFilter());
	}

	@Test
	public void testAllMyTeamsProjectFilterCase() {
		String testToken = testUserId + PROJECTS_DELIMITER + ALL_MY_TEAM_PROJECTS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
		assertEquals(ProjectFilterEnum.ALL_MY_TEAM_PROJECTS, place.getProjectFilter());
	}

	@Test
	public void testTeamProjectFilterCase() {
		String teamId = "99993";
		String testToken = testUserId + PROJECTS_DELIMITER + TEAM_PROJECTS_DELIMITER + Profile.DELIMITER + teamId;
		Profile place = tokenizer.getPlace(testToken);
		assertEquals(testUserId, place.getUserId());
		assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		assertEquals(testToken, tokenizer.getToken(place));
		assertEquals(ProjectFilterEnum.TEAM, place.getProjectFilter());
		assertEquals(teamId, place.getTeamId());
	}

	public static final String SETTINGS_DELIMITER = Profile.getDelimiter(Synapse.ProfileArea.SETTINGS);
	public static final String PROJECTS_DELIMITER = Profile.getDelimiter(Synapse.ProfileArea.PROJECTS);
	public static final String PROFILE_DELIMITER = Profile.getDelimiter(Synapse.ProfileArea.PROFILE);
	public static final String CHALLENGES_DELIMITER = Profile.getDelimiter(Synapse.ProfileArea.CHALLENGES);
	public static final String TEAMS_DELIMITER = Profile.getDelimiter(Synapse.ProfileArea.TEAMS);

	public static final String ALL_PROJECTS_DELIMITER = Profile.getDelimiter(ProjectFilterEnum.ALL);
	public static final String FAV_PROJECTS_DELIMITER = Profile.getDelimiter(ProjectFilterEnum.FAVORITES);
	public static final String CREATED_BY_ME_DELIMITER = Profile.getDelimiter(ProjectFilterEnum.CREATED_BY_ME);
	public static final String ALL_MY_TEAM_PROJECTS_DELIMITER = Profile.getDelimiter(ProjectFilterEnum.ALL_MY_TEAM_PROJECTS);
	public static final String SHARED_DIRECTLY_WITH_ME_PROJECTS_DELIMITER = Profile.getDelimiter(ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME);
	public static final String TEAM_PROJECTS_DELIMITER = Profile.getDelimiter(ProjectFilterEnum.TEAM);


}
