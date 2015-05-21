package org.sagebionetworks.web.unitclient.place;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.place.SignedToken;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.server.servlet.NotificationTokenType;

/**
 *
 */
public class SignedTokenTest {
	
	SignedToken.Tokenizer tokenizer = new SignedToken.Tokenizer();
	String testTokenType, testSignedEncodedToken;
	@Before
	public void setup(){
		testTokenType = NotificationTokenType.JoinTeam.toString();
		testSignedEncodedToken = "1223334444";
	}	
	
	@Test
	public void testStandardCase() {
		String testToken =  testTokenType + SignedToken.DELIMITER + testSignedEncodedToken;
		SignedToken place = tokenizer.getPlace(testToken);
		Assert.assertEquals(testTokenType, place.getTokenType());
		Assert.assertEquals(testSignedEncodedToken, place.getSignedEncodedToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testMissingParams() {
		String testToken = testTokenType + SignedToken.DELIMITER;
		SignedToken place = tokenizer.getPlace(testToken);
		Assert.assertNull(place.getTokenType());
		Assert.assertNull(place.getSignedEncodedToken());
	}
}
