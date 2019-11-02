package org.sagebionetworks.web.unitclient.place;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.sagebionetworks.web.client.place.Account;

public class AccountPlaceTest {

	Account.Tokenizer tokenizer = new Account.Tokenizer();
	String testToken = "+";

	@Test
	public void testAccountToken() {
		// token should not be changed (and this should not reference javascript, since the check for FF is
		// on getFixedToken())
		Account place = tokenizer.getPlace(testToken);
		place = tokenizer.getPlace(tokenizer.getToken(place));
		assertEquals(testToken, place.toToken());
	}
}
