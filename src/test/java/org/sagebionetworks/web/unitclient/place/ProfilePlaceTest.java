package org.sagebionetworks.web.unitclient.place;

import org.junit.Assert;
import org.junit.Test;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;

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
		
		Assert.assertEquals(testUserId, place.getUserId());
		//default area is projects
		Assert.assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	
	@Test
	public void testProjectsCase() {
		String testToken = testUserId + Profile.PROJECTS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		Assert.assertEquals(testUserId, place.getUserId());
		Assert.assertEquals(Synapse.ProfileArea.PROJECTS, place.getArea());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testChallengesCase() {
		String testToken = testUserId + Profile.CHALLENGES_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		Assert.assertEquals(testUserId, place.getUserId());
		Assert.assertEquals(Synapse.ProfileArea.CHALLENGES, place.getArea());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testSettingsCase() {
		String testToken = testUserId + Profile.SETTINGS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		Assert.assertEquals(testUserId, place.getUserId());
		Assert.assertEquals(Synapse.ProfileArea.SETTINGS, place.getArea());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testTeamsCase() {
		String testToken = testUserId + Profile.TEAMS_DELIMITER;
		Profile place = tokenizer.getPlace(testToken);
		Assert.assertEquals(testUserId, place.getUserId());
		Assert.assertEquals(Synapse.ProfileArea.TEAMS, place.getArea());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
}
