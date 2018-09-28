package org.sagebionetworks.web.unitclient.place;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;

/**
 * Synapse Place token testing
 * @author jayhodgson
 *
 */
public class SynapsePlaceTest {
	
	public static final String ADMIN_DELIMITER = Synapse.getDelimiter(Synapse.EntityArea.CHALLENGE);
	public static final String WIKI_DELIMITER = Synapse.getDelimiter(Synapse.EntityArea.WIKI);
	public static final String FILES_DELIMITER = Synapse.getDelimiter(Synapse.EntityArea.FILES);
	public static final String TABLES_DELIMITER = Synapse.getDelimiter(Synapse.EntityArea.TABLES);
	public static final String DISCUSSION_DELIMITER = Synapse.getDelimiter(Synapse.EntityArea.DISCUSSION);
	public static final String DOCKER_DELIMITER = Synapse.getDelimiter(Synapse.EntityArea.DOCKER);
	public static final String VERSION_DELIMITER = "/version/";
	
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
		String testToken = testEntityId + VERSION_DELIMITER + testVersionNumber;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertEquals(testVersionNumber, place.getVersionNumber());
		Assert.assertNull(place.getAreaToken());
		Assert.assertNull(place.getArea());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testFilesVersionCase() {
		String testToken = testEntityId + VERSION_DELIMITER + testVersionNumber + FILES_DELIMITER;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertEquals(testVersionNumber, place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.FILES, place.getArea());
		Assert.assertNull(place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testInvalidVersionCase() {
		String testToken = testEntityId + VERSION_DELIMITER;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertNull(place.getVersionNumber());
		Assert.assertNull(place.getArea());
		Assert.assertNull(place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testAdminVersionCase() {
		String testToken = testEntityId + VERSION_DELIMITER + testVersionNumber + ADMIN_DELIMITER;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertEquals(testVersionNumber, place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.CHALLENGE, place.getArea());
		Assert.assertNull(place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testTablesQueryCase() {
		String queryToken = "abcdef";
		String testToken = testEntityId + TABLES_DELIMITER + TablesTab.TABLE_QUERY_PREFIX + queryToken;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertEquals(Synapse.EntityArea.TABLES, place.getArea());
		String areaToken = place.getAreaToken();
		assertTrue(areaToken.startsWith(TablesTab.TABLE_QUERY_PREFIX));
		assertEquals(queryToken, areaToken.substring(TablesTab.TABLE_QUERY_PREFIX.length()));
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testEntityDotVersionCase() {
		String testToken = testEntityId + "." + testVersionNumber;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertEquals(testVersionNumber, place.getVersionNumber());
		Assert.assertNull(place.getAreaToken());
		Assert.assertNull(place.getArea());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	
	@Test
	public void testWikiVersionCase() {
		String testToken = testEntityId + VERSION_DELIMITER + testVersionNumber + WIKI_DELIMITER + testAreaToken;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertEquals(testVersionNumber, place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.WIKI, place.getArea());
		Assert.assertEquals(testAreaToken, place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testFilesCase() {
		String testToken = testEntityId +  FILES_DELIMITER;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertNull(place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.FILES, place.getArea());
		Assert.assertNull(place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}
	
	@Test
	public void testAdminCase() {
		String testToken = testEntityId +  ADMIN_DELIMITER;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertNull(place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.CHALLENGE, place.getArea());
		Assert.assertNull(place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testDiscussionCase() {
		String testToken = testEntityId +  DISCUSSION_DELIMITER;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertNull(place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.DISCUSSION, place.getArea());
		Assert.assertNull(place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testDockerCase() {
		String testToken = testEntityId +  DOCKER_DELIMITER;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertNull(place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.DOCKER, place.getArea());
		Assert.assertNull(place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testWikiCase() {
		String testToken = testEntityId + WIKI_DELIMITER + testAreaToken;
		Synapse place = tokenizer.getPlace(testToken);
		
		Assert.assertEquals(testEntityId, place.getEntityId());
		Assert.assertNull(place.getVersionNumber());
		Assert.assertEquals(Synapse.EntityArea.WIKI, place.getArea());
		Assert.assertEquals(testAreaToken, place.getAreaToken());
		Assert.assertEquals(testToken, tokenizer.getToken(place));
	}

	@Test
	public void testGetHrefForDotVersionNull(){
		assertEquals(null, Synapse.getHrefForDotVersion(null));
	}
	
	@Test
	public void testGetTokenForDotVersionEmpty(){
		assertEquals(null, Synapse.getHrefForDotVersion(""));
	}
	
	@Test
	public void testGetTokenForDotVersionNoVersion(){
		assertEquals("#!Synapse:syn123", Synapse.getHrefForDotVersion("SYN123"));
	}
	
	@Test
	public void testGetTokenForDotVersionWithVersion(){
		assertEquals("#!Synapse:syn123.3", Synapse.getHrefForDotVersion("syn123.3"));
	}
}
