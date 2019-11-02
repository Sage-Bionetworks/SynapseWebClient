package org.sagebionetworks.web.unitclient.place;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.TeamSearch;

public class TeamSearchPlaceTest {

	TeamSearch.Tokenizer tokenizer = new TeamSearch.Tokenizer();
	String searchTerm;
	Integer start;

	@Before
	public void setup() {
		searchTerm = "testing 123";
		start = 20;
	}

	@Test
	public void testStandardCase() {
		// without start
		String testToken = searchTerm;
		TeamSearch place = tokenizer.getPlace(testToken);
		Assert.assertEquals(searchTerm, place.getSearchTerm());
		Assert.assertNull(place.getStart());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testStartCase() {
		// with start
		String testToken = searchTerm + Search.START_DELIMITER + start;
		TeamSearch place = tokenizer.getPlace(testToken);
		Assert.assertEquals(searchTerm, place.getSearchTerm());
		Assert.assertEquals(start, place.getStart());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
}
