package org.sagebionetworks.web.unitserver.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.sagebionetworks.web.server.servlet.filter.PlacesRedirectFilter;

public class PlacesRedirectFilterTest {

	@Test
	public void testFixPath() {
		PlacesRedirectFilter filter = new PlacesRedirectFilter();
		assertEquals("", filter.fixPath(""));
		assertNull(filter.fixPath(null));
		assertEquals("/#!Synapse:syn1234", filter.fixPath("/Synapse:syn1234"));
		assertEquals("/#!Synapse:syn123/wiki/2222", filter.fixPath("/Synapse:syn123/wiki/2222"));
		assertEquals("NoChange", filter.fixPath("NoChange"));
	}

}
