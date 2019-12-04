package org.sagebionetworks.web.unitclient.place;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.place.Team;

public class TeamPlaceTest {

	Team.Tokenizer tokenizer = new Team.Tokenizer();
	String testTeamId;

	@Before
	public void setup() {
		testTeamId = "42";
	}

	@Test
	public void testTeamId() {
		String testToken = testTeamId;
		Team place = tokenizer.getPlace(testToken);

		Assert.assertEquals(testTeamId, place.getTeamId());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
}
