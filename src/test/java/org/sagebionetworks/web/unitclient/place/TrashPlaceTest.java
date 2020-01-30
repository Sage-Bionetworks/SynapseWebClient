package org.sagebionetworks.web.unitclient.place;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.place.Trash;

public class TrashPlaceTest {

	Trash.Tokenizer tokenizer = new Trash.Tokenizer();
	String testToken;
	Integer start;

	@Before
	public void setup() {
		testToken = ClientProperties.DEFAULT_PLACE_TOKEN;
		start = 20;
	}

	@Test
	public void testStandardCase() {
		// without start
		Trash place = tokenizer.getPlace(testToken);
		Assert.assertNull(place.getStart());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testStartCaseOneArgConstructor() {
		// with start, one argument constructor
		testToken += Trash.START_DELIMITER + start;
		Trash place = tokenizer.getPlace(testToken);
		Assert.assertEquals(start, place.getStart());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testStartCaseTwoArgConstructor() {
		// with start, one argument constructor
		Trash place = new Trash(testToken, start);
		Assert.assertEquals(start, place.getStart());
		Assert.assertEquals(testToken + Trash.START_DELIMITER + start, tokenizer.getToken(place));
	}
}
