package org.sagebionetworks.web.unitclient.place;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.place.SignedToken;

/**
 *
 */
public class SignedTokenTest {

	SignedToken.Tokenizer tokenizer = new SignedToken.Tokenizer();
	String testSignedEncodedToken;

	@Before
	public void setup() {
		testSignedEncodedToken = "1223334444";
	}

	@Test
	public void testStandardCase() {
		String testToken = testSignedEncodedToken;
		SignedToken place = tokenizer.getPlace(testToken);
		Assert.assertEquals(testSignedEncodedToken, place.getSignedEncodedToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testLegacyCase() {
		String testToken = "abcd" + SignedToken.DELIMITER + testSignedEncodedToken;
		SignedToken place = tokenizer.getPlace(testToken);
		Assert.assertEquals(testSignedEncodedToken, place.getSignedEncodedToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testMissingParams() {
		String testToken = "abc" + SignedToken.DELIMITER;
		SignedToken place = tokenizer.getPlace(testToken);
		Assert.assertNull(place.getSignedEncodedToken());
	}
}
