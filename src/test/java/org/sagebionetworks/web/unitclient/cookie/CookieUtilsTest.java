package org.sagebionetworks.web.unitclient.cookie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.sagebionetworks.web.client.cookie.CookieUtils;

public class CookieUtilsTest {
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
