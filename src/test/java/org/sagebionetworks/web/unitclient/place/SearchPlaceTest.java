package org.sagebionetworks.web.unitclient.place;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.place.Search;

/**
 * Search place token test
 * 
 * @author jayhodgson
 *
 */
public class SearchPlaceTest {

	Search.Tokenizer tokenizer = new Search.Tokenizer();
	String searchTerm;
	Long start;

	@Before
	public void setup() {
		searchTerm = "testing 123";
		start = 20l;
	}

	@Test
	public void testStandardCase() {
		// without start
		String testToken = searchTerm;
		Search place = tokenizer.getPlace(testToken);
		Assert.assertEquals(searchTerm, place.getSearchTerm());
		Assert.assertNull(place.getStart());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testStartCase() {
		// with start
		String testToken = searchTerm + Search.START_DELIMITER + start;
		Search place = tokenizer.getPlace(testToken);
		Assert.assertEquals(searchTerm, place.getSearchTerm());
		Assert.assertEquals(start, place.getStart());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
}
