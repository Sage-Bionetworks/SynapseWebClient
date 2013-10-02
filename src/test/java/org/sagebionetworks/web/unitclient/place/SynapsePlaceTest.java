package org.sagebionetworks.web.unitclient.place;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.place.Synapse;

/**
 * Synapse Place token testing
 * @author jayhodgson
 *
 */
public class SynapsePlaceTest {
	
	Synapse.Tokenizer tokenizer = new Synapse.Tokenizer();
	String testEntityId, testAreaToken;
	Long testVersionNumber;
	@Before
	public void setup(){
		testAreaToken = "42";
		testEntityId = "syn1234";
		testVersionNumber = 9l;
	}	
	
	@Test
	public void testEntityOnlyCase() {
		String testToken = testEntityId;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertNull(place.getVersionNumber());
		Assert.assertNull(place.getAreaToken());
		Assert.assertNull(place.getArea());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testEntityVersionCase() {
		String testToken = testEntityId + Synapse.VERSION_DELIMITER + testVersionNumber;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertEquals(testVersionNumber, place.getVersionNumber());
		Assert.assertNull(place.getAreaToken());
		Assert.assertNull(place.getArea());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testFilesVersionCase() {
		String testToken = testEntityId + Synapse.VERSION_DELIMITER + testVersionNumber + Synapse.FILES_DELIMITER;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertEquals(testVersionNumber, place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.FILES, place.getArea());
		Assert.assertNull(place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testAdminVersionCase() {
		String testToken = testEntityId + Synapse.VERSION_DELIMITER + testVersionNumber + Synapse.ADMIN_DELIMITER;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertEquals(testVersionNumber, place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.ADMIN, place.getArea());
		Assert.assertNull(place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testWikiVersionCase() {
		String testToken = testEntityId + Synapse.VERSION_DELIMITER + testVersionNumber + Synapse.WIKI_DELIMITER + testAreaToken;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertEquals(testVersionNumber, place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.WIKI, place.getArea());
		Assert.assertEquals(testAreaToken, place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testFilesCase() {
		String testToken = testEntityId +  Synapse.FILES_DELIMITER;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertNull(place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.FILES, place.getArea());
		Assert.assertNull(place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testAdminCase() {
		String testToken = testEntityId +  Synapse.ADMIN_DELIMITER;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertNull(place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.ADMIN, place.getArea());
		Assert.assertNull(place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testWikiCase() {
		String testToken = testEntityId + Synapse.WIKI_DELIMITER + testAreaToken;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertNull(place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.WIKI, place.getArea());
		Assert.assertEquals(testAreaToken, place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

}
