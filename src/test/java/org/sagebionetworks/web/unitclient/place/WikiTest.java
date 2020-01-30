package org.sagebionetworks.web.unitclient.place;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.place.Wiki;

/**
 * Wiki Place token test
 * 
 * @author jayhodgson
 *
 */
public class WikiTest {

	Wiki.Tokenizer tokenizer = new Wiki.Tokenizer();
	String testOwnerId, testOwnerType, testWikiId;

	@Before
	public void setup() {
		testOwnerId = "syn1234";
		testOwnerType = ObjectType.ENTITY.toString();
		testWikiId = "20";
	}

	@Test
	public void testStandardCase() {
		String testToken = testOwnerId + Wiki.DELIMITER + testOwnerType + Wiki.DELIMITER + testWikiId;
		Wiki place = tokenizer.getPlace(testToken);
		Assert.assertEquals(testOwnerId, place.getOwnerId());
		Assert.assertEquals(testOwnerType, place.getOwnerType());
		Assert.assertEquals(testWikiId, place.getWikiId());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testNullWikiId() {
		String testToken = testOwnerId + Wiki.DELIMITER + testOwnerType;
		Wiki place = tokenizer.getPlace(testToken);
		Assert.assertEquals(testOwnerId, place.getOwnerId());
		Assert.assertEquals(testOwnerType, place.getOwnerType());
		Assert.assertNull(place.getWikiId());
	}

	@Test
	public void testMissingParams() {
		String testToken = testOwnerId + Wiki.DELIMITER;
		Wiki place = tokenizer.getPlace(testToken);
		Assert.assertNull(place.getOwnerId());
		Assert.assertNull(place.getOwnerType());
		Assert.assertNull(place.getWikiId());
	}
}
