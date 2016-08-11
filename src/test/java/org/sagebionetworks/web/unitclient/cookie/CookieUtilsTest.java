package org.sagebionetworks.web.unitclient.cookie;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.sagebionetworks.web.client.cookie.CookieUtils;

public class CookieUtilsTest {
	
	@Test
	public void testListRoundTrip(){
		// First create a list
		List<String> originalList = new ArrayList<String>();
		// First try a single value that includes the delimiter
		originalList.add("prefix"+CookieUtils.COOKIE_DELIMITER+"sufix");
		// Go to string
		String serialized = CookieUtils.createStringFromList(originalList);
		System.out.println(serialized);
		// The results should not contain the delimiter
		assertTrue(serialized.indexOf(CookieUtils.COOKIE_DELIMITER) < 0);
		// It should contain the escape sequence
		assertTrue(serialized.indexOf(CookieUtils.COOKIE_DEL_ESCAPE) > 0);
		// Now create a new list from the string
		List<String> copy = CookieUtils.createListFromString(serialized);
		// Should match the original
		assertEquals(originalList, copy);
		
		// Try more than one value
		originalList.add("ValueTwo");
		originalList.add(CookieUtils.COOKIE_DELIMITER+"Value"+CookieUtils.COOKIE_DELIMITER+"Three");
		serialized = CookieUtils.createStringFromList(originalList);
		System.out.println(serialized);
		// Now create a new list from the string
		copy = CookieUtils.createListFromString(serialized);
		// Should match the original
		assertEquals(originalList, copy);
		
	}

	@Test
	public void testGetDomain() {
		assertEquals(".synapse.org", CookieUtils.getDomain("www.synapse.org"));
		assertEquals(".synapse.org", CookieUtils.getDomain("staging.synapse.org"));
		assertEquals(".synapse.org", CookieUtils.getDomain("shiny.synapse.org"));
		assertNull(CookieUtils.getDomain("localhost"));
		assertNull(CookieUtils.getDomain("127.0.0.1"));
		assertNull(CookieUtils.getDomain(null));
		assertNull(CookieUtils.getDomain(""));
	}
}
