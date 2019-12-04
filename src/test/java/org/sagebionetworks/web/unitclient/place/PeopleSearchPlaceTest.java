package org.sagebionetworks.web.unitclient.place;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.place.Search;

public class PeopleSearchPlaceTest {
	PeopleSearch.Tokenizer tokenizer = new PeopleSearch.Tokenizer();
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
		PeopleSearch place = tokenizer.getPlace(testToken);
		Assert.assertEquals(searchTerm, place.getSearchTerm());
		Assert.assertNull(place.getStart());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testStartCase() {
		// with start
		String testToken = searchTerm + Search.START_DELIMITER + start;
		PeopleSearch place = tokenizer.getPlace(testToken);
		Assert.assertEquals(searchTerm, place.getSearchTerm());
		Assert.assertEquals(start, place.getStart());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
}
