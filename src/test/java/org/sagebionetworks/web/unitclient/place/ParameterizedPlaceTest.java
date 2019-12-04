package org.sagebionetworks.web.unitclient.place;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.sagebionetworks.web.client.place.ParameterizedPlace;
import org.sagebionetworks.web.client.place.ParameterizedToken;

/**
 * Wiki Place token test
 * 
 * @author jayhodgson
 *
 */
public class ParameterizedPlaceTest {

	@Test
	public void testStandardCase() {
		String testToken = "a=1&b=2&c=&d&";
		ParameterizedPlace place = new ParameterizedPlace(testToken);
		assertEquals("1", place.getParam("a"));
		assertEquals("2", place.getParam("b"));
		assertEquals(null, place.getParam("c"));
		assertEquals(null, place.getParam("d"));
		String newToken = place.toToken();
		assertTrue(newToken.contains("a=1"));
		assertTrue(newToken.contains("b=2"));
		assertTrue(!newToken.endsWith("&"));
	}

	@Test
	public void testReplaceToken() {
		String token = "a=1";
		ParameterizedPlace place = new ParameterizedPlace(token);
		assertEquals("1", place.getParam("a"));
		ParameterizedToken newToken = new ParameterizedToken("");
		newToken.put("b", "2");

		place.setParameterizedToken(newToken);
		assertEquals("2", place.getParam("b"));
		assertEquals(null, place.getParam("a"));
	}

	@Test
	public void testNoParams() {
		String testToken = "";
		ParameterizedPlace place = new ParameterizedPlace(testToken);
		// revert to the default token
		String newToken = place.toToken();
		assertEquals(newToken, ParameterizedToken.DEFAULT_TOKEN);
	}

	@Test
	public void testClearParams() {
		String testToken = "a=1&b=2&c=&d&";
		ParameterizedPlace place = new ParameterizedPlace(testToken);
		assertEquals("1", place.getParam("a"));
		place.clearParams();
		String newToken = place.toToken();
		assertEquals(newToken, ParameterizedToken.DEFAULT_TOKEN);
	}

}
